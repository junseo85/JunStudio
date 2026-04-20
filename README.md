# JunStudio - Full Stack Cello Instruction Platform

JunStudio is a production-ready web application designed for music educators to manage students and lessons. This project served as my "bridge" into software engineering, moving from 20 years of professional cello performance into a full-stack cloud environment.

## 🚀 Live Demo
**URL:** [k8s-default-junstudi-3ed54b9d5b-aa91faa9775ff51c.elb.us-east-2.amazonaws.com ]

## 🛠️ Tech Stack
* **Backend:** Java 17, Spring Boot, Spring Security (JWT)
* **Frontend:** React.js, Tailwind CSS, Framer Motion
* **Database:** MySQL (AWS RDS / Containerized)
* **Cloud & DevOps:** AWS EKS (Fargate), Docker, Kubernetes, AWS Load Balancer Controller
* **Payments:** Stripe API integration

## 🔑 Access & Testing
To review the platform's functionality, please use the following credentials:

### User Roles:
* **Admin Account:**
   * **Email:** `admin@junstudio.com` (or your actual admin email)
   * **Password:** `admin123` (or your actual test password)
* **Student Account:** Create any new account via the Sign-Up page.

### 💳 Stripe Checkout (Test Mode):
The payment system is integrated with Stripe in Test Mode. You can use any of the following test cards:
* **Card Number:** `4242 4242 4242 4242`
* **Expiry:** Any future date (e.g., 12/28)
* **CVC:** `123`
* **Zip:** `12345`

## 🏗️ Architecture & Infrastructure
This application is deployed using a **Serverless Kubernetes (EKS Fargate)** architecture to ensure high availability and zero-overhead scaling.
* **CI/CD:** Containerized with Docker and pushed to AWS ECR.
* **Networking:** Traffic is managed via an AWS Network Load Balancer (NLB) provisioned by the AWS Load Balancer Controller.
* **Security:** IAM OIDC providers are used to allow Kubernetes service accounts to securely communicate with AWS resources.

---