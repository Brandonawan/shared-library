#!/usr/bin/env groovy

def call() {

    def jenkinsBuildPath = 'jenkins/jenkin-build'
    def pipelineConfigPath = 'jenkins/pipeline-config.yml'
    def confluenceDocLink = 'https://your-confluence-link.com/documentation'
    def customCheckoutScriptName = 'custom-checkout.sh'

    pipeline {
        agent any
        options {
            ansiColor('xterm')
            timestamps()
        }
        stages {
            // stage('Checkout') {
            //     steps {
            //         checkout scm
            //     }
            stage('Checkout') {
                steps {
                    script {
                        def pipelineConfigContent = readFile(file: pipelineConfigPath)
                        def pipelineConfig = readYaml text: pipelineConfigContent

                        if (pipelineConfig.scmCheckoutStrategies) {
                            def defaultStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'default' }
                            def customStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'custom-checkout' }

                            if (defaultStrategy) {
                                echo "Checking out using 'default' strategy."
                                checkout scm
                            } else if (customStrategy) {
                                echo "Checking out using 'custom-checkout' strategy."
                                // sh "./${customStrategy['checkout-script-name']}"
                                sh "./${customCheckoutScriptName}"
                            } else {
                                echo "No supported checkout strategy found in the configuration. Skipping checkout."
                            }
                        } else {
                            echo "No scmCheckoutStrategies defined in the configuration. Skipping checkout."
                        }
                    }
                }
            }

            stage('Check Files') {
                steps {
                    script {
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

            stage('Read Docker Image Name') {
                steps {
                    script {
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
                    cleanWs()
                    echo "${env.JOB_NAME} #${env.BUILD_NUMBER} completed successfully"
                    echo "View Documentation: ${confluenceDocLink}"
                }
            }
        }
    }
}

def checkFileExistsInternal(fileName) {
    def fileExists = fileExists(fileName)
    if (!fileExists) {
        error "Error: File '${fileName}' not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]"
    }
}

def checkFileExists(fileName) {
    checkFileExistsInternal(fileName)
}

def checkIfJenkinsBuildIsExecutable(fileName) {
    if (!fileExists(fileName)) {
        error "Error: The '${fileName}' file is not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]"
    }

    def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
    if (isExecutable != 0) {
        error "Error: The '${fileName}' file is not executable. Refer to the documentation for guidance: [${confluenceDocLink}]"
    }
}
