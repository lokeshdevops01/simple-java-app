# Simple Java App — Jenkins CI/CD to Amazon EKS

## 1. Project Overview

This project demonstrates an end-to-end CI/CD pipeline for deploying a Java application to Amazon EKS.

The application source code is stored in GitHub. Jenkins builds the application using Maven, publishes the JAR file to Nexus, builds a Docker image, pushes the image to Docker Hub, and deploys the application to Amazon EKS.

Kubernetes runs two application Pods and uses HPA for automatic scaling.

The application is exposed externally using an AWS Network Load Balancer (NLB).

### Complete Flow

```text
GitHub
   ↓
Jenkins
   ↓
Maven Build
   ↓
JAR → Nexus
   ↓
Docker Build
   ↓
Docker Image → Docker Hub
   ↓
Amazon EKS
   ↓
Kubernetes Deployment
   ↓
Kubernetes Service
   ↓
AWS Network Load Balancer
   ↓
Application :8080
```

---

# 2. Technologies Used

| Technology             | Purpose                            |
| ---------------------- | ---------------------------------- |
| GitHub                 | Source code repository             |
| Jenkins                | CI/CD automation                   |
| Jenkins Shared Library | Reusable pipeline functions        |
| Java                   | Application                        |
| Maven                  | Build and package Java application |
| Nexus                  | Maven artifact repository          |
| Docker                 | Containerization                   |
| Docker Hub             | Docker image repository            |
| Kubernetes             | Container orchestration            |
| Amazon EKS             | Managed Kubernetes cluster         |
| AWS NLB                | External access to application     |
| HPA                    | Automatic Pod scaling              |
| Metrics Server         | CPU and memory metrics             |
| AWS IAM                | AWS authentication and permissions |
| AWS VPC                | Network infrastructure             |

---

# 3. GitHub Repository

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

# 4. Project Structure

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
│
├── Dockerfile
├── Jenkinsfile
├── pom.xml
├── README.md
└── .gitignore
```

## Important Files

### `pom.xml`

Maven configuration for the Java application.

It contains the project information, dependencies, Java configuration, and build configuration.

### `Jenkinsfile`

Defines the Jenkins CI/CD pipeline.

### `Dockerfile`

Defines how the Java application is packaged into a Docker image.

### `kubernetes/namespace.yaml`

Creates the Kubernetes namespace:

```text
demo-ns
```

### `kubernetes/deployment.yml`

Creates and manages the Java application Pods.

### `kubernetes/service.yml`

Creates the Kubernetes `LoadBalancer` Service and provisions the AWS Network Load Balancer.

### `kubernetes/hpa.yml`

Configures automatic Pod scaling.

### `kubernetes/configmap.yml`

Stores non-sensitive application configuration.

### `kubernetes/secret.yml`

Stores sensitive Kubernetes configuration.

Real passwords, tokens, and credentials should never be committed to GitHub.

### `kubernetes/ingress.yml`

Contains an Ingress configuration for the project. The verified external access path for this project is the `LoadBalancer` Service and AWS NLB.

---

# 5. Jenkins Configuration

Jenkins runs on an AWS EC2 instance.

Jenkins EC2:

```text
ip-172-31-16-184
```

IAM Role:

```text
Jenkins-role
```

Jenkins has AWS permissions to access the EKS cluster.

EKS access includes:

```text
AmazonEKSClusterAdminPolicy
```

> For a production environment, a more restricted least-privilege policy should be used.

---

# 6. Jenkins Tools

The Jenkins environment uses:

```text
JDK:
java21

Maven:
maven3.9.16

Jenkins Shared Library:
my-shared-lib
```

---

# 7. CI/CD Pipeline

The pipeline follows this process:

```text
1. GitHub
      ↓
2. Jenkins Checkout
      ↓
3. Maven Build
      ↓
4. Deploy JAR to Nexus
      ↓
5. Build Docker Image
      ↓
6. Push Docker Image to Docker Hub
      ↓
7. Deploy to Amazon EKS
```

---

# 8. Maven Build

Maven builds the Java application and generates the JAR file.

The generated JAR is created under:

```text
target/
```

The JAR is then deployed to Nexus.

Flow:

```text
Java Source
    ↓
Maven
    ↓
