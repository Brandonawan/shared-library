#!/usr/bin/env groovy

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
            stage('Check Files') {
                steps {
                    script {
                        echo "Starting 'Check Files' stage"
                        if (jenkinsBuildPath.isEmpty()) {
                            error "Error: No build script is found. Please specify a valid file path. Refer to the documentation for guidance: [${confluenceDocLink}]"
                        }

                        checkFileExistsInternal(jenkinsBuildPath)
                        checkFileExists(pipelineConfigPath)

                        // Check if jenkins-build is executable
                        checkIfJenkinsBuildIsExecutable(jenkinsBuildPath)
                    }
                }
            }

            stage('Checkout') {
                steps {
                    script {
                        echo "Starting 'Checkout' stage"
                        def pipelineConfigContent = readFile(file: pipelineConfigPath)
                        def pipelineConfig = readYaml text: pipelineConfigContent

                        echo "Starting 'Validate YAML Configuration' stage"

                        def errors = []  // Create an array to collect errors

                        try {
                            if (!pipelineConfig.token) {
                                errors.add("Error: 'token' key is missing or misconfigured in the YAML configuration.")
                            }

                            if (!pipelineConfig.label) {
                                errors.add("Error: 'label' key is missing or misconfigured in the YAML configuration.")
                            }

                            if (!pipelineConfig.dockerImage) {
                                errors.add("Error: 'dockerImage' key is missing or misconfigured in the YAML configuration.")
                            }

                            if (!pipelineConfig.scmCheckoutStrategies) {
                                errors.add("Error: 'scmCheckoutStrategies' key is missing or misconfigured in the YAML configuration.")
                            }

                            if (errors) {
                                // If there are errors, log each one
                                for (error in errors) {
                                    error(error)
                                }
                            } else {
                                echo "YAML configuration is valid."
                            }

                            if (pipelineConfig.scmCheckoutStrategies) {
                                def defaultStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'default' }
                                def customStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'custom-checkout' }
                                def repoToolStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'repo-tool-with-gh-token' }

                                if (defaultStrategy) {
                                    echo "Checking out Source Code using 'SCM default' strategy."
                                    checkout scm
                                } else if (customStrategy) {
                                    echo "Checking out Source Code using 'SCM custom-checkout' strategy."
                                    sh "./${customStrategy['checkout-script-name']}"
                                } else if (repoToolStrategy) {
                                    echo "Checking out Source Code using 'repo-tool-with-gh-token' strategy."

                                    // Install Repo tool if not already installed
                                    sh "if [ ! -f \"\$(which repo)\" ]; then curl https://storage.googleapis.com/git-repo-downloads/repo > /var/lib/jenkins/bin/repo; chmod +x /var/lib/jenkins/bin/repo; echo 'Repo tool installation completed.'; fi"

                                    // Fetch the manifest repository
                                    dir('repo') {
                                        script {
                                            // Add the directory containing 'repo' to the PATH
                                            def repoDir = '/var/lib/jenkins/bin'  // Adjust to the actual path where 'repo' is located
                                            env.PATH = "${repoDir}:${env.PATH}"
                                        }
                                        withCredentials([string(credentialsId: repoToolStrategy['github-token-jenkins-credential-id'], variable: 'GITHUB_TOKEN')]) {
                                            sh "repo init -u ${repoToolStrategy['repo-manifest-url']} -b ${repoToolStrategy['repo-manifest-branch']}"
                                            sh "repo sync"
                                        }
                                    }
                                    // Checkout the specified manifest group (uncomment if needed)
                                    // sh "repo forall -c 'git checkout ${repoToolStrategy['repo-manifest-branch']}' -g ${repoToolStrategy['repo-manifest-group']}"
                                } else {
                                    echo "No supported checkout strategy found in the configuration. Skipping checkout."
                                }
                            } else {
                                echo "No scmCheckoutStrategies defined in the configuration. Skipping checkout."
                            }
                        } catch (e) {
                            error "Error: Failed to validate the YAML configuration or checkout source code. Please check the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]"
                        }
                    }
                }
            }

            stage('Read Docker Image Name') {
                steps {
                    script {
                        echo "Starting 'Read Docker Image Name' stage"
                        try {
                            def dockerConfig = readYaml file: pipelineConfigPath
                            if (dockerConfig && dockerConfig.dockerImage) {
                                env.DOCKER_IMAGE = dockerConfig.dockerImage
                            } else {
                                error "Error: Docker image name not found in ${pipelineConfigPath}. Please specify a Docker image name in the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]"
                            }
                        } catch (e) {
                            error "Error: Could not read Docker image name from ${pipelineConfigPath}. Please check the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]"
                        }
                    }
                }
            }

            stage('Run Inside Docker Image') {
                agent {
                    docker {
                        image "${env.DOCKER_IMAGE}"
                        args '--user=root -v /mnt:/mnt'
                        reuseNode(true)
                    }
                }
                steps {
                    echo "Starting 'Run Inside Docker Image' stage"
                    sh 'apt-get update'
                    sh 'apt-get install -y python3-venv python3-pip'
                    sh 'python3 -m venv venv'
                    sh '. venv/bin/activate'

                    sh 'pip install -r requirements.txt'

                    sh "./${jenkinsBuildPath}"

                    sh 'deactivate || true'
                }
            }

            stage('Clean Workspace') {
                steps {
                    echo "Starting 'Clean Workspace' stage"
                    cleanWs()
                    echo "${env.JOB_NAME} #${env.BUILD_NUMBER} completed successfully"
                    echo "View Documentation: ${confluenceDocLink}"
                }
            }
        }
    }
}

def checkFileExistsInternal(fileName) {
    echo "Checking if file '${fileName}' exists."
    def fileExists = fileExists(fileName)
    if (!fileExists) {
        error "Error: File '${fileName}' not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]"
    }
}

def checkFileExists(fileName) {
    echo "Checking if file '${fileName}' exists."
    checkFileExistsInternal(fileName)
}

def checkIfJenkinsBuildIsExecutable(fileName) {
    echo "Checking if '${fileName}' is executable."
    if (!fileExists(fileName)) {
        error "Error: The '${fileName}' file is not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]"
    }

    def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
    if (isExecutable != 0) {
        error "Error: The '${fileName}' file is not executable. Refer to the documentation for guidance: [${confluenceDocLink}]"
    }
}
