// pipeline-library-demo/src/MyNodejsPipeline.groovy
class MyNodejsPipeline {
    static void build() {
        sh 'npm install'
    }

    static void test() {
        sh 'npm test'
    }

    static void deploy() {
        sh 'npm start'
    }
}
