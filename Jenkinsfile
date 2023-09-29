@Library('pipeline-library-demo')_
runMyPipeline()


// pipeline {
//     agent any
    
//     stages {
//         stage('Build and Test') {
//             steps {
//                 script {
//                     def containerName = "my-container-${new Date().getTime()}"
                    
//                     // Pull the desired Docker image (e.g., Ubuntu)
//                     sh 'docker pull ubuntu:latest'

//                     // Create and run a Docker container with a unique name
//                     sh "docker run -d --name ${containerName} ubuntu:latest"
                    
//                     // Execute commands inside the Docker container
//                     sh "docker exec ${containerName} mvn --version"
//                     sh "docker exec ${containerName} mvn clean install"
                    
//                     // Stop and remove the Docker container
//                     sh "docker stop ${containerName}"
//                     sh "docker rm ${containerName}"
//                 }
//             }
//         }
//     }
// }






































// stage('Demo') {

//   echo 'Hello World'
//   runPipeline()
//   sayHello 'Dave'

// }


// pipeline {
//     agent any

//     stages {
//         stage('Read YAML') {
//             steps {
//                 script {
//                     // Define yamlData within the script block
//                     def yamlData = readYaml file: 'pipeline-config.yml'
                    
//                     // Access data from the YAML file
//                     def name = yamlData.name
//                     def age = yamlData.age
//                     def email = yamlData.email
                    
//                     // Print the values for demonstration
//                     echo "Name: ${name}"
//                     echo "Age: ${age}"
//                     echo "Email: ${email}"
//                 }
//             }
//         }

//         stage('Run jenkin-build Script') {
//             steps {
//                 script {
//                     // Call the external script without passing arguments
//                     sh './jenkin-build'
//                 }
//             }
//         }
//     }
// }
