# JunStudio — Full Stack Cello Instruction Platform

JunStudio is a production-ready web application designed for music educators to manage **students**, **scheduling**, and **payments**. This project represents my career transition from 20+ years of professional experience into full-stack software engineering.

---

## 🚀 Live Production Environment

- **Domain:** `celloJun.com`
- **Architecture:** AWS EKS (Kubernetes) + RDS (MySQL) + Cloudflare

---

## 🛠️ Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security (JWT)
- Hibernate / JPA

### Frontend
- React.js
- Tailwind CSS
- Framer Motion

### Database
- Amazon RDS (MySQL)

### Cloud & DevOps
- AWS EKS (Fargate)
- Docker
- Kubernetes
- AWS Load Balancer Controller

### Security / Edge
- Cloudflare DNS
- SSL/TLS Encryption

### Integrations
- **Payments:** Stripe API (Test Mode Integration)
- **Email:** Java Mail Sender (SMTP) via Gmail API for automated lesson confirmations and user notifications

---

## 💎 Core Features

- **Student Dashboard:** Real-time view of upcoming cello lessons and practice materials
- **Automated Scheduling:** Synchronized calendar system with instant email confirmations for both instructor and student
- **Secure Payments:** Integrated Stripe checkout for lesson packages with automated digital receipting
- **Teacher Admin Panel:** Centralized hub for managing student rosters and tracking studio revenue

---

## 🏗️ Infrastructure & Architecture

The platform is built on a professional-grade cloud stack designed for high availability and security:

- **Orchestration:** Deployed on Amazon EKS using Fargate profiles for serverless pod execution (no EC2 instance management)
- **Networking:** Traffic routed through an AWS Network Load Balancer (NLB) and secured at the edge by Cloudflare for optimized global latency and DDoS protection
- **Data Persistence:** Managed Amazon RDS instance configured within a private VPC subnet to ensure data isolation and security
- **Configuration Management:** Kubernetes Secrets securely inject production credentials (RDS, Stripe, SMTP) at runtime, keeping the repository free of sensitive data

---

## 📧 Automated Communication System

JunStudio includes an automated notification system to keep teachers and students synchronized:

- **Trigger-Based Notifications:** Sends professionally formatted HTML emails upon successful registration, lesson scheduling, and payment confirmation
- **Production Security:** Uses Google App Passwords + Kubernetes Secrets for SMTP authentication so credentials are never exposed
- **Asynchronous Execution (Optional):** If implemented via `@Async`, email dispatch runs asynchronously to keep the UI responsive

---

## 🧠 Key Engineering Challenges

### Cross-VPC Connectivity & Security
During the migration from a containerized local database to Amazon RDS, I encountered a `SocketTimeoutException` caused by VPC isolation. I diagnosed the networking conflict and resolved the issue by correctly configuring VPC access and security boundaries.

### Zero-Downtime Deployments
Implemented Kubernetes Rolling Updates and `kubectl rollout` management so that environment variable updates and code changes can be deployed without service interruption.

---

## 🔑 Access & Testing

Student accounts can be created through the Sign-Up page. Teacher and admin
accounts must be provisioned securely; no default admin is created.

---

## 💳 Stripe Checkout (Test Mode)

Stripe is integrated in **Test Mode**.

- **Card Number:** `4242 4242 4242 4242`
- **Expiry:** Any future date
- **CVC:** `123`

---

# 🎼 JunStudio: Music Lesson Management Platform
> **Strategic Cost-Optimized Deployment Edition (EC2 Branch)**

