pipeline {
    agent any

    stages {
        stage('Load YAML Config') {
            steps {
                script {
                    def configFile = new File('config.yaml')
                    if (configFile.exists()) {
                        def yaml = readYaml file: configFile
                        env.APP_NAME = yaml.app_name
                        env.ENVIRONMENT = yaml.environment
                        env.BUILD_VERSION = yaml.build_version
                    } else {
                        error "Config file 'config.yaml' not found!"
                    }
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
