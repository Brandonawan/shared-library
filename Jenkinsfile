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
