# Simple Java App — Jenkins CI/CD → Nexus → Docker Hub → Amazon EKS

## 1. Project Overview

This project demonstrates a complete CI/CD pipeline for deploying a Java application to Amazon EKS.

The source code is stored in GitHub. Jenkins automatically builds the application, publishes the JAR file to Nexus, creates a Docker image, pushes the image to Docker Hub, and deploys the application to Amazon EKS.

The application is finally exposed to the internet using an AWS Network Load Balancer (NLB).

### Complete Flow

```text
Developer
    |
    | git push
    v
GitHub
    |
    | Webhook / Jenkins Trigger
    v
Jenkins
    |
    +--> Maven Build
    |       |
    |       +--> JAR
    |              |
    |              v
    |           Nexus Repository
    |
    +--> Docker Build
    |       |
    |       v
    |    Docker Image
    |       |
    |       v
    |    Docker Hub
    |
    +--> AWS EKS Deployment
            |
            v
       Kubernetes
            |
            +--> Deployment
            |       |
            |       v
            |    Java Pods
            |     :8080
            |
            +--> Service
            |     :80
            |
            +--> HPA
            |
            v
    AWS Network Load Balancer
            |
            v
         Internet
            |
            v
         Browser
```

---

# 2. Project Goals

The main goals of this project are:

* Store Java source code in GitHub.
* Automatically build the Java application using Jenkins.
* Use Maven for application packaging.
* Store the generated JAR artifact in Nexus.
* Build a Docker image.
* Push the Docker image to Docker Hub.
* Deploy the application to Amazon EKS.
* Run multiple application replicas.
* Automatically scale Pods using Kubernetes HPA.
* Expose the application through an AWS Network Load Balancer.
* Demonstrate AWS VPC public/private subnet networking.
* Demonstrate Jenkins-to-AWS authentication using an IAM role.

---

# 3. Technologies Used

| Technology             | Purpose                                 |
| ---------------------- | --------------------------------------- |
| GitHub                 | Source code repository                  |
| Jenkins                | CI/CD automation                        |
| Jenkins Shared Library | Reusable Jenkins pipeline functions     |
| Java                   | Application runtime/development         |
| Maven                  | Build and package Java application      |
| Nexus Repository       | Maven artifact repository               |
| Docker                 | Containerization                        |
| Docker Hub             | Docker image registry                   |
| Kubernetes             | Container orchestration                 |
| Amazon EKS             | Managed Kubernetes cluster              |
| AWS NLB                | External application access             |
| Kubernetes HPA         | Automatic Pod scaling                   |
| Metrics Server         | Kubernetes CPU/memory metrics           |
| ConfigMap              | Non-sensitive application configuration |
| Secret                 | Sensitive application configuration     |
| AWS IAM                | AWS authentication/authorization        |
| AWS VPC                | Network infrastructure                  |

---

# 4. GitHub Repository

Repository:

```text
https://github.com/lokeshdevops01/simple-java-app
```

Branch:

```text
main
```

Local project directory:

```text
~/Documents/projects/simple-java-app
```

---

# 5. Project Structure

```text
simple-java-app/
│
├── kubernetes/
│   ├── configmap.yml
│   ├── deployment.yml
│   ├── hpa.yml
│   ├── ingress.yml
│   ├── namespace.yaml
│   ├── secret.yml
│   └── service.yml
│
├── src/
│   └── main/
│       └── ...
│
├── Dockerfile
├── Jenkinsfile
├── pom.xml
├── README.md
└── .gitignore
```

## Purpose of Each Important File

### `pom.xml`

Maven project configuration.

It defines:

* Java project information
* Group ID
* Artifact ID
* Version
* Dependencies
* Build configuration
* Maven plugins

The application version is:

```text
1.0-SNAPSHOT
```

---

### `Jenkinsfile`

Defines the Jenkins CI/CD pipeline.

The pipeline performs:

```text
Build
   ↓
Nexus
   ↓
Docker Build
   ↓
Docker Hub
   ↓
EKS Deployment
```

---

### `Dockerfile`

Defines how the Java application is packaged into a container image.

Current runtime:

```text
eclipse-temurin:17-jdk-alpine
```

Application port:

```text
8080
```

---

### `kubernetes/namespace.yaml`

Creates the Kubernetes namespace:

```text
demo-ns
```

---

### `kubernetes/configmap.yml`

Contains non-sensitive application configuration.