JAR
    ↓
Nexus
```

---

# 9. Nexus Repository

Nexus is used as the Maven artifact repository.

Jenkins deploys the generated JAR to Nexus.

Purpose:

```text
Store and manage Maven application artifacts
```

The Maven deployment uses:

```text
/var/lib/jenkins/.m2/settings.xml
```

---

# 10. Docker

After Maven builds the Java application, Jenkins creates a Docker image.

Docker image:

```text
lokeshdevops01/java-eks-app:latest
```

The Dockerfile uses:

```text
eclipse-temurin:17-jdk-alpine
```

Application port:

```text
8080
```

Flow:

```text
Java JAR
   ↓
Dockerfile
   ↓
Docker Image
```

---

# 11. Docker Hub

Docker Hub repository:

```text
lokeshdevops01/java-eks-app
```

Image:

```text
lokeshdevops01/java-eks-app:latest
```

Jenkins pushes the image to Docker Hub.

EKS uses this image to run the application Pods.

---

# 12. Amazon EKS

Cluster:

```text
dev-cluster
```

Region:

```text
us-east-1
```

Namespace:

```text
demo-ns
```

Jenkins configures EKS access using:

```bash
aws eks update-kubeconfig \
  --name dev-cluster \
  --region us-east-1
```

The Jenkins IAM role is authorized to access the cluster.

---

# 13. Kubernetes Deployment

Deployment:

```text
simple-java-app
```

Replicas:

```text
2
```

Application port:

```text
8080
```

Container resource limits:

```text
CPU limit:
500m

Memory limit:
512Mi
```

The Deployment maintains the desired number of application Pods.

Current application Pods run successfully:

```text
Pod 1 → 10.0.3.57:8080
Pod 2 → 10.0.2.179:8080
```

Both Pods were verified as:

```text
1/1 Running
0 restarts
```

---

# 14. Kubernetes Service

Service:

```text
simple-java-app-service
```

Namespace:

```text
demo-ns
```

Type:

```text
LoadBalancer
```

Ports:

```text
Service Port:
80

Target Port:
8080
```

Traffic flow:

```text
AWS NLB :80
      ↓
Kubernetes Service :80
      ↓
Java Pod :8080
```

---

# 15. AWS Network Load Balancer

The Kubernetes `LoadBalancer` Service creates an AWS Network Load Balancer.

Configuration:

```text
Type:
Network Load Balancer

Scheme:
internet-facing

Listener:
TCP :80

Target Type:
IP

Target Port:
8080
```

The NLB forwards traffic directly to the Kubernetes Pod IPs.

Current targets:

```text
10.0.3.57:8080
10.0.2.179:8080
```

Final target health:

```text
10.0.3.57:8080 → healthy
10.0.2.179:8080 → healthy
```

---

# 16. NLB Service Annotations

The final `service.yml` contains:

```yaml
annotations:
  service.beta.kubernetes.io/aws-load-balancer-scheme: internet-facing

  service.beta.kubernetes.io/aws-load-balancer-subnets: subnet-09672585ed6105145,subnet-02031ce81f58f8c8b,subnet-0a3b96de6f9a006ab,subnet-0438f274b72e41a09

  service.beta.kubernetes.io/aws-load-balancer-attributes: load_balancing.cross_zone.enabled=true
```

## Annotation 1 — Internet Facing

```yaml
service.beta.kubernetes.io/aws-load-balancer-scheme: internet-facing
```

This tells EKS to create an NLB intended for external/internet traffic.

---

## Annotation 2 — NLB Subnets

```yaml
service.beta.kubernetes.io/aws-load-balancer-subnets: ...
```

This explicitly tells AWS which subnets to use for the NLB.

The final configuration uses:

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

---

## Annotation 3 — Cross-Zone Load Balancing

```yaml
service.beta.kubernetes.io/aws-load-balancer-attributes: load_balancing.cross_zone.enabled=true
```

This enables cross-zone load balancing.

It allows the NLB to distribute traffic across healthy targets in different Availability Zones.

---

# 17. Important NLB Problem and Fix

This was the main AWS networking problem encountered during the project.

Initially, the NLB was enabled only in:

```text
us-east-1a
us-east-1b
```

But the application Pods were running in:

```text
us-east-1c
us-east-1d
```

The target group showed:

```text
Target.NotInUse
```

### Initial Configuration

```text
NLB
├── us-east-1a
└── us-east-1b

