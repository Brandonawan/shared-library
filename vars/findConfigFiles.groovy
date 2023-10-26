#!/usr/bin/env groovy
def call() {
    // Define the paths to the required files
    def jenkinsBuildPath = '.jenkins/jenkin-build'
    def pipelineConfigPath = '.jenkins/pipeline-config.yml'

    // Check if the files exist in the workspace
    def jenkinsBuildExists = fileExists(jenkinsBuildPath)
    def pipelineConfigExists = fileExists(pipelineConfigPath)

    // Log the results
    if (jenkinsBuildExists) {
        echo "jenkin-build file found: $jenkinsBuildPath"
    } else {
        error "jenkin-build file not found: $jenkinsBuildPath"
    }

    if (pipelineConfigExists) {
        echo "pipeline-config.yml file found: $pipelineConfigPath"
    } else {
        error "pipeline-config.yml file not found: $pipelineConfigPath"
    }
}
