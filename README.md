# My Jenkins Pipeline Library

My Jenkins Pipeline Library is a collection of reusable Jenkins pipeline scripts for building and testing projects using Docker. This library simplifies the setup process for Jenkins pipelines by providing a set of predefined stages and best practices.

## Table of Contents

- [Usage](#usage)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Usage Example](#usage-example)
- [Pipeline Configuration](#pipeline-configuration)
- [Best Practices](#best-practices)
- [Contributing](#contributing)
- [License](#license)

## Usage

### Prerequisites

Before using this Jenkins Pipeline Library, ensure you have the following prerequisites in your project repository:

1. `pipeline-config.yml`: A YAML configuration file that specifies the Docker image name and other pipeline settings.
2. `jenkin-build`: An executable script used for the delivery stage.

### Installation

To use this Jenkins Pipeline Library in your project, follow these steps:

1. Add the following line to your project's Jenkinsfile to load the library:

   ```groovy
   @Library('my-pipeline-library')_
