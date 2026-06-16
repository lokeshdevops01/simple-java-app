pipeline {
    agent any
	
    tools {
    	jdk 'java21'
		maven 'maven3.9.16'
    }
    stages{
    	stage('Build Package'){
		    steps {
	    	    sh 'mvn clean package'
	    	}
		}
		stage('Deploy Package'){
			steps{
				withCredentials ([
					usernamePassword(
						credentilsId: 'nexus-creds',
						usernameVariable: 'NEXUS_USER',
						passwordVariable: 'NEXUS_PASS'
						)
					]) {
					sh 'mvn deploy -s /var/lib/jenkins/.m2/settings.xml'
				}
			}
		}
    }
}
