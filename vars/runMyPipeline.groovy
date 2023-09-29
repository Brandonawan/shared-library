#!/usr/bin/env groovy

def call() {
    pipeline {
        agent any

        stages {
            stage('Read YAML') {
                steps {
                    script {
                        // Define yamlData within the script block
                        def yamlData = readYaml file: 'pipeline-config.yml'
                        
                        // Access the Docker image name from the YAML file
                        def dockerImage = yamlData.dockerImage
                        
                        // Print the Docker image name for demonstration
                        echo "Docker Image: ${dockerImage}"
                        
                        // Run a Docker container using the specified image
                        def containerName = 'my-container'
                        sh "docker run -d --name ${containerName} ${dockerImage}"
                        
                        // Execute the jenkin-build script inside the container
                        sh "docker exec ${containerName} ./jenkin-build"
                        
                        // Stop and remove the container
                        sh "docker stop ${containerName}"
                        sh "docker rm ${containerName}"
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





