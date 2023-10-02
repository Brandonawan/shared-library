#!/usr/bin/env groovy

// Define a function to check files and run the pipeline
def call() {
    pipeline {
        agent any
        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }
            
            stage('Check Files') {
                steps {
                    script {
                        checkExecutableFile('jenkin-build')
                        checkFileExists('pipeline-config.yml')
                    }
                }
            }

            stage('Read Docker Image Name') {
                steps {
                    script {
                        def dockerConfig = readYaml file: 'pipeline-config.yml'
                        def dockerImage = dockerConfig.dockerImage

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
                    sh './jenkin-build'
                }
            }
        }
    }
}

// Define a function to check if a file exists and is executable
def checkExecutableFile(fileName) {
    def file = new File(fileName)
    if (!file.exists()) {
        error "File '${fileName}' not found in the repository."
    }
    if (!file.canExecute()) {
        error "File '${fileName}' is not executable."
    }
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





