// pipeline-library-demo/vars/nodejsPipeline.groovy
def call(body) {
    def myNodejsPipeline = new MyNodejsPipeline()

    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = myNodejsPipeline
    body()
}
