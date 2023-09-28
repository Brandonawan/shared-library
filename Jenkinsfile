pipeline {
    agent any

    // Define yamlData at the top level so it's accessible in all stages
    def yamlData

    stages {
        stage('Read YAML') {
            steps {
                script {
                    // Read YAML data and assign it to the top-level yamlData variable
                    yamlData = readYaml file: 'pipeline-config.yml'

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
                    // Access yamlData here
                    def name = yamlData.name
                    def age = yamlData.age
                    def email = yamlData.email

                    // Pass the values as environment variables to the script
                    sh "./jenkin-build",
                    "-DNAME=${name}",
                    "-DAGE=${age}",
                    "-DEMAIL=${email}"
                }
            }
        }
    }
}
