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