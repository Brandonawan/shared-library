#!/usr/bin/env groovy
def call() {
    def confluenceDocLink = 'https://your-confluence-link.com/documentation'
    pipeline {
        agent any
        options {
            ansiColor('xterm')
            timestamps()
        }
        triggers {
            GenericTrigger(
                token: "my-dummy-token",
                printContributedVariables: true,
                printPostContent: false,
            )
        }
        stages {
            stage('Clean Jenkins Workspace') {
                steps {
                    echo "Starting 'Clean Workspace' stage"
                    cleanWs()
                    sh 'ls -la'  // Use the 'sh' step to execute 'ls' shell command
                    echo "${env.JOB_NAME} #${env.BUILD_NUMBER} completed successfully"
                    echo "View Documentation: ${confluenceDocLink}"
                }
            }
            stage('Checkout') {
                steps {
                    script {
                        echo "Starting 'Checkout' stage"
                        
                        // Define the path to the YAML configuration file
                        def yamlConfigPath = '.jenkins/pipeline-config.yml'
                        
                        // Check if the YAML configuration file exists
                        def yamlConfigExists = fileExists(yamlConfigPath)
                        
                        if (yamlConfigExists) {
                            // Read the YAML configuration
                            def pipelineConfigContent = readFile(file: yamlConfigPath)
                            def pipelineConfig = readYaml text: pipelineConfigContent
                        
                            // Check the YAML configuration for the checkout strategy
                            if (pipelineConfig.scmCheckoutStrategies) {
                                def defaultStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'default' }
                                def customStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'custom-checkout' }
                                def repoToolStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'repo-tool-with-gh-token' }
                                
                                if (defaultStrategy) {
                                    echo "Checking out Source Code using 'SCM default' strategy."
                                    checkout scm
                                } else if (customStrategy) {
                                    echo "Checking out Source Code using 'SCM custom-checkout' strategy."
                                    sh "./${customStrategy['checkout-script-name']}"
                                } else if (repoToolStrategy) {
                                    echo "Checking out Source Code using 'repo-tool-with-gh-token' strategy."
                                    
                                    // Implement repo-tool checkout logic here
                                    // ...
                                } else {
                                    echo "No supported checkout strategy found in the configuration. Skipping checkout."
                                }
                            } else {
                                echo "No scmCheckoutStrategies defined in the configuration. Skipping checkout."
                            }
                        } else {
                            error "YAML configuration file not found: $yamlConfigPath"
                        }
                    }
                }
            }
        }
    }
}


// def call() {
//     def confluenceDocLink = 'https://your-confluence-link.com/documentation'
//     pipeline {
//         agent any
//         options {
//             ansiColor('xterm')
//             timestamps()
//         }
        // triggers {
        //     GenericTrigger(
        //         token: "my-dummy-token",
        //         printContributedVariables: true,
        //         printPostContent: false,
        //     )
        // }
//         stages {
            // stage('Clean Workspace') {
            //     steps {
            //         echo "Starting 'Clean Workspace' stage"
            //         cleanWs()
            //         echo "${env.JOB_NAME} #${env.BUILD_NUMBER} completed successfully"
            //         echo "View Documentation: ${confluenceDocLink}"
            //     }
            // }
//             stage('Checkout') {
//                 steps {
//                     // Perform the checkout here, e.g., using Git
//                     checkout scm
//                 }
//             }
//             stage('Check Files') {
//                 steps {
//                     script {
//                         def pipelineConfigPath = '.jenkins/pipeline-config.yml'

//                         def pipelineConfigExists = fileExists(pipelineConfigPath)
//                         if (pipelineConfigExists) {
//                             echo "pipeline-config.yml file found: $pipelineConfigPath"
//                         } else {
//                             error "pipeline-config.yml file not found: $pipelineConfigPath"
//                         }
//                     }
//                 }
//             }
//         }
//     }
// }




// def call() {
//     def jenkinsBuildPath = '.jenkins/jenkin-build'
//     def pipelineConfigPath = '.jenkins/pipeline-config.yml'
//     def confluenceDocLink = 'https://your-confluence-link.com/documentation'

//     pipeline {
//         agent any
//         options {
//             ansiColor('xterm')
//             timestamps()
//         }
//         stages {
//             stage('Clean Workspace') {
//                 steps {
//                     echo "Starting 'Clean Workspace' stage"
//                     cleanWs()
//                     echo "${env.JOB_NAME} #${env.BUILD_NUMBER} completed successfully"
//                     echo "View Documentation: ${confluenceDocLink}"
//                 }
//             }
//             stage('Check Files') {
//                 steps {
//                     script {
//                         echo "Starting 'Check Files' stage"
//                         pwd()
//                         if (jenkinsBuildPath.isEmpty()) {
//                             error "Error: No build script is found. Please specify a valid file path. Refer to the documentation for guidance: [${confluenceDocLink}]"
//                         }

//                         checkFileExistsInternal(jenkinsBuildPath)
//                         checkFileExists(pipelineConfigPath)

