#!/usr/bin/env groovy

def call() {
    pipeline {
        agent any

        stages {
            stage('Default Stage') {
                steps {
                    echo "This is the default stage."
                }
            }
        }
    }
}
