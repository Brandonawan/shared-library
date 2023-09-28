pipeline {
    agent any
    
    stages {
        stage('Read YAML') {
            steps {
                script {
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
                // Assuming 'jenkin-build' is in the same directory as the Jenkinsfile
                sh './jenkin-build "${name}" "${age}" "${email}"'
            }
        }
        
    }
}
