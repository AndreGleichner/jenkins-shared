#!/usr/bin/groovy

// Use like so in your Jenkinsfile:
// defaultPipeline { }
// or:
// defaultPipeline {
//     buildDaysToKeep = 2
// }
// Depending on whether Jenkins is configured to implicitly import this git repo as a 
// global shared library or not you may explicity import this lib e.g. via '@Library'

// For a very complex shared lib see https://github.com/griddynamics/mpl

// To understand how the below works see:
// http://docs.groovy-lang.org/latest/html/api/groovy/lang/Closure.html and
// https://jenkins.io/blog/2016/04/21/dsl-plugins/
// Basically the passed 'body' parameter is of type Closure.


def call(body) {
    def config = [
        buildNumsToKeep: 0,
        buildDaysToKeep: 0
    ]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

	pipeline {
		agent any
		options {
			timestamps()
			buildDiscarder(logRotator(
				// number of builds to keep
				numToKeepStr:   config.buildNumsToKeep != 0 ? config.buildNumsToKeep : 
								env.BRANCH_NAME == null ? '-1' :
								env.BRANCH_NAME ==~ /master/ ? '100' :
								env.BRANCH_NAME ==~ /(release|b2c|b2b)\/.+/ ? '100' :
								env.BRANCH_NAME ==~ /(feature|bug)\/.+/ ? '5' : '1',
				// number of days to keep builds
				daysToKeepStr:  config.buildDaysToKeep != 0 ? config.buildDaysToKeep : 
								env.BRANCH_NAME == null ? '-1' :
								env.BRANCH_NAME ==~ /master/ ? '30' :
								env.BRANCH_NAME ==~ /(release|b2c|b2b)\/.+/ ? '50' :
								env.BRANCH_NAME ==~ /(feature|bug)\/.+/ ? '5' : '1',
			))
		}
		stages {
			stage('Dump Infos') {
				steps {
					echo """#############################
JOB_NAME    = ${env.JOB_NAME}
BUILD_ID    = ${env.BUILD_ID}
WORKSPACE   = ${env.WORKSPACE}
BRANCH_NAME = ${env.BRANCH_NAME}
GIT_COMMIT  = ${env.GIT_COMMIT}
#############################"""
				}
			}
		}
	}

}