#!/usr/bin/env groovy

def dockerConfig // Declare dockerConfig at a higher scope

def call() {
    pipeline {
        agent any
        stages {
            stage('Validation and Setup') {
                steps {
                    script {
                        sh 'pwd'  // Print the current working directory (CWD)
                        sh 'ls -la'  // List all files and their permissions in the current directory

                        // Validate pipeline-config.yml
                        dockerConfig = readYaml file: 'pipeline-config.yml' // Assign to the higher scope variable
                        // validateDockerConfig('${WORKSPACE}/dockerConfig')

                        // Check if jenkin-build script exists and is executable
                        // validateExecutableScript('${WORKSPACE}/jenkin-build')

                        // Set default values if not provided
                        dockerConfig.dockerImage = dockerConfig.dockerImage ?: 'maven:3-alpine'
                    }
                }
            }

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Run Inside Docker Image') {
                agent {
                    docker {
                        image "${dockerConfig.dockerImage}"
                        args '--user=root'
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
                    // sh './jenkin-build'
                    sh "${WORKSPACE}/jenkin-build"
                }
            }
        }
    }
}

def validateDockerConfig(config) {
    if (!config) {
        error("pipeline-config.yml is missing or empty.")
    }

    if (!config.dockerImage) {
        error("Docker image name is missing in pipeline-config.yml.")
    }

    // Add more validation checks as needed
}

def validateExecutableScript(scriptName) {
    sh 'ls -la'  // List all files and their permissions in the current directory before checking the script
    if (!fileExists(scriptName)) {
        error("${scriptName} script is missing in the repository.")
    }

    if (!isScriptExecutable(scriptName)) {
        error("${scriptName} script is not executable. Make sure to run 'chmod +x ${scriptName}' before committing.")
    }
}

def fileExists(fileName) {
    return new File(fileName).exists()
}

def isScriptExecutable(scriptName) {
    return new File(scriptName).canExecute()
}



// def call() {
//     pipeline {
//         agent any
//         stages {
//             stage('Checkout') {
//                 steps {
//                     checkout scm
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
//                     sh '. venv/bin/activate' // Activate the virtual environment using dot command

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
//                     sh './jenkin-build'
//                 }
//             }
//         }
//     }
// }


// def call() {
//         pipeline {
//         agent {
//             docker {
//                 // Pull the Ubuntu image and run as root user
//                 image 'ubuntu:latest'
//                 args '--user=root'
//             }
//         }

//         stages {
//             stage('Checkout') {
//                 steps {
//                     checkout scm
//                 }
//             }

//             stage('Run Inside Ubuntu Docker Image') {
//                 steps {
//                     sh 'apt-get update'
//                     sh 'apt-get install -y python3-venv python3-pip' // Install Python virtualenv and pip
//                     sh 'python3 -m venv venv' // Create a virtual environment
//                     sh '. venv/bin/activate' // Activate the virtual environment using dot command

//                     // Install dependencies (if you have a requirements.txt file)
//                     sh 'pip install -r requirements.txt'

//                     // Run tests (adjust the command accordingly)
//                     sh 'pytest --junitxml=pytest-results.xml'

//                     // Deactivate the virtual environment using the deactivate function
//                     sh 'deactivate || true' // Use '|| true' to ignore errors if deactivate fails
//                 }
//             }
//             stage('Deliver') {
//             steps {
//                 sh './jenkin-build'
//             }
//         }
//         }
//     }
// }


// def call() {
//     pipeline {
//         agent any

//         stages {
//             stage('Read YAML') {
//                 steps {
//                     script {
//                         // Define yamlData within the script block
//                         def yamlData = readYaml file: 'pipeline-config.yml'
                        
//                         // Access data from the YAML file
//                         def name = yamlData.name
//                         def age = yamlData.age
//                         def email = yamlData.email
                        
//                         // Print the values for demonstration
//                         echo "Name: ${name}"
//                         echo "Age: ${age}"
//                         echo "Email: ${email}"
//                     }
//                 }
//             }

//             stage('Run jenkin-build Script') {
//                 steps {
//                     script {
//                         // Call the external script without passing arguments
//                         sh './jenkin-build'
//                     }
//                 }
//             }
//         }
//     }
// }





