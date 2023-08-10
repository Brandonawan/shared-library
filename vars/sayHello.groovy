def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    pipeline {
        agent any
        stages {
            stage('Build') {
                steps {
                    script {
                        config.nodejsBuild()
                    }
                }
            }
            stage('Test') {
                steps {
                    script {
                        config.nodejsTest()
                    }
                }
            }
            stage('Deploy') {
                steps {
                    script {
                        config.nodejsDeploy()
                    }
                }
            }
        }
    }
}

