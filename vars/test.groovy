#!/usr/bin/env groovy

// Define a function to check files and run the pipeline
def call() {

    def jenkinBuildPath = 'jenkin-build'
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
    } else {
        def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
        if (isExecutable != 0) {
            // Attempt to make the file executable
            sh "chmod +x ${fileName}"
            
            // Check again if it's executable now
            isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
            if (isExecutable == 0) {
                echo "Made '${fileName}' executable."
            } else {
                error "Failed to make '${fileName}' executable."
            }
        }
    }
}


// // Define a function to check files and run the pipeline
// def call() {
//     pipeline {
//         agent any
//         options {
//             ansiColor('xterm')
//             timestamps()
//         }
//         stages {
//             stage('Checkout') {
//                 steps {
//                     checkout scm
//                 }
//             }

//             // stage('Create Non-Executable File') {
//             //     steps {
//             //         script {
//             //             writeFile file: 'jenkin-build', text: 'This is a non-executable file content'
//             //         }
//             //     }
//             // }

//             stage('Check Files') {
//                 steps {
//                     script {
//                         checkFileExists('jenkin-build')
//                         checkFileExists('pipeline-config.yml')

//                         // Check if jenkin-build is executable
//                         checkIfJenkinBuildIsExecutable()
//                     }
//                 }
//             }

//             stage('Read Docker Image Name') {
//                 steps {
//                     script {
//                         def dockerConfig = readYaml file: 'pipeline-config.yml'
//                         def dockerImage = dockerConfig.dockerImage

//                         // Set the Docker image name as an environment variable
//                         env.DOCKER_IMAGE = dockerImage
//                     }
//                 }
//             }

//             stage('Run Inside Docker Image') {
//                 agent {
//                     docker {
//                         image "${env.DOCKER_IMAGE}"
//                         args '--user=root'
//                     }
//                 }
//                 steps {
//                     sh 'apt-get update'
//                     sh 'apt-get install -y python3-venv python3-pip' // Install Python virtualenv and pip
//                     sh 'python3 -m venv venv' // Create a virtual environment
//                     sh '. venv/bin/activate' // Activate the virtual environment using the dot command

//                     // Install dependencies (if you have a requirements.txt file)
//                     sh 'pip install -r requirements.txt'

//                     // Run tests (adjust the command accordingly)
//                     sh 'pytest --junitxml=pytest-results.xml'

//                     // Deactivate the virtual environment using the deactivate function
//                     sh 'deactivate || true' // Use '|| true' to ignore errors if deactivate fails
//                 }
//             }

//             stage('Deliver') {
//                 steps {
//                     sh ''' #!/bin/bash
//                     ./jenkin-build
//                     '''
//                 }
//             }
//         }
//     }
// }

// // Define a function to check if a file exists
// def checkFileExists(fileName) {
//     def fileExists = fileExists(fileName)
//     if (!fileExists) {
//         error "File '${fileName}' not found in the repository."
//     }
// }

// // Define a function to check if jenkin-build is executable
// def checkIfJenkinBuildIsExecutable() {
//     script {
//         if (!fileExists('jenkin-build')) {
//             error "The 'jenkin-build' file is not found in the repository."
//         }

//         def isExecutable = sh(script: "test -x jenkin-build", returnStatus: true)
//         if (isExecutable != 0) {
//             error "The 'jenkin-build' file is not executable."
//         }
//     }
// }










