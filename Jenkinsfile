@Library('my-shared-lib') _

pipeline {
    agent any

    tools {
        jdk 'java21'
        maven 'maven3.9.16'
    }

    environment {
        DOCKER_IMAGE = "lokeshdevops01/java-eks-app:latest"
        EKS_CLUSTER  = "dev-cluster"
        AWS_REGION   = "us-east-1"
    }

    stages {

        stage('Build Package') {
            steps {
                mavenBuild()
            }
        }

        stage('Deploy Package to Nexus') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )
                ]) {
                    sh 'mvn deploy -s /var/lib/jenkins/.m2/settings.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build -t $DOCKER_IMAGE .
                '''
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $DOCKER_IMAGE
                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to EKS') {
            steps {
                sh '''
                    aws eks update-kubeconfig \
                        --name $EKS_CLUSTER \
                        --region $AWS_REGION

                    kubectl apply -f kubernetes/namespace.yaml
                    kubectl apply -f kubernetes/configmap.yml
                    kubectl apply -f kubernetes/secret.yml

                    kubectl apply -f kubernetes/deployment.yml

                    kubectl set image deployment/simple-java-app \
                        simple-java-app=$DOCKER_IMAGE \
                        -n demo-ns

                    kubectl apply -f kubernetes/service.yml
                    kubectl apply -f kubernetes/hpa.yml
                    kubectl apply -f kubernetes/ingress.yml

                    kubectl rollout status deployment/simple-java-app \
                        -n demo-ns \
                        --timeout=180s

                    kubectl get pods -n demo-ns
                    kubectl get svc -n demo-ns
                    kubectl get ingress -n demo-ns
                '''
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully."
            echo "Docker Image: ${DOCKER_IMAGE}"
            echo "EKS Cluster: ${EKS_CLUSTER}"
        }

        failure {
            echo "Pipeline failed."
        }

        always {
            sh 'docker images | grep java-eks-app || true'
        }
    }
}
