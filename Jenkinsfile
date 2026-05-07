pipeline {
    agent {label 'agent1'}
    tools {
    	jdk 'java21'
		maven 'maven3.9.15'
    }
    stages{
    	stage('Build Package'){
		    steps {
	    	    sh 'mvn clean package'
	    	}
		}
		stage('Deploy Package'){
			steps{
				sh 'mvn deploy'
			}
		}
    }
}
