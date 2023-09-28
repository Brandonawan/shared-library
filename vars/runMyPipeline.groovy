#!/usr/bin/env groovy

def checkFileExists(String filePath) {
    if (!fileExists(filePath)) {
        error "File not found: ${filePath}"
    }
}

def checkFileExecutable(String filePath) {
    if (!fileExecutable(filePath)) {
        error "File is not executable: ${filePath}"
    }
}

def call() {
    pipeline {
        agent any

        stages {
            stage('Check Files') {
                steps {
                    script {
                        // Check if pipeline-config.yml exists
                        checkFileExists 'pipeline-config.yml'
                        
                        // Check if jenkin-build script exists and is executable
                        checkFileExists 'jenkin-build'
                        checkFileExecutable 'jenkin-build'
                    }
                }
            }

            stage('Read YAML') {
                steps {
                    script {
                        // Define yamlData within the script block
                        def yamlData = readYaml file: 'pipeline-config.yml'
                        
                        // Access data from the YAML file
                        def name = yamlData.name
                        def age = yamlData.age
                        def email = yamlData.email
                        
                        // Print the values for demonstration
                        echo "Name: ${name}"
                        echo "Age: ${age}"
                        echo "Email: ${email}"
                    }
                }
            }

            stage('Run jenkin-build Script') {
                steps {
                    script {
                        // Call the external script without passing arguments
                        sh './jenkin-build'
                    }
                }
            }
        }
    }
}
