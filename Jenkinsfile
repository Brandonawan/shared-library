// @Library('pipeline-library-demo')_
// runMyPipeline()


pipeline {
    agent {
        docker {
            // Pull the Ubuntu image
            image 'ubuntu:latest'
            label 'ubuntu-agent'
        }
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                script {
                    // Set up a virtual environment
                    sh 'python -m venv venv'
                    sh 'source venv/bin/activate'
                    
                    // Install dependencies
                    sh 'pip install -r requirements.txt'
                    
                    // Run tests
                    sh 'pytest --junitxml=pytest-results.xml'
                }
                junit 'pytest-results.xml' // Publish test results
            }
        }
    }
}







































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
