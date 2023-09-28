pipeline {
    agent any

    stages {
        stage('Load YAML Config') {
            steps {
                script {
                    def yaml = new org.yaml.snakeyaml.Yaml().load(new File('config.yaml'))
                    env.APP_NAME = yaml.app_name
                    env.ENVIRONMENT = yaml.environment
                    env.BUILD_VERSION = yaml.build_version
                }
            }
        }

        stage('Build and Deploy') {
            steps {
                sh "echo Building ${env.APP_NAME} version ${env.BUILD_VERSION} for ${env.ENVIRONMENT}"
                // Your build and deployment steps here
            }
        }
    }
}
