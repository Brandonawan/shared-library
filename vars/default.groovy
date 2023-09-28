#!/usr/bin/env groovy

// vars/default.groovy

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
