#!/usr/bin/env groovy

// def call() {
//     pipeline {
//         agent any

//         stages {
//             stage('Read YAML') {
//                 steps {
//                     script {
//                         // Define yamlData within the script block
//                         def yamlData = readYaml file: 'pipeline-config.yml'
                        
//                         // Access data from the YAML file
//                         def name = yamlData.name
//                         def age = yamlData.age
//                         def email = yamlData.email
                        
//                         // Print the values for demonstration
//                         echo "Name: ${name}"
//                         echo "Age: ${age}"
//                         echo "Email: ${email}"
//                     }
//                 }
//             }

//             stage('Run jenkin-build Script') {
//                 steps {
//                     script {
//                         // Call the external script without passing arguments
//                         sh './jenkin-build'
//                     }
//                 }
//             }
//         }
//     }
// }


def call() {
    pipeline {
        agent any

        stages {
            stage('Check Files') {
                steps {
                    script {
                        def configFile = 'pipeline-config.yml'
                        def buildScript = 'jenkin-build'
                        
                        def configFileExists = fileExists(configFile)
                        def buildScriptExists = fileExists(buildScript)
                        
                        echo "Checking File: ${configFile}"
                        if (configFileExists) {
                            echo "${configFile} exists."
                        } else {
                            error "${configFile} does not exist."
                        }
                        
                        echo "Checking File: ${buildScript}"
                        if (buildScriptExists) {
                            echo "${buildScript} exists."
                        } else {
                            error "${buildScript} does not exist."
                        }
                    }
                }
            }

            stage('Read YAML') {
                steps {
                    script {
                        def yamlData = readYaml file: configFile
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
                        sh "./${buildScript}"
                    }
                }
            }
        }
    }
}

def fileExists(String fileName) {
    def file = new File("${WORKSPACE}/${fileName}")
    
    if (file.exists()) {
        echo "${fileName} exists at ${file.absolutePath}"
        return true
    } else {
        echo "${fileName} does not exist at ${file.absolutePath}"
        return false
    }
}