//                         // Check if jenkins-build is executable
//                         checkIfJenkinsBuildIsExecutable(jenkinsBuildPath)
//                     }
//                 }
//             }

            // stage('Checkout') {
            //     steps {
            //         script {
            //             echo "Starting 'Checkout' stage"
            //             def pipelineConfigContent = readFile(file: pipelineConfigPath)
            //             def pipelineConfig = readYaml text: pipelineConfigContent

            //             echo "Starting 'Validate YAML Configuration' stage"

            //             def errors = []  // Create an array to collect errors

            //             try {
            //                 if (!pipelineConfig.token) {
            //                     errors.add("Error: 'token' key is missing or misconfigured in the YAML configuration.")
            //                 }

            //                 if (!pipelineConfig.label) {
            //                     errors.add("Error: 'label' key is missing or misconfigured in the YAML configuration.")
            //                 }

            //                 if (!pipelineConfig.dockerImage) {
            //                     errors.add("Error: 'dockerImage' key is missing or misconfigured in the YAML configuration.")
            //                 }

            //                 if (!pipelineConfig.scmCheckoutStrategies) {
            //                     errors.add("Error: 'scmCheckoutStrategies' key is missing or misconfigured in the YAML configuration.")
            //                 }

            //                 if (errors) {
            //                     // If there are errors, log each one
            //                     for (error in errors) {
            //                         error(error)
            //                     }
            //                 } else {
            //                     echo "YAML configuration is valid."
            //                 }

            //                 if (pipelineConfig.scmCheckoutStrategies) {
            //                     def defaultStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'default' }
            //                     def customStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'custom-checkout' }
            //                     def repoToolStrategy = pipelineConfig.scmCheckoutStrategies.find { it['strategy-name'] == 'repo-tool-with-gh-token' }

            //                     if (defaultStrategy) {
            //                         echo "Checking out Source Code using 'SCM default' strategy."
            //                         checkout scm
            //                     } else if (customStrategy) {
            //                         echo "Checking out Source Code using 'SCM custom-checkout' strategy."
            //                         sh "./${customStrategy['checkout-script-name']}"
            //                     } else if (repoToolStrategy) {
            //                         echo "Checking out Source Code using 'repo-tool-with-gh-token' strategy."

            //                         // Check if 'repo' tool is installed
            //                         def repoToolInstalled = sh(script: "which repo", returnStatus: true)
            //                         if (repoToolInstalled != 0) {
            //                             error "Error: The 'repo' tool is not installed. Please install 'repo' tool or choose a different checkout strategy. Refer to the documentation for guidance: [${confluenceDocLink}]"
            //                         }

            //                         // Fetch the manifest repository
            //                         dir('repo') {
            //                             script {
            //                                 // Add the directory containing 'repo' to the PATH
            //                                 def repoDir = '/var/lib/jenkins/bin'  // Adjust to the actual path where 'repo' is located
            //                                 env.PATH = "${repoDir}:${env.PATH}"
            //                             }
            //                             withCredentials([string(credentialsId: repoToolStrategy['github-token-jenkins-credential-id'], variable: 'GITHUB_TOKEN')]) {
            //                                 sh "repo init -u ${repoToolStrategy['repo-manifest-url']} -b ${repoToolStrategy['repo-manifest-branch']} -g ${repoToolStrategy['repo-manifest-group']}"
            //                                 sh "repo sync"
            //                             }
            //                         }
            //                         // Checkout the specified manifest group (uncomment if needed)
            //                         // sh "repo forall -c 'git checkout ${repoToolStrategy['repo-manifest-branch']}' -g ${repoToolStrategy['repo-manifest-group']}"
            //                     } else {
            //                         echo "No supported checkout strategy found in the configuration. Skipping checkout."
            //                     }
            //                 } else {
            //                     echo "No scmCheckoutStrategies defined in the configuration. Skipping checkout."
            //                 }
            //             } catch (e) {
            //                 error "Error: Failed to validate the YAML configuration or checkout source code. Please check the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]"
            //             }
            //         }
            //     }
            // }

//             stage('Read Docker Image Name') {
//                 steps {
//                     script {
//                         echo "Starting 'Read Docker Image Name' stage"
//                         try {
//                             def dockerConfig = readYaml file: pipelineConfigPath
//                             if (dockerConfig && dockerConfig.dockerImage) {
//                                 env.DOCKER_IMAGE = dockerConfig.dockerImage
//                             } else {
//                                 error "Error: Docker image name not found in ${pipelineConfigPath}. Please specify a Docker image name in the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]"
//                             }
//                         } catch (e) {
//                             error "Error: Could not read Docker image name from ${pipelineConfigPath}. Please check the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]"
//                         }
//                     }
//                 }
//             }

//             stage('Run Inside Docker Image') {
//                 agent {
//                     docker {
//                         image "${env.DOCKER_IMAGE}"
//                         args '--user=root -v /mnt:/mnt'
//                         reuseNode(true)
//                     }
//                 }
//                 steps {
//                     echo "Starting 'Run Inside Docker Image' stage"
//                     sh 'apt-get update'
//                     sh 'apt-get install -y python3-venv python3-pip'
//                     sh 'python3 -m venv venv'
//                     sh '. venv/bin/activate'

//                     sh 'pip install -r requirements.txt'

//                     sh "./${jenkinsBuildPath}"

//                     sh 'deactivate || true'
//                 }
//             }
//         }
//     }
// }

// def checkFileExistsInternal(fileName) {
//     echo "Checking if file '${fileName}' exists."
//     def fileExists = fileExists(fileName)
//     if (!fileExists) {
//         error "Error: File '${fileName}' not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]"
//     }
// }

// def checkFileExists(fileName) {
//     echo "Checking if file '${fileName}' exists."
//     checkFileExistsInternal(fileName)
// }

// def checkIfJenkinsBuildIsExecutable(fileName) {
//     echo "Checking if '${fileName}' is executable."
//     if (!fileExists(fileName)) {
//         error "Error: The '${fileName}' file is not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]"
//     }

//     def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
//     if (isExecutable != 0) {
//         error "Error: The '${fileName}' file is not executable. Refer to the documentation for guidance: [${confluenceDocLink}]"
//     }
// }
