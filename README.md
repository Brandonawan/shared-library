# My Jenkins Pipeline Library

Project One Build Jenkins Pipeline Library is a collection of reusable Jenkins pipeline scripts for building and testing projects using Docker. This library simplifies the setup process for Jenkins pipelines by providing a set of predefined stages and best practices.

## Table of Contents

- [Usage](#usage)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Usage Example](#usage-example)
- [Pipeline Configuration](#pipeline-configuration)


## Usage

### Prerequisites

Before using this Jenkins Pipeline Library, ensure you have the following prerequisites in your project repository inside a folder at the root called scripts:

1. `scripts/pipeline-config.yml`: A YAML configuration file that specifies the Docker image name and other pipeline settings.
2. `scripts/jenkin-build`: An executable script used for the Build stage..

### Installation

To use this Jenkins Pipeline Library in your project, follow these steps:

1. Add the following line to your project's Jenkinsfile to load the library:

   ```groovy
   @Library('my-pipeline-library')_
   projectOne()
    ```

sudo mkdir -p /var/lib/jenkins/bin

sudo chown -R jenkins:jenkins /var/lib/jenkins/bin

