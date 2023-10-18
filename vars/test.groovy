#!/usr/bin/env groovy

// Define a function to check files and run the pipeline
def call() {

    def jenkinsBuildPath = 'jenkins/jenkin-build'
    def pipelineConfigPath = 'jenkins/pipeline-config.yml'
    def confluenceDocLink = 'https://your-confluence-link.com/documentation'

    pipeline {
        agent any
        options {
            ansiColor('xterm')
            timestamps()
        }
        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Check Files') {
                steps {
                    script {
                        if (jenkinsBuildPath.isEmpty()) {
                            error("Error: No build script is found. Please specify a valid file path. Refer to the documentation for guidance: [${confluenceDocLink}]")
                        }

                        checkFileExistsInternal(jenkinsBuildPath)
                        checkFileExists(pipelineConfigPath)

                        // Check if jenkins-build is executable
                        checkIfJenkinsBuildIsExecutable(jenkinsBuildPath)
                    }
                }
            }

            stage('Read Docker Image Name') {
                steps {
                    script {
                        // Try to read the Docker image name from the pipeline-config.yml file
                        try {
                            def dockerConfig = readYaml file: pipelineConfigPath
                            if (dockerConfig && dockerConfig.dockerImage) {
                                // Set the Docker image name as an environment variable
                                env.DOCKER_IMAGE = dockerConfig.dockerImage
                            } else {
                                error("Error: Docker image name not found in ${pipelineConfigPath}. Please specify a Docker image name in the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]")
                            }
                        } catch (e) {
                            error("Error: Could not read Docker image name from ${pipelineConfigPath}. Please check the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]")
                        }
                    }
                }
            }

            stage('Run Inside Docker Image') {
                agent {
                    docker {
                        image "${env.DOCKER_IMAGE}"
                        args '--user=root -v /mnt:/mnt'
                        reuseNode(true) // Always pull the image if not available locally
                    }
                }
                steps {
                    sh 'apt-get update'
                    sh 'apt-get install -y python3-venv python3-pip' // Install Python virtualenv and pip
                    sh 'python3 -m venv venv' // Create a virtual environment
                    sh '. venv/bin/activate' // Activate the virtual environment using the dot command

                    // Install dependencies (if you have a requirements.txt file)
                    sh 'pip install -r requirements.txt'

                    // Run the script (adjust the command accordingly)
                    sh "./${jenkinsBuildPath}"

                    // Deactivate the virtual environment using the deactivate function
                    sh 'deactivate || true' // Use '|| true' to ignore errors if deactivate fails
                }
            }

            stage('Clean Workspace') {
                steps {
                    cleanWs()
                    echo 'Workspace cleaned successfully'
                    echo '${env.JOB_NAME} #${env.BUILD_NUMBER} completed successfully}'
                    echo 'View Documentation: ${confluenceDocLink}'
                }
            }
        }
    }
}

def checkFileExistsInternal(fileName) {
    def fileExists = fileExists(fileName)
    if (!fileExists) {
        error("Error: File '${fileName}' not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]")
    }
}

def checkFileExists(fileName) {
    checkFileExistsInternal(fileName)
}

def checkIfJenkinsBuildIsExecutable(fileName) {
    if (!fileExists(fileName)) {
        error("Error: The '${fileName}' file is not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]")
    }

    def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
    if (isExecutable != 0) {
        error("Error: The '${fileName}' file is not executable. Refer to the documentation for guidance: [${confluenceDocLink}]")
    }
}
