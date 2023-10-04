
#!/usr/bin/env groovy

def call() {

    def jenkinBuildPath = 'scripts/jenkin-build'
    def pipelineConfigPath = 'scripts/pipeline-config.yml'

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
                        if (jenkinBuildPath.isEmpty()) {
                            error "No build script is found. Please specify a valid file path."
                        }

                        checkFileExistsInternal(jenkinBuildPath)
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
                        args '--user=root -v /mnt:/mnt'
                        reuseNode(true)
                    }
                }
                steps {
                    sh 'apt-get update'
                    sh 'apt-get install -y python3-venv python3-pip'
                    sh 'python3 -m venv venv'
                    sh '. venv/bin/activate'

                    // Install dependencies (if you have a requirements.txt file)
                    sh 'pip install -r requirements.txt'

                    // Retry mechanism for running the script with a maximum of 3 retries
                    script {
                        def maxRetries = 3
                        def retryCount = 0
                        def success = false

                        while (!success && retryCount < maxRetries) {
                            try {
                                sh "./${jenkinBuildPath}"
                                success = true
                            } catch (Exception e) {
                                retryCount++
                                if (retryCount < maxRetries) {
                                    echo "Failed to run ${jenkinBuildPath}. Retrying (${retryCount}/${maxRetries})..."
                                    sleep 10  // Add a delay before retrying
                                } else {
                                    error "Failed to run ${jenkinBuildPath} after ${maxRetries} retries."
                                }
                            }
                        }
                    }

                    sh 'deactivate || true'
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

//             stage('Check Files') {
//                 steps {
//                     script {
//                         if (jenkinBuildPath.isEmpty()) {
//                             error "No build script is found. Please specify a valid file path."
//                         }

//                         checkFileExistsInternal(jenkinBuildPath)
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
//                         def dockerImage = 'fedora:30'

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

//             stage('Run Inside Docker Image') {
//                 agent {
//                     docker {
//                         image "${env.DOCKER_IMAGE}"
//                         args '--user=root -v /mnt:/mnt'
//                         reuseNode(true) // Always pull the image if not available locally
//                     }
//                 }
//                 steps {
//                     sh 'apt-get update'
//                     sh 'apt-get install -y python3-venv python3-pip' // Install Python virtualenv and pip
//                     sh 'python3 -m venv venv' // Create a virtual environment
//                     sh '. venv/bin/activate' // Activate the virtual environment using the dot command

//                     // Install dependencies (if you have a requirements.txt file)
//                     sh 'pip install -r requirements.txt'

//                     // Run the script (adjust the command accordingly)
//                     sh "./${jenkinBuildPath}"

//                     // Deactivate the virtual environment using the deactivate function
//                     sh 'deactivate || true' // Use '|| true' to ignore errors if deactivate fails
//                 }
//             }
//         }
//     }
// }

// def checkFileExistsInternal(fileName) {
//     def fileExists = fileExists(fileName)
//     if (!fileExists) {
//         error "File '${fileName}' not found in the repository."
//     }
// }

// def checkFileExists(fileName) {
//     checkFileExistsInternal(fileName)
// }

// def checkIfJenkinBuildIsExecutable(fileName) {
//     if (!fileExists(fileName)) {
//         error "The '${fileName}' file is not found in the repository."
//     }

//     def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
//     if (isExecutable != 0) {
//         error "The '${fileName}' file is not executable."
//     }
// }


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
