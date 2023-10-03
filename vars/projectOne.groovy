#!/usr/bin/env groovy

// Define a function to check files and run the pipeline
def call() {

    // def jenkinBuildPath = 'jenkin-build'
    def jenkinBuildPath = 'hello.py'
    def pipelineConfigPath = 'config/pipeline-config.yml'

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
                        checkFileExists(jenkinBuildPath)
                        checkFileExists(pipelineConfigPath)

                        // Check if jenkin-build is executable
                        checkIfJenkinBuildIsExecutable(jenkinBuildPath)
                    }
                }
            }

            stage('Read Docker Image Name') {
                steps {
                    script {
                        // Set the default Docker image name
                        def dockerImage = 'fedora:30'

                        // Try to read the Docker image name from the pipeline-config.yml file
                        try {
                            def dockerConfig = readYaml file: pipelineConfigPath
                            if (dockerConfig && dockerConfig.dockerImage) {
                                dockerImage = dockerConfig.dockerImage
                            }
                        } catch (e) {
                            // If the file does not exist or cannot be read, use the default image name
                            logger.warning("Could not read Docker image name from ${pipelineConfigPath}. Using default image name: ${dockerImage}")
                        }

                        // Set the Docker image name as an environment variable
                        env.DOCKER_IMAGE = dockerImage
                    }
                }
            }

            stage('Run Inside Docker Image') {
                agent {
                    docker {
                        image "${env.DOCKER_IMAGE}"
                        args '--user=root'
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

                    // Run tests (adjust the command accordingly)
                    sh 'pytest --junitxml=pytest-results.xml'

                    // Deactivate the virtual environment using the deactivate function
                    sh 'deactivate || true' // Use '|| true' to ignore errors if deactivate fails
                }
            }

            stage('Deliver') {
                steps {
                    sh ''' #!/bin/bash
                    ./jenkin-build
                    '''
                }
            }
        }
    }
}

def checkFileExistsInternal(fileName) {
    def fileExists = fileExists(fileName)
    if (!fileExists) {
        error "File '${fileName}' not found in the repository."
    }
}

def checkFileExists(fileName) {
    checkFileExistsInternal(fileName)
}

def checkIfJenkinBuildIsExecutable(fileName) {
    if (!fileExists(fileName)) {
        error "The '${fileName}' file is not found in the repository."
    }

    def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
    if (isExecutable != 0) {
        error "The '${fileName}' file is not executable."
    }
}


// // Define a function to check files and run the pipeline
// def call() {
//     def jenkinBuildPath = 'scripts/jenkin-build'
//     def pipelineConfigPath = 'scripts/pipeline-config.yml'

//     pipeline {
//         agent {
//             label readYaml(file: pipelineConfigPath).label ?: 'component-ci-nodes'
//         }
//         options {
//             ansiColor('xterm')
//             timestamps()
//         }
//         triggers {
//             GenericTrigger(
//                 token: readYaml(file: pipelineConfigPath).token ?: 'MEV_CI_DOCS',
//                 printContributedVariables: true,
//                 printPostContent: false,
//             )
//         }
//         stages {
//             stage('Checkout') {
//                 steps {
//                     checkout scm
//                 }
//             }
//             stage('Check Files') {
//                 steps {
//                     script {
//                         checkFileExists(jenkinBuildPath)
//                         checkFileExists(pipelineConfigPath)

//                         // Check if jenkin-build is executable
//                         checkIfJenkinBuildIsExecutable(jenkinBuildPath)
//                     }
//                 }
//             }

//             stage('Read Docker Image Name') {
//                 steps {
//                     script {
//                         // Set the default Docker image name
//                         def dockerImage = 'ubuntu:latest'

//                         // Try to read the Docker image name from the pipeline-config.yml file
//                         try {
//                             def dockerConfig = readYaml file: pipelineConfigPath
//                             if (dockerConfig && dockerConfig.dockerImage) {
//                                 dockerImage = dockerConfig.dockerImage
//                             }
//                         } catch (e) {
//                             // If the file does not exist or cannot be read, use the default image name
//                             logger.warning("Could not read Docker image name from ${pipelineConfigPath}. Using default image name: ${dockerImage}")
//                         }

//                         // Set the Docker image name as an environment variable
//                         env.DOCKER_IMAGE = dockerImage
//                     }
//                 }
//             }

//             stage('Build') {
//                 agent {
//                     docker {
//                         image "${env.DOCKER_IMAGE}"
//                         registryUrl 'https://artifact-bxdsw.sc.intel.com:9444'
//                         registryCredentialsId 'nexus-docker-creds'
//                         args '--user=root -v /mnt:/mnt'
//                         alwaysPull true
//                     }
//                 }
//                 steps {
//                     withCredentials([usernamePassword(credentialsId: 'nexus-docker-creds', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
//                         sh ''' #!/bin/bash
//                         ./${jenkinBuildPath}
//                         '''
//                     }
//                 }
//             }
//         }
//     }
// }
