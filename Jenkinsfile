pipeline {
    agent any
    
    stages {
        stage('Read YAML') {
            steps {
                script{ datas = readYaml (file: 'pipeline-config.yml') },
                echo datas.ear_file.deploy.toString()
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
        
        // Add more stages as needed for your Jenkins pipeline
    }
}
