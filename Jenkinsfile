@Library('pipeline-library-demo')_
runMyPipeline()

// Define a function to check files and run the pipeline
// def call() {
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

//             // stage('Create Non-Executable File') {
//             //     steps {
//             //         script {
//             //             writeFile file: 'jenkin-build', text: 'This is a non-executable file content'
//             //         }
//             //     }
//             // }

//             stage('Check Files') {
//                 steps {
//                     script {
//                         checkFileExists('jenkin-build')
//                         checkFileExists('pipeline-config.yml')

//                         // Check if jenkin-build is executable
//                         checkIfJenkinBuildIsExecutable()
//                     }
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
//                         registryUrl 'https://artifact-bxdsw.sc.intel.com:9444'
//                         registryCredentialsId 'nexus-docker-creds'
//                         args '--user=root -v /mnt:/mnt'
//                         alwaysPull true
//                     }
//                 }
//                 steps {
//                     withCredentials([usernamePassword(credentialsId: 'nexus-docker-creds', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
//                         sh 'docker login -u ${USERNAME} -p ${PASSWORD} https://artifact-bxdsw.sc.intel.com:9444'
//                     }
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

// // Define a function to check if a file exists
// def checkFileExists(fileName) {
//     def fileExists = fileExists(fileName)
//     if (!fileExists) {
//         error "File '${fileName}' not found in the repository."
//     }
// }

// // Define a function to check if jenkin-build is executable
// def checkIfJenkinBuildIsExecutable() {
//     script {
//         if (!fileExists('jenkin-build')) {
//             error "The 'jenkin-build' file is not found in the repository."
//         }

//         def isExecutable = sh(script: "test -x jenkin-build", returnStatus: true)
//         if (isExecutable != 0) {
//             error "The 'jenkin-build' file is not executable."
//         }
//     }
// }


// pipeline {
//     agent any

//     environment {
//         // Define the repository URL and the branch you want to check
//         REPO_URL = 'https://github.com/Brandonawan/shared-library.git'
//         BRANCH_NAME = 'main'
//         FILE_TO_CHECK = 'jenkin-buildss' // Specify the path to the file you want to check
//     }

//     stages {
//         stage('Checkout') {
//             steps {
//                 script {
//                     // Checkout the repository
//                     checkout([$class: 'GitSCM', branches: [[name: BRANCH_NAME]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'your-git-credentials-id', url: REPO_URL]]])
//                 }
//             }
//         }
//         stage('Check Workspace Contents') {
//             steps {
//                 sh 'ls -l' // List the contents of the workspace
//             }
//         }

//         stage('Check File') {
//             steps {
//                 script {
//                     // Check if the specified file exists
//                     def fileExists = fileExists(FILE_TO_CHECK)

//                     if (fileExists) {
//                         echo "File '$FILE_TO_CHECK' exists in the repository."
//                     } else {
//                         error "File '$FILE_TO_CHECK' does not exist in the repository."
//                     }
//                 }
//             }
//         }
//     }
// }

// pipeline {
//     agent {
//         docker {
//             // Pull the Ubuntu image and run as root user
//             image 'ubuntu:latest'
//             args '--user=root'
//         }
//     }

//     stages {
//         stage('Checkout') {
//             steps {
//                 checkout scm
//             }
//         }

//         stage('Run Inside Ubuntu Docker Image') {
//             steps {
//                 sh 'apt-get update'
//                 sh 'apt-get install -y python3-venv python3-pip' // Install Python virtualenv and pip
//                 sh 'python3 -m venv venv' // Create a virtual environment
//                 sh '. venv/bin/activate' // Activate the virtual environment using dot command

//                 // Install dependencies (if you have a requirements.txt file)
//                 sh 'pip install -r requirements.txt'

//                 // Run tests (adjust the command accordingly)
//                 sh 'pytest --junitxml=pytest-results.xml'

//                 // Deactivate the virtual environment using the deactivate function
//                 sh 'deactivate || true' // Use '|| true' to ignore errors if deactivate fails
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
