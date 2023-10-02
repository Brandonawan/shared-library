#!/usr/bin/env groovy

def call() {
    pipeline {
        agent any
        stages {
            stage('Validation and Setup') {
                steps {
                    script {
                        // Validate pipeline-config.yml
                        def dockerConfig = readYaml file: 'pipeline-config.yml'
                        validateDockerConfig(dockerConfig)

                        // Check if jenkin-build script exists and is executable
                        validateExecutableScript('jenkin-build')
                        
                        // Set default values if not provided
                        dockerConfig.dockerImage = dockerConfig.dockerImage ?: 'maven:3-alpine'
                    }
                }
            }

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Run Inside Docker Image') {
                agent {
                    docker {
                        image "${dockerConfig.dockerImage}"
                        args '--user=root'
                    }
                }
                steps {
                    sh 'apt-get update'
                    sh 'apt-get install -y python3-venv python3-pip' // Install Python virtualenv and pip
                    sh 'python3 -m venv venv' // Create a virtual environment
                    sh '. venv/bin/activate' // Activate the virtual environment using dot command

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

def validateDockerConfig(config) {
    if (!config) {
        error("pipeline-config.yml is missing or empty.")
    }
    
    if (!config.dockerImage) {
        error("Docker image name is missing in pipeline-config.yml.")
    }
    
    // Add more validation checks as needed
}

def validateExecutableScript(scriptName) {
    if (!fileExists(scriptName)) {
        error("${scriptName} script is missing in the repository.")
    }
    
    if (!isScriptExecutable(scriptName)) {
        error("${scriptName} script is not executable. Make sure to run 'chmod +x ${scriptName}' before committing.")
    }
}

def fileExists(fileName) {
    return new File(fileName).exists()
}

def isScriptExecutable(scriptName) {
    return new File(scriptName).canExecute()
}

