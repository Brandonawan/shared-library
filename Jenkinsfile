// @Library('pipeline-library-demo')_
// runMyPipeline()


pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run Inside Ubuntu Docker Image') {
            agent {
                docker {
                    // Pull the Ubuntu image and use privileged mode
                    image 'ubuntu:latest'
                    args '--privileged'
                }
            }
            steps {
                sh 'apt-get update'
                sh 'apt-get install -y python3-venv' // Install Python virtualenv
                sh 'python3 -m venv venv' // Create a virtual environment
                sh 'source venv/bin/activate' // Activate the virtual environment

                // Install dependencies (if you have a requirements.txt file)
                sh 'pip install -r requirements.txt'

                // Run tests (adjust the command accordingly)
                sh 'pytest --junitxml=pytest-results.xml'

                // Deactivate the virtual environment
                sh 'deactivate'
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
