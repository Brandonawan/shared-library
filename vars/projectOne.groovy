// Define a function to check files and run the pipeline
def call() {

    def jenkinBuildPath = 'scripts/jenkin-build'
    def pipelineConfigPath = 'scripts/pipeline-config.yml'

    pipeline {
        agent {
            label 'component-ci-nodes'
        }
        options {
            ansiColor('xterm')
            timestamps()
        }
        triggers {
            GenericTrigger(
                token: 'MEV_CI_DOCS',
                printContributedVariables: true,
                printPostContent: false,
            )
        }
        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }
            stage('Check Files') {
                steps {
                    script {
                        checkFileExists(jenkinBuildPath)
                        checkFileExists(pipelineConfigPath)

                        // Check if jenkin-build is executable
                        checkIfJenkinBuildIsExecutable(jenkinBuildPath)
                    }
                }
            }

            stage('Read Docker Image Name') {
                steps {
                    script {
                        // Set the default Docker image name
                        def dockerImage = 'ubuntu:latest'

                        // Try to read the Docker image name from the pipeline-config.yml file
                        try {
                            def dockerConfig = readYaml file: pipelineConfigPath
                            if (dockerConfig && dockerConfig.dockerImage) {
                                dockerImage = dockerConfig.dockerImage
                            }
                        } catch (e) {
                            // If the file does not exist or cannot be read, use the default image name
                            logger.warning("Could not read Docker image name from ${pipelineConfigPath}. Using default image name: ${dockerImage}")
                        }

                        // Set the Docker image name as an environment variable
                        env.DOCKER_IMAGE = dockerImage
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
                        sh ''' #!/bin/bash
                        ./${jenkinBuildPath}
                        '''
                    }
                }
            }

        }
    }
}