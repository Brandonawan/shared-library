#!/usr/bin/env groovy

// Define a function to check files and run the pipeline
def call() {
    
    def jenkinBuildPath = 'jenkins/jenkins-build'
    def pipelineConfigPath = 'jenkins/pipeline-config.yml'

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
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }
            stage('Check Files') {
                steps {
                    script {
                        if (jenkinBuildPath.isEmpty()) {
                            error "No build script is found. Please specify a valid file path."
                        }

                        checkFileExistsInternal(jenkinBuildPath)
                        checkFileExists(pipelineConfigPath)

                        // Check if jenkin-build is executable
                        checkIfJenkinBuildIsExecutable(jenkinBuildPath)
                    }
                }
            }
            stage('Read Docker Image Name') {
                steps {
                    script {
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
                }
            }

        }
    }
}

def checkFileExistsInternal(fileName) {
    def fileExists = fileExists(fileName)
    if (!fileExists) {
        error "File '${fileName}' not found in the repository."
    }
}

def checkFileExists(fileName) {
    checkFileExistsInternal(fileName)
}

def checkIfJenkinBuildIsExecutable(fileName) {
    if (!fileExists(fileName)) {
        error "The '${fileName}' file is not found in the repository."
    }

    def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
    if (isExecutable != 0) {
        error "The '${fileName}' file is not executable."
    }
}