Example configuration:

```text
APP_ENV=production
APP_DEBUG=false
```

---

### `kubernetes/secret.yml`

Contains sensitive application configuration.

Secrets should not contain real credentials in GitHub.

For a production system, use a proper secret-management solution such as AWS Secrets Manager with an appropriate Kubernetes integration.

---

### `kubernetes/deployment.yml`

Creates and manages the Java application Pods.

Current configuration:

```text
Deployment:
simple-java-app

Replicas:
2

Container Port:
8080
```

---

### `kubernetes/service.yml`

Creates a Kubernetes `LoadBalancer` Service.

It exposes:

```text
Port:
80

Target Port:
8080
```

This Service causes Amazon EKS to provision an AWS Network Load Balancer.

---

### `kubernetes/hpa.yml`

Creates the Horizontal Pod Autoscaler.

Current configuration:

```text
Minimum replicas:
2

Maximum replicas:
5

CPU target:
50%
```

---

### `kubernetes/ingress.yml`

Contains an AWS ALB Ingress configuration.

The project currently uses the Kubernetes `LoadBalancer` Service/NLB as the verified external access path.

The Ingress manifest is retained as part of the project for demonstrating an alternative AWS load-balancing approach.

---

# 6. Jenkins Configuration

Jenkins is running on an AWS EC2 instance.

Jenkins EC2:

```text
ip-172-31-16-184
```

The Jenkins EC2 instance uses the IAM role:

```text
Jenkins-role
```

The IAM role has permission to access the EKS cluster.

EKS access is configured for the Jenkins role using:

```text
AmazonEKSClusterAdminPolicy
```

This allows Jenkins to execute Kubernetes deployment commands against the EKS cluster.

---

# 7. Jenkins Tools

The Jenkins global tools used by this project are:

```text
JDK:
java21

Maven:
maven3.9.16

Jenkins Shared Library:
my-shared-lib
```

---

# 8. Application and Docker Image

Docker Hub repository:

```text
lokeshdevops01/java-eks-app
```

Image:

```text
lokeshdevops01/java-eks-app:latest
```

Application port:

```text
8080
```

The Java application starts successfully and listens on port `8080`.

Application response:

```text
Welcome to My Simple Java Application!
```

---

# 9. Jenkins CI/CD Pipeline

## Stage 1 — Build Java Application

Jenkins uses Maven to build the application.

Conceptually:

```text
Source Code
    |
    v
Maven
    |
    v
JAR file
```

The JAR is generated under:

```text
target/
```

---

# 10. Stage 2 — Deploy JAR to Nexus

The generated JAR is deployed to Nexus.

Jenkins uses the Maven settings file:

```text
/var/lib/jenkins/.m2/settings.xml
```

The deployment command is:

```bash
mvn deploy -s /var/lib/jenkins/.m2/settings.xml
```

Nexus acts as the Maven artifact repository.

This means:

```text
Jenkins
   |
   | Maven deploy
   v
Nexus
   |
   v
simple-java-app-1.0-SNAPSHOT.jar
```

---

# 11. Stage 3 — Build Docker Image

Jenkins builds the Docker image from the Dockerfile.

Image:

```text
lokeshdevops01/java-eks-app:latest
```

Conceptually:

```text
Java JAR
   |
   v
Dockerfile
   |
   v
Docker Image
```

---

# 12. Stage 4 — Push Image to Docker Hub

Jenkins pushes the Docker image to Docker Hub.

```text
Jenkins
   |
   v
Docker Hub
   |
   v
lokeshdevops01/java-eks-app:latest
```

The EKS deployment then uses this image.

---

# 13. Stage 5 — Deploy to Amazon EKS

EKS cluster:

```text
dev-cluster
```

AWS Region:

```text
us-east-1
```

Before using `kubectl`, Jenkins configures access to the EKS cluster:

```bash
aws eks update-kubeconfig \
  --name dev-cluster \
  --region us-east-1
```

The Kubernetes namespace used by the application is:

```text
demo-ns
```

---

# 14. Kubernetes Deployment

Deployment:

```text
simple-java-app
```

Current desired replicas:

```text
2
```

Application container port:

```text
8080
```

Resource configuration:

```text
CPU Request:
100m

Memory Request:
128Mi

CPU Limit:
500m

Memory Limit:
512Mi
```

The Deployment ensures that the requested number of Pods are running.

Example:

```text
Deployment
    |
    +---- Pod 1
    |
    +---- Pod 2
```

---

# 15. Kubernetes Service

Service:

```text
simple-java-app-service
```

Type:

```text
LoadBalancer
```

Configuration:

```text
Service Port:
80

Target Port:
8080
```

Traffic flow:

```text
NLB :80
    |
    v
Kubernetes Service :80
    |
    v
Java Pod :8080
```

---

# 16. AWS Network Load Balancer

The Kubernetes `LoadBalancer` Service creates an AWS Network Load Balancer.

Scheme:

```text
internet-facing
```

Protocol:

```text
TCP
```

Listener:

```text
TCP :80
```

Target type:

```text
IP
```

Target port:

```text
8080
```

The NLB forwards traffic to the Java application Pods.

---

# 17. Final NLB Configuration

The final working NLB is enabled in all four Availability Zones:

```text
us-east-1a
us-east-1b
us-east-1c
us-east-1d
```

The Kubernetes Service explicitly specifies these four subnets:

```text
us-east-1a
subnet-09672585ed6105145

us-east-1b
subnet-02031ce81f58f8c8b

us-east-1c
subnet-0a3b96de6f9a006ab

us-east-1d
subnet-0438f274b72e41a09
```

Cross-zone load balancing is enabled:

```text
load_balancing.cross_zone.enabled=true
```

This configuration is important because the application Pods are running in `us-east-1c` and `us-east-1d`.

---

# 18. AWS VPC Architecture

VPC:

```text
vpc-06cd325fed320244b
```

CIDR:

```text
10.0.0.0/16
```

Internet Gateway:

```text
igw-091ee181d81db37e9
```

---

## Public Subnets

### us-east-1a

```text
Subnet:
subnet-09672585ed6105145

CIDR:
10.0.0.0/24
```

Route:

```text
0.0.0.0/0
    |
    v
Internet Gateway
```

### us-east-1b

```text
Subnet:
subnet-02031ce81f58f8c8b

CIDR:
10.0.1.0/24
```

Route:

```text
0.0.0.0/0
    |
    v
Internet Gateway
```

---

## Private EKS Subnets

### us-east-1c

```text
Subnet:
subnet-0a3b96de6f9a006ab

CIDR:
10.0.2.0/24
```

### us-east-1d

```text
Subnet:
subnet-0438f274b72e41a09

CIDR:
10.0.3.0/24
```

These subnets use the NAT Gateway for outbound internet access.

NAT Gateway:

```text
nat-164b8336ff33ffb35
```

Route:

```text
0.0.0.0/0
    |
    v
NAT Gateway
```

---

# 19. Why the NLB Networking Issue Happened

This was an important troubleshooting issue in the project.

Initially:

```text
NLB:
us-east-1a
us-east-1b
```

Application Pods:

```text
us-east-1c
us-east-1d
```

The NLB target group showed:

```text
Target.NotInUse
```

because the NLB was not enabled in the Availability Zones where the IP targets were located.

The initial configuration therefore looked like:

```text
NLB
  |
  +--> 1a
  |
  +--> 1b

Pods
  |
  +--> 1c
  |
  +--> 1d
```

The final solution was to configure the NLB across all four Availability Zones:

```text
NLB
  |
  +--> 1a
  +--> 1b
  +--> 1c
  +--> 1d
```

After the change, the targets became healthy:

```text
10.0.3.57:8080
healthy

10.0.2.179:8080
healthy
```

This restored external application access.

---

# 20. Kubernetes HPA

Horizontal Pod Autoscaler:

```text
simple-java-app-hpa
```

Configuration:

```text
Minimum replicas:
2

Maximum replicas:
5

CPU target:
50%
```

The HPA uses the Kubernetes Metrics Server.

Example:

```bash
kubectl top pods -n demo-ns
```

Current low-load behavior:

```text
CPU:
approximately 1% / 50%

Replicas:
2
```

If CPU utilization increases and remains above the configured target, Kubernetes can increase the number of Pods up to 5.

---

# 21. Metrics Server

Metrics Server is installed and working.

Verify:

```bash
kubectl top pods -n demo-ns
```

Example:

```text
NAME                              CPU    MEMORY
simple-java-app-xxxxx             1m     ...
simple-java-app-yyyyy             1m     ...
```

Metrics Server is required for CPU-based HPA scaling.

---

# 22. Useful Kubernetes Commands

## Configure EKS access