**Live Platform:** [https://cellojun.com](https://cellojun.com)  
**Architecture:** Multi-Container Docker on AWS EC2 + AWS RDS

---

## 📖 Project Overview
**JunStudio** is a full-stack studio management solution built to eliminate administrative friction for music educators. It features a "three-click" lesson booking system, an automated admin dashboard for revenue and scheduling analytics, and secure role-based access control.

### **The "Why" Behind This Branch**
While the `master` branch utilizes a high-availability **AWS EKS (Kubernetes)** cluster, this branch represents a complete migration to a standalone **AWS EC2** architecture. By transitioning from EKS/Fargate to a containerized EC2 setup, I successfully reduced the monthly infrastructure "burn rate" by over **95%** while maintaining production performance and security.

---

## 🛠 Tech Stack
| Layer | Technologies |
| :--- | :--- |
| **Frontend** | React.js, Tailwind CSS, Framer Motion (UI/UX) |
| **Backend** | Java 17, Spring Boot, Spring Security (JWT) |
| **Database** | AWS RDS (MySQL 8.0) |
| **DevOps** | Docker, Docker Compose, Linux (Amazon Linux 2023) |
| **Networking** | Cloudflare (Proxy, SSL/TLS), Reverse Proxy Orchestration |

---

## 🚀 Key Engineering Challenges & Solutions

### **1. Infrastructure Migration (EKS → EC2)**
* **Problem:** The original EKS Control Plane cost ~$72/month, making it unsustainable for a solo project.
* **Solution:** Migrated the orchestration to a single `t3.micro` EC2 instance using **Docker Compose**. I manually configured the networking layer to bridge the gap between Docker's internal bridge network and the AWS VPC.

### **2. JVM Performance Tuning on Limited Hardware**
* **Problem:** Spring Boot and MySQL containers combined exceed the 1GB RAM limit of a `t3.micro` instance, leading to "Out of Memory" (OOM) crashes.
* **Solution:** Implemented **2GB of Swap Space (Virtual RAM)** on the Linux host. This allowed the JVM to handle memory-intensive startup tasks without requiring an expensive instance upgrade.

### **3. Production Networking & SSL**
* **Problem:** AWS Load Balancers (ALB) add significant monthly costs.
* **Solution:** Leveraged **Cloudflare’s Flexible SSL** and configured a manual reverse proxy within Docker. By mapping internal container port `8080` to host port `80`, I achieved a professional `https://` domain experience with $0 overhead.

---

## 📊 Features
* **Admin Analytics:** High-efficiency data mapping in the Java backend converts raw relational data into drill-down charts for lesson reporting.
* **Booking System:** A user-centric React interface designed for students to book lessons in under 3 clicks.
* **Secure Authentication:** JWT-based stateless authentication with secure role-based navigation.

---

## 📦 Local & Deployment Setup

### **Prerequisites**
* Docker & Docker Compose
* Java 17 (for local dev)

### **Deployment Command**
Set `MYSQL_ROOT_PASSWORD`, `MAIL_USER`, and `MAIL_PASS` in a protected,
untracked `.env` file or in the deployment environment before starting Compose.
The Compose file runs its own MySQL container; it is not an RDS configuration.

```bash
# Clone the optimized branch
git clone -b EC2 https://github.com/junseo85/JunStudio.git
cd JunStudio

# Start the environment
docker-compose up -d --build
```

### Credential remediation for existing deployments

- Restrict public access until the known default admin and documented test
  teacher accounts have been disabled or securely reset in the live database.
  Reset all accounts created with the former shared default password as well.
  Removing the seeder and shared default does not change existing records.
- Rotate any exposed database, Stripe, mail, AWS, and other credentials at
  their providers. Update the deployed secrets and inspect account access logs.
- For Kubernetes, provision `junstudio-mysql-secrets` with a
  `MYSQL_ROOT_PASSWORD` key through your secret-management process before
  deploying `k8s/mysql-deployment.yaml`. Keep the app's database credentials
  in `junstudio-secrets` consistent with the database.
- MySQL initialization variables apply to a new data directory only. For an
  existing volume, change the database password using MySQL's account management
  procedure, then update both application and database deployment secrets.
  Back up the database before making changes.
- Environment variables and local `.env` files are accessible to sufficiently
  privileged host users. Restrict host and file permissions and prefer a managed
  secrets solution for production. The Compose MySQL port is bound to localhost;
  the Compose configuration is not a hardened production database setup.
- Review the full Git history and secret-scanning alerts after rotation. Removing
  a value from the current tree does not remove it from history, forks or clones.
  Coordinate any history rewrite with repository collaborators.
- Verify the live deployment, login, database connectivity, mail and payments
  after rotating secrets and redeploying.
