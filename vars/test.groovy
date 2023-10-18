#!/usr/bin/env groovy

def call() {

    def jenkinsBuildPath = 'jenkins/jenkin-build'
    def pipelineConfigPath = 'jenkins/pipeline-config.yml'
    def confluenceDocLink = 'https://your-confluence-link.com/documentation'

    pipeline {
        agent any
        options {
            ansiColor('xterm')
            timestamps()
        }
        stages {
            // stage('Checkout') {
            //     steps {
            //         checkout scm
            //     }
            stage('Checkout') {
                steps {
                    script {
                        def pipelineConfig = readYaml file: pipelineConfigPath
                        def checkoutStrategy = pipelineConfig.scmCheckoutStrategy

                        if (checkoutStrategy) {
                            checkoutStrategy.each { strategy ->
                                switch (strategy.strategyName) {
                                    case 'default':
                                        checkout scm
                                        break
                                    case 'clean-before-checkout':
                                        deleteDir()
                                        checkout scm
                                        break
                                    case 'revert-before-update':
                                        dir(strategy.svnDirectory) {
                                            sh "svn revert -R ."
                                            checkout([$class: 'SubversionSCM', locations: [[remote: strategy.svnRepoUrl]]])
                                        }
                                        break
                                    case 'wipe-out-workspace':
                                        deleteDir()
                                        checkout scm
                                        break
                                    case 'sparse-checkout':
                                        checkout([$class: 'GitSCM', branches: [[name: strategy.gitBranch]],
                                                  userRemoteConfigs: [[url: strategy.gitRepoUrl]],
                                                  extensions: [[$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: strategy.gitSparsePath]]]])
                                        break
                                    case 'lightweight-checkout':
                                        checkout([$class: 'GitSCM', branches: [[name: strategy.gitBranch]],
                                                  userRemoteConfigs: [[url: strategy.gitRepoUrl]],
                                                  extensions: [[$class: 'CleanCheckout'], [$class: 'CloneOption', depth: 1]])
                                        break
                                    case 'custom-checkout':
                                        sh "sh ${strategy.checkoutScriptName}"
                                        break
                                    default:
                                        error "Invalid checkout strategy: ${strategy.strategyName}"
                                }
                            }
                        } else {
                            error "No scmCheckoutStrategy defined in the configuration."
                        }
                    }
                }
            }

            stage('Check Files') {
                steps {
                    script {
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

            stage('Run Inside Docker Image') {
                agent {
                    docker {
                        image "${env.DOCKER_IMAGE}"
                        args '--user=root -v /mnt:/mnt'
                        reuseNode(true)
                    }
                }
                steps {
                    sh 'apt-get update'
                    sh 'apt-get install -y python3-venv python3-pip'
                    sh 'python3 -m venv venv'
                    sh '. venv/bin/activate'

                    sh 'pip install -r requirements.txt'

                    sh "./${jenkinsBuildPath}"

                    sh 'deactivate || true'
                }
            }

            stage('Clean Workspace') {
                steps {
                    cleanWs()
                    echo "${env.JOB_NAME} #${env.BUILD_NUMBER} completed successfully"
                    echo "View Documentation: ${confluenceDocLink}"
                }
            }
        }
    }
}

def checkFileExistsInternal(fileName) {
    def fileExists = fileExists(fileName)
    if (!fileExists) {
        error "Error: File '${fileName}' not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]"
    }
}

def checkFileExists(fileName) {
    checkFileExistsInternal(fileName)
}

def checkIfJenkinsBuildIsExecutable(fileName) {
    if (!fileExists(fileName)) {
        error "Error: The '${fileName}' file is not found in the repository. Refer to the documentation for guidance: [${confluenceDocLink}]"
    }

    def isExecutable = sh(script: "test -x ${fileName}", returnStatus: true)
    if (isExecutable != 0) {
        error "Error: The '${fileName}' file is not executable. Refer to the documentation for guidance: [${confluenceDocLink}]"
    }
}