```bash
aws eks update-kubeconfig \
  --name dev-cluster \
  --region us-east-1
```

## Check cluster nodes

```bash
kubectl get nodes
```

## Check all application resources

```bash
kubectl get all -n demo-ns
```

## Check Pods

```bash
kubectl get pods -n demo-ns -o wide
```

## Check Deployment

```bash
kubectl get deployment -n demo-ns
```

## Check Service

```bash
kubectl get svc -n demo-ns
```

## Check HPA

```bash
kubectl get hpa -n demo-ns
```

## Check CPU and memory

```bash
kubectl top pods -n demo-ns
```

## Check application logs

```bash
kubectl logs -n demo-ns deployment/simple-java-app
```

## Follow logs

```bash
kubectl logs -f -n demo-ns deployment/simple-java-app
```

## Check endpoints

```bash
kubectl get endpoints -n demo-ns
```

For newer Kubernetes versions, EndpointSlices can also be checked:

```bash
kubectl get endpointslice -n demo-ns
```

## Check rollout

```bash
kubectl rollout status \
  deployment/simple-java-app \
  -n demo-ns
```

---

# 23. Useful AWS Commands

## Check EKS cluster

```bash
aws eks describe-cluster \
  --name dev-cluster \
  --region us-east-1
```

## Check AWS identity used by Jenkins

```bash
aws sts get-caller-identity
```

## List load balancers

```bash
aws elbv2 describe-load-balancers \
  --region us-east-1
```

## Check NLB target health

```bash
aws elbv2 describe-target-health \
  --region us-east-1 \
  --target-group-arn <TARGET_GROUP_ARN>
```

## Check VPC

```bash
aws ec2 describe-vpcs \
  --region us-east-1
```

## Check subnets

```bash
aws ec2 describe-subnets \
  --region us-east-1
```

## Check route tables

```bash
aws ec2 describe-route-tables \
  --region us-east-1
```

---

# 24. Application Verification

After deployment, first verify the Pods:

```bash
kubectl get pods -n demo-ns
```

Expected:

```text
2 Pods
1/1 Running
0 restarts
```

Then verify the Service:

```bash
kubectl get svc -n demo-ns
```

The Service should have an AWS NLB hostname under `EXTERNAL-IP`.

Then verify the NLB target health from AWS.

Expected:

```text
10.0.2.179:8080    healthy
10.0.3.57:8080     healthy
```

Finally, open the NLB hostname in a browser:

```text
http://<NLB-DNS-NAME>
```

Expected application response:

```text
Welcome to My Simple Java Application!
```

A successful HTTP response of:

```text
HTTP/1.1 200 OK
```

confirms that the complete traffic path is working.

---

# 25. End-to-End Traffic Flow

The final production-like traffic flow is:

```text
                        INTERNET
                            |
                            |
                            v
                AWS Network Load Balancer
                     Internet-Facing
                         TCP :80
                            |
                            v
              Kubernetes LoadBalancer Service
                       Port :80
                            |
                            v
                  Java Application Pods
                       Port :8080
                       /          \
                      /            \
                     v              v
              Pod 1 :8080     Pod 2 :8080
              10.0.2.179      10.0.3.57
```

The application Pods are located in:

```text
us-east-1c
us-east-1d
```

The NLB is enabled in:

```text
us-east-1a
us-east-1b
us-east-1c
us-east-1d
```

---

# 26. CI/CD Flow in Detail

When code is pushed to GitHub:

```text
1. Developer pushes code
        |
        v
2. GitHub
        |
        v
3. Jenkins starts pipeline
        |
        v
4. Maven builds Java application
        |
        v
5. JAR is deployed to Nexus
        |
        v
6. Docker image is built
        |
        v
7. Image is pushed to Docker Hub
        |
        v
8. Jenkins authenticates with EKS
        |
        v
9. Kubernetes manifests are applied
        |
        v
10. Deployment creates/updates Pods
        |
        v
11. Service exposes application through NLB
        |
        v
12. HPA monitors CPU
        |
        v
13. Application is accessible from the internet
```

---

# 27. Troubleshooting Guide

## Pods are not running

Check:

```bash
kubectl get pods -n demo-ns
```

Then:

```bash
kubectl describe pod <POD_NAME> -n demo-ns
```

And:

```bash
kubectl logs <POD_NAME> -n demo-ns
```

---

## Application is running but Service has no external IP

