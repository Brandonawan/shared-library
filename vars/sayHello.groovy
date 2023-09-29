#!/usr/bin/env groovy

def call(String name = 'human') {
  echo "Hello, ${name}."
}

pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run Inside Ubuntu Docker Image') {
            steps {
                script {
                    def pipelineConfig = readYaml file: 'pipeline-config.yml'

                    // Ensure the dockerImage key exists in the YAML file
                    def dockerImage = pipelineConfig.dockerImage

                    if (dockerImage) {
                        // Use the Docker image name from the YAML file
                        docker.image(dockerImage).withRun('-u root') {
                            sh 'apt-get update'
                            sh 'apt-get install -y python3-venv python3-pip' // Install Python virtualenv and pip
                            sh 'python3 -m venv venv' // Create a virtual environment
                            sh 'source venv/bin/activate' // Activate the virtual environment

                            // Install dependencies (if you have a requirements.txt file)
                            sh 'pip install -r requirements.txt'

                            // Run tests (adjust the command accordingly)
                            sh 'pytest --junitxml=pytest-results.xml'
                        }
                    } else {
                        error('Docker image not specified in pipeline-config.yml')
                    }
                }
            }
        }
    }

    post {
        always {
            // Deactivate the virtual environment using the deactivate function
            sh 'deactivate || true' // Use '|| true' to ignore errors if deactivate fails
        }
    }
}
