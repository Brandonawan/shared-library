// pipeline-library-demo/vars/nodejsPipeline.groovy
def call(body) {
    // Define the default values for stages
    def stages = [:]

    // Execute the provided closure to modify the stages
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = stages
    body()

    // Define the Node.js pipeline stages
    pipeline {
        agent any
        stages.each { stageName, stageBody ->
            stage(stageName) {
                steps {
                    script {
                        stageBody()
                    }
                }
            }
        }
    }
}
