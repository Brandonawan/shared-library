#!/usr/bin/env groovy

def call() {
    pipeline {
        agent any

        stages {
            stage('Read Config YAML') {
                steps {
                    script {
                        // Define yamlConfig within the script block
                        def yamlConfig = readYaml file: 'pipeline-config.yml'

                        // Access the dockerImage value from the YAML file
                        def dockerImage = yamlConfig.dockerImage

                        // Print the dockerImage value for demonstration
                        echo "Docker Image: ${dockerImage}"
                    }
                }
            }

            stage('Run Inside Docker Image') {
                steps {
                    script {
                        // Use the Docker image from the YAML configuration
                        agent {
                            docker {
                                image dockerImage
                                args '--user=root'
                            }
                        }

                        // Update package lists and install packages with sudo
                        sh 'sudo apt-get update'
                        sh 'sudo apt-get install -y python3-venv python3-pip'
                        sh 'python3 -m venv venv'
                        sh '. venv/bin/activate'
                        sh 'pip install -r requirements.txt'
                        sh 'pytest --junitxml=pytest-results.xml'
                        sh 'deactivate || true'
                    }
                }
            }
        }
    }
}


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





