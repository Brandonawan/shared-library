#!/usr/bin/env groovy

// Define a function to check files and run the pipeline
def call() {
    def jenkinBuildPath = 'jenkins/jenkins-build'
    def pipelineConfigPath = 'jenkins/pipeline-config.yml'
    def confluenceDocLink = 'https://your-confluence-link.com/documentation'

    def pipelineConfig = readYaml file: pipelineConfigPath
    def label = pipelineConfig.label
    def token = pipelineConfig.token

    pipeline {
        agent {
            label label
        }
        options {
            ansiColor('xterm')
            timestamps()
        }
        triggers {
            GenericTrigger(
                token: token,
                printContributedVariables: true,
                printPostContent: false,
            )
        }
        stages {
            stage('Check Files') {
                steps {
                    script {
                        echo "Starting 'Check Files' stage"
                        if (jenkinsBuildPath.isEmpty()) {
                            error "Error: No build script is found. Please specify a valid file path. Refer to the documentation for guidance: [${confluenceDocLink}]"
                        }

                        checkFileExistsInternal(jenkinsBuildPath)
                        checkFileExists(pipelineConfigPath)

                        // Check if jenkins-build is executable
                        checkIfJenkinsBuildIsExecutable(jenkinsBuildPath)
                    }
                }
            }
            
            stage('Checkout') {
                steps {
                    script {
                        echo "Starting 'Checkout' stage"
                        def pipelineConfigContent = readFile(file: pipelineConfigPath)
                        def pipelineConfig = readYaml text: pipelineConfigContent

                        echo "Starting 'Validate YAML Configuration' stage"

                        def errors = []  // Create an array to collect errors

                        try {
                            if (!pipelineConfig.token) {
                                errors.add("Error: 'token' key is missing or misconfigured in the YAML configuration.")
                            }

                            if (!pipelineConfig.label) {
                                errors.add("Error: 'label' key is missing or misconfigured in the YAML configuration.")
                            }

                            if (!pipelineConfig.dockerImage) {
                                errors.add("Error: 'dockerImage' key is missing or misconfigured in the YAML configuration.")
                            }

                            if (!pipelineConfig.scmCheckoutStrategies) {
                                errors.add("Error: 'scmCheckoutStrategies' key is missing or misconfigured in the YAML configuration.")
                            }

                            if (errors) {
                                // If there are errors, log each one
                                for (error in errors) {
                                    error(error)
                                }
                            } else {
                                echo "YAML configuration is valid."
                            }

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

                                    // Install Repo tool if not already installed
                                    sh "if [ ! -f \"\$(which repo)\" ]; then curl https://storage.googleapis.com/git-repo-downloads/repo > /var/lib/jenkins/bin/repo; chmod +x /var/lib/jenkins/bin/repo; echo 'Repo tool installation completed.'; fi"

                                    // Fetch the manifest repository
                                    dir('repo') {
                                        script {
                                            // Add the directory containing 'repo' to the PATH
                                            def repoDir = '/var/lib/jenkins/bin'  // Adjust to the actual path where 'repo' is located
                                            env.PATH = "${repoDir}:${env.PATH}"
                                        }
                                        withCredentials([string(credentialsId: repoToolStrategy['github-token-jenkins-credential-id'], variable: 'GITHUB_TOKEN')]) {
                                            sh "repo init -u ${repoToolStrategy['repo-manifest-url']} -b ${repoToolStrategy['repo-manifest-branch']}"
                                            sh "repo sync"
                                        }
                                    }
                                    // Checkout the specified manifest group (uncomment if needed)
                                    sh "repo forall -c 'git checkout ${repoToolStrategy['repo-manifest-branch']}' -g ${repoToolStrategy['repo-manifest-group']}"
                                } else {
                                    echo "No supported checkout strategy found in the configuration. Skipping checkout."
                                }
                            } else {
                                echo "No scmCheckoutStrategies defined in the configuration. Skipping checkout."
                            }
                        } catch (e) {
                            error "Error: Failed to validate the YAML configuration or checkout source code. Please check the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]"
                        }
                    }
                }
            }
            stage('Check Files') {
                steps {
                    script {
                        echo "Starting 'Check Files' stage"
                        checkFileExists(jenkinBuildPath)
                        checkFileExists(pipelineConfigPath)
                        checkIfJenkinsBuildIsExecutable(jenkinBuildPath)
                    }
                }
            }
            stage('Read Docker Image Name') {
                steps {
                    script {
                        echo "Starting 'Read Docker Image Name' stage"
                        try {
                            def dockerConfig = readYaml file: pipelineConfigPath
                            if (dockerConfig && dockerConfig.dockerImage) {
                                env.DOCKER_IMAGE = dockerConfig.dockerImage
                            } else {
                                error "Error: Docker image name not found in ${pipelineConfigPath}. Please specify a Docker image name in the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]"
                            }
                        } catch (e) {
                            error "Error: Could not read Docker image name from ${pipelineConfigPath}. Please check the configuration. Refer to the documentation for guidance: [${confluenceDocLink}]"
                        }
                    }
                }
            }

            stage('Build') {
                agent {
                    docker {
                        image "${env.DOCKER_IMAGE}"
                        registryUrl 'https://artifact-bxdsw.sc.intel.com:9444'
                        registryCredentialsId 'nexus-docker-creds'
                        args '--user=root -v /mnt:/mnt'
                        alwaysPull true
                    }
                }
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-docker-creds', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                        sh "./${jenkinBuildPath}"
                    }
                    echo "${env.JOB_NAME} #${env.BUILD_NUMBER} completed successfully"
                    echo "View Documentation: ${confluenceDocLink}"
                }
            }

        }
    }
}

def checkFileExists(fileName) {
    if (!fileExists(fileName)) {
        error "File '${fileName}' not found in the repository."
    }
}

def checkIfJenkinsBuildIsExecutable(fileName) {
    if (!fileExists(fileName)) {
        error "The '${fileName}' file is not found in the repository."
    }

    def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
    if (isExecutable != 0) {
        error "The '${fileName}' file is not executable."
    }
}