Pods
├── us-east-1c
└── us-east-1d
```

The NLB did not have nodes in the Availability Zones containing the IP targets.

### Final Configuration

The NLB was configured across all four Availability Zones:

```text
NLB
├── us-east-1a
├── us-east-1b
├── us-east-1c
└── us-east-1d

Pods
├── us-east-1c
└── us-east-1d
```

After the change, both targets became healthy:

```text
10.0.3.57:8080 → healthy
10.0.2.179:8080 → healthy
```

### Important Lesson

When troubleshooting an EKS Service backed by an AWS NLB:

```text
Check Pod AZs
      ↓
Check NLB AZs
      ↓
Check NLB subnets
      ↓
Check target health
```

If an NLB IP target is in an Availability Zone that is not enabled for the NLB, the target can show:

```text
Target.NotInUse
```

---

# 18. AWS VPC

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
0.0.0.0/0 → Internet Gateway
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
0.0.0.0/0 → Internet Gateway
```

---

## EKS Application Subnets

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

These subnets use the NAT Gateway for outbound internet connectivity.

NAT Gateway:

```text
nat-164b8336ff33ffb35
```

Route:

```text
0.0.0.0/0 → NAT Gateway
```

---

# 19. Kubernetes HPA

HPA name:

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

The HPA uses CPU metrics from Metrics Server.

Flow:

```text
Low CPU
   ↓
2 Pods

High CPU
   ↓
HPA increases replicas
   ↓
Maximum 5 Pods
```

---

# 20. Metrics Server

Metrics Server is installed and working.

Verify:

```bash
kubectl top pods -n demo-ns
```

Example:

```text
CPU:
approximately 1%

Target:
50%
```

Metrics Server provides the metrics required by the HPA.

---

# 21. Application

Application port:

```text
8080
```

Application startup log confirmed:

```text
Application started on port 8080
```

Application response:

```text
Welcome to My Simple Java Application!
```

---

# 22. Final Application Traffic Flow

```text
Internet
    |
    v
AWS Internet-Facing NLB
TCP :80
    |
    v
Kubernetes LoadBalancer Service
Port :80
    |
    v
Java Application Pods
Port :8080
    |
    +---- Pod 1
    |
    +---- Pod 2
```

---

# 23. NLB DNS

The NLB DNS name generated during this project was:

```text
k8s-demons-simpleja-e20719481b-a691ca863744c4cc.elb.us-east-1.amazonaws.com
```

The DNS name can change if the Kubernetes Service/NLB is recreated.

Therefore, do not hard-code this DNS name into application code.

Get the current NLB DNS using:

```bash
kubectl get svc simple-java-app-service -n demo-ns
```

---

# 24. Verification

## Check Pods

```bash
kubectl get pods -n demo-ns -o wide
```

Expected:

```text
2 Pods
1/1 Running
0 restarts
```

## Check Service

```bash
kubectl get svc -n demo-ns
```

## Check HPA

```bash
kubectl get hpa -n demo-ns
```

## Check Metrics

```bash
kubectl top pods -n demo-ns
```

## Check Application Logs

```bash
kubectl logs -n demo-ns deployment/simple-java-app
```

## Check Endpoints

```bash
kubectl get endpoints -n demo-ns
```

## Check EndpointSlices

```bash
kubectl get endpointslice -n demo-ns
```

## Check Deployment Rollout

```bash
kubectl rollout status \
  deployment/simple-java-app \
  -n demo-ns
```

---

# 25. AWS Verification Commands

## Check AWS Identity

```bash
aws sts get-caller-identity
```

## Check EKS Cluster

```bash
aws eks describe-cluster \
  --name dev-cluster \
  --region us-east-1
```

## Check NLB

```bash
aws elbv2 describe-load-balancers \
  --region us-east-1
```

## Check Target Health

```bash
aws elbv2 describe-target-health \
  --region us-east-1 \
  --target-group-arn <TARGET_GROUP_ARN>
```

Expected:

```text
10.0.3.57:8080 → healthy
10.0.2.179:8080 → healthy
```

