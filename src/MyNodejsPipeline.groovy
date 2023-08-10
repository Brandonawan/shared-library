// pipeline-library-demo/src/MyNodejsPipeline.groovy
class MyNodejsPipeline {
    static void build() {
        steps.sh 'npm install'
    }

    static void test() {
        steps.sh 'npm test'
    }

    static void deploy() {
        steps.sh 'npm start'
    }
}