Check:

```bash
kubectl get svc -n demo-ns
```

Then:

```bash
kubectl describe svc simple-java-app-service -n demo-ns
```

Look at the Events section for AWS load-balancer errors.

---

## NLB target is `Target.NotInUse`

Check the NLB Availability Zones:

```bash
aws elbv2 describe-load-balancers \
  --region us-east-1
```

Check target health:

```bash
aws elbv2 describe-target-health \
  --region us-east-1 \
  --target-group-arn <TARGET_GROUP_ARN>
```

Make sure the NLB is enabled in the Availability Zones containing the application Pod IPs.

In this project, the final NLB configuration uses:

```text
us-east-1a
us-east-1b
us-east-1c
us-east-1d
```

---

## NLB targets are unhealthy

Check:

```bash
kubectl get pods -n demo-ns -o wide
```

Then verify that the application is actually listening on:

```text
8080
```

Check logs:

```bash
kubectl logs -n demo-ns deployment/simple-java-app
```

Check Service endpoints:

```bash
kubectl get endpoints -n demo-ns
```

---

## HPA is not scaling

Check:

```bash
kubectl get hpa -n demo-ns
```

Then:

```bash
kubectl top pods -n demo-ns
```

If metrics are unavailable, check Metrics Server:

```bash
kubectl get pods -n kube-system | grep metrics
```

---

## Jenkins cannot access EKS

Check AWS identity:

```bash
aws sts get-caller-identity
```

Check kubeconfig:

```bash
aws eks update-kubeconfig \
  --name dev-cluster \
  --region us-east-1
```

Then:

```bash
kubectl get nodes
```

The Jenkins EC2 IAM role must have the required EKS permissions/access.

---

# 28. Important Security Notes

This project is primarily a learning/portfolio project.

For a production implementation, improve the following:

### Docker image tags

Instead of always using:

```text
latest
```

use immutable tags such as:

```text
1.0.0
BUILD_NUMBER
GIT_COMMIT
```

This makes rollback easier.

### Kubernetes Secrets

Do not commit real passwords, API keys, tokens, or credentials to GitHub.

Use:

* AWS Secrets Manager
* External Secrets
* Sealed Secrets
* Another approved secret-management solution

### IAM permissions

Avoid giving Jenkins more AWS permissions than necessary.

Use least-privilege IAM policies.

### HTTPS

The current NLB listener is:

```text
TCP :80
```

For production, use HTTPS/TLS and a suitable certificate.

### Image scanning

Add container image vulnerability scanning before deploying images to production.

### Kubernetes security

Consider:

* Pod Security Standards
* Network Policies
* Resource limits
* Read-only root filesystem where possible
* Non-root containers
* Security contexts
* RBAC with least privilege

---

# 29. What I Learned From This Project

This project helped demonstrate practical understanding of:

* Git and GitHub
* Jenkins CI/CD
* Jenkins Shared Libraries
* Maven
* Nexus Repository
* Docker
* Docker Hub
* Kubernetes
* Amazon EKS
* Kubernetes Deployments
* Kubernetes Services
* Kubernetes HPA
* Metrics Server
* ConfigMaps
* Secrets
* AWS IAM
* AWS VPC
* Public and private subnets
* Internet Gateway
* NAT Gateway
* AWS Network Load Balancer
* Kubernetes-to-AWS integration
* Troubleshooting AWS networking
* End-to-end application deployment

---

# 30. Final Project Status

The complete CI/CD pipeline has been successfully implemented.

```text
GitHub
   |
   v
Jenkins
   |
   +--> Maven Build
   |
   +--> Nexus
   |
   +--> Docker Build
   |
   +--> Docker Hub
   |
   +--> Amazon EKS
          |
          +--> Kubernetes Deployment
          |
          +--> 2 Java Pods
          |
          +--> HPA: 2-5 replicas
          |
          +--> LoadBalancer Service
          |
          +--> AWS NLB
          |
          v
       Internet
          |
          v
       Browser
          |
          v
Welcome to My Simple Java Application!
```

## Final Result

The application successfully responds with:

```text
Welcome to My Simple Java Application!
```

The final verified path is:

```text
GitHub
→ Jenkins
→ Maven
→ Nexus
→ Docker Build
→ Docker Hub
→ Amazon EKS
→ Kubernetes Service
→ AWS Network Load Balancer
→ Java Application
```

**Project Status: Completed Successfully**