## Check VPC

```bash
aws ec2 describe-vpcs \
  --region us-east-1
```

## Check Subnets

```bash
aws ec2 describe-subnets \
  --region us-east-1
```

## Check Route Tables

```bash
aws ec2 describe-route-tables \
  --region us-east-1
```

---

# 26. Troubleshooting

## Pods are not running

```bash
kubectl get pods -n demo-ns

kubectl describe pod <POD_NAME> -n demo-ns

kubectl logs <POD_NAME> -n demo-ns
```

---

## Service has no external DNS

```bash
kubectl get svc -n demo-ns

kubectl describe svc \
  simple-java-app-service \
  -n demo-ns
```

Check the Events section for AWS load-balancer errors.

---

## NLB target shows `Target.NotInUse`

Check:

```text
1. Pod Availability Zones
2. NLB Availability Zones
3. NLB subnet configuration
4. Target group target type
5. Target health
```

Commands:

```bash
kubectl get pods -n demo-ns -o wide
```

and:

```bash
aws elbv2 describe-target-health \
  --region us-east-1 \
  --target-group-arn <TARGET_GROUP_ARN>
```

---

## HPA is not scaling

Check:

```bash
kubectl get hpa -n demo-ns

kubectl top pods -n demo-ns
```

---

## Jenkins cannot access EKS

Check:

```bash
aws sts get-caller-identity

aws eks update-kubeconfig \
  --name dev-cluster \
  --region us-east-1

kubectl get nodes
```

Verify the Jenkins IAM role and EKS access configuration.

---

# 27. Security Notes

This project is primarily a learning and portfolio project.

For production use:

* Use least-privilege IAM permissions.
* Do not store real credentials in GitHub.
* Use AWS Secrets Manager or another proper secret-management solution.
* Use immutable Docker image tags instead of only `latest`.
* Use HTTPS/TLS instead of plain HTTP.
* Add container vulnerability scanning.
* Use Kubernetes RBAC with least privilege.
* Use appropriate Kubernetes security contexts and network policies.

---

# 28. What This Project Demonstrates

This project demonstrates practical experience with:

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
* Kubernetes Secrets
* AWS IAM
* AWS VPC
* Public and application subnets
* Internet Gateway
* NAT Gateway
* AWS Network Load Balancer
* Availability Zones
* AWS networking troubleshooting
* End-to-end application deployment

---

# 29. Final Architecture

```text
                         GitHub
                            |
                            v
                         Jenkins
                            |
              +-------------+-------------+
              |                           |
              v                           v
         Maven Build                  Docker Build
              |                           |
              v                           v
            Nexus                    Docker Hub
              |                           |
              +-------------+-------------+
                            |
                            v
                       Amazon EKS
                            |
                       demo-ns
                            |
                       Deployment
                            |
                 +----------+----------+
                 |                     |
                 v                     v
              Pod 1                 Pod 2
             :8080                 :8080
                 \                     /
                  \                   /
                   +-------+---------+
                           |
                           v
                 Kubernetes Service
                        :80
                           |
                           v
                AWS Network Load Balancer
                        :80
                           |
                           v
                       Internet
```

---

# 30. Final Project Status

```text
GitHub                  ✅
Jenkins                 ✅
Maven Build             ✅
Nexus                   ✅
Docker Build            ✅
Docker Hub Push         ✅
Amazon EKS              ✅
Kubernetes Deployment   ✅
Kubernetes Service      ✅
HPA                     ✅
Metrics Server          ✅
AWS NLB                 ✅
NLB Target Health       ✅
External HTTP Test      ✅
```

The NLB was successfully tested from the Jenkins EC2 instance and returned:

```text
HTTP/1.1 200 OK
```

Application response:

```text
Welcome to My Simple Java Application!
```

---

# 31. Final Result

The completed CI/CD and deployment flow is:

```text
GitHub
   ↓
Jenkins
   ↓
Maven
   ↓
Nexus
   ↓
Docker Build
   ↓
Docker Hub
   ↓
Amazon EKS
   ↓
Kubernetes Deployment
   ↓
Kubernetes Service
   ↓
AWS Network Load Balancer
   ↓
Java Application
```

## Project Status: COMPLETED ✅

