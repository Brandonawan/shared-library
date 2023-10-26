#!/usr/bin/env groovy

def call() {
    def confluenceDocLink = 'https://your-confluence-link.com/documentation'
    def pipelineConfig // Define pipelineConfig variable in a higher scope

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
            stage('Validate YAML Configuration') {
                steps {
                    script {
                        echo "Checking for files in the workspace"
                        sh 'pwd'
                        sh 'ls -la'
                        dir('.jenkins') {
                            sh 'pwd'
                            sh 'ls -la'

                            // Define the path to the YAML configuration file
                            def yamlConfigPath = 'pipeline-config.yml'

                            // Check if the YAML configuration file exists
                            def yamlConfigExists = fileExists(yamlConfigPath)

                            if (yamlConfigExists) {
                                echo "YAML configuration file found: $yamlConfigPath"

                                echo "Starting 'Validate YAML Configuration' stage"
                                // Read the YAML configuration
                                def pipelineConfigContent = readFile(file: yamlConfigPath)
                                pipelineConfig = readYaml text: pipelineConfigContent // Assign pipelineConfig

                                if (!pipelineConfig) {
                                    error "Error: The YAML configuration is empty or malformed."
                                } else {
                                    def missingKeys = []

                                    if (!pipelineConfig.token) {
                                        missingKeys.add("'token'")
                                    }

                                    if (!pipelineConfig.label) {
                                        missingKeys.add("'label'")
                                    }

                                    if (!pipelineConfig.dockerImage) {
                                        missingKeys.add("'dockerImage'")
                                    }

                                    if (!pipelineConfig.scmCheckoutStrategies) {
                                        missingKeys.add("'scmCheckoutStrategies'")
                                    }

                                    if (missingKeys) {
                                        error "Error: The following keys are missing in the YAML configuration: ${missingKeys.join(', ')}."
                                    } else {
                                        echo "YAML configuration is valid."
                                    }
                                }
                            } else {
                                error "YAML configuration file not found: $yamlConfigPath"
                            }
                        }
                    }
                }
            }
            
            stage('Checkout Source Code') {
                steps {
                    script {
                        echo "Starting 'Checkout Source Code' stage"
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

                                // Define the directory where you want to install 'repo' within .jenkins
                                def repoDirectory = "${WORKSPACE}/.jenkins/repo"

                                // Check if 'repo' tool is installed
                                def repoToolInstalled = sh(script: 'command -v repo', returnStatus: true)

                                if (repoToolInstalled != 0) {
                                    // 'repo' is not installed, so we need to install it
                                    dir('.jenkins') {
                                        script {
                                            // Create the directory if it doesn't exist
                                            if (!fileExists(repoDirectory)) {
                                                sh "mkdir -p $repoDirectory"
                                            }

                                            // Download and install 'repo' in the specified directory
                                            sh "curl https://storage.googleapis.com/git-repo-downloads/repo > $repoDirectory/repo"
                                            sh "chmod a+x $repoDirectory/repo"
                                        }
                                    }

                                    // Add the directory containing 'repo' to the PATH
                                    env.PATH = "${repoDirectory}:${env.PATH}"
                                }

                                withCredentials([string(credentialsId: repoToolStrategy['github-token-jenkins-credential-id'], variable: 'GITHUB_TOKEN')]) {
                                    dir('.jenkins/repo') {
                                        script {
                                            sh "repo init -u ${repoToolStrategy['repo-manifest-url']} -b ${repoToolStrategy['repo-manifest-branch']}"
                                            sh "repo sync"
                                        }
                                    }
                                }
                            } else {
                                echo "No supported checkout strategy found in the configuration. Skipping checkout."
                            }
                        } else {
                            echo "No scmCheckoutStrategies defined in the configuration. Skipping checkout."
                        }
                    }
                }
            }
        }
    }
}
