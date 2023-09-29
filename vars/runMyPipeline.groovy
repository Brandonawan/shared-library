#!/usr/bin/env groovy

def call() {
    pipeline {
        agent any

        stages {
            stage('Read YAML and Run Docker Container') {
                steps {
                    script {
                        // Define the name of the Docker container
                        def containerName = 'my-container'
                        
                        try {
                            // Define yamlData within the script block
                            def yamlData = readYaml file: 'pipeline-config.yml'
                            
                            // Access the Docker image name from the YAML file
                            def dockerImage = yamlData.dockerImage
                            
                            // Print the Docker image name for demonstration purposes
                            echo "Docker Image: ${dockerImage}"
                            
                            // Check if the container is already running
                            def isContainerRunning = sh(script: "docker ps --filter name=${containerName} --format '{{.Names}}'", returnStatus: true) == 0
                            
                            // If the container is not running, start it
                            if (!isContainerRunning) {
                                sh "docker run -d --name ${containerName} ${dockerImage}"
                                
                                // Wait for the container to be up and running (adjust as needed)
                                sh "docker wait ${containerName}"
                            }
                            
                            // Execute the jenkin-build script inside the container
                            sh "docker exec ${containerName} ./jenkin-build"
                        } catch (Exception e) {
                            currentBuild.result = 'FAILURE'
                            error("An error occurred: ${e.message}")
                        } finally {
                            // Optionally, stop and remove the container
                            sh "docker stop ${containerName}"
                            sh "docker rm ${containerName}"
                        }
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





