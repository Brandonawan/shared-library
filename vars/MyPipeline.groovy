def runPipeline() {
    pipeline {
        agent any

        stages {
            stage('Read YAML') {
                steps {
                    script {
                        def yamlData = readYaml file: '../pipeline-config.yml'

                        def name = yamlData.name
                        def age = yamlData.age
                        def email = yamlData.email

                        echo "Name: ${name}"
                        echo "Age: ${age}"
                        echo "Email: ${email}"
                    }
                }
            }

            stage('Run jenkin-build Script') {
                steps {
                    script {
                        sh '../jenkin-build'
                    }
                }
            }
        }
    }
}