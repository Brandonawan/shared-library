#!/usr/bin/env groovy

def call() {
    pipeline {
        agent any

        stages {
            stage('Read YAML and Run Docker Container') {
                steps {
                    script {
                        // Define yamlData within the script block
                        def yamlData = readYaml file: 'pipeline-config.yml'
                        
                        // Access the Docker image name from the YAML file
                        def dockerImage = yamlData.dockerImage
                        
                        // Print the Docker image name for demonstration purposes
                        echo "Docker Image: ${dockerImage}"
                        
                        // Define the name of the Docker container
                        def containerName = 'my-container'
                        
                        // Check if the container is already running
                        def isContainerRunning = sh(script: "docker ps -q --filter name=${containerName}", returnStatus: true) == 0
                        
                        // If the container is not running, create and start it
                        if (!isContainerRunning) {
                            sh "docker run -d --name ${containerName} ${dockerImage}"
                            // Wait for a few seconds to ensure the container is up (adjust as needed)
                            sleep(10)
                        }
                        
                        // Execute the jenkin-build script inside the container
                        sh "docker exec ${containerName} ./jenkin-build"
                        
                        // Stop and remove the container (optional)
                        // sh "docker stop ${containerName}"
                        // sh "docker rm ${containerName}"
                    }
                }
            }
        }
    }
}


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





