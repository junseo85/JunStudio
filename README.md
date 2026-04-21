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

To review the platform’s functionality, use the credentials below.

### User Roles

**Admin Account**
- **Email:** `admin@junstudio.com`
- **Password:** `admin123`

**Student Account**
- Create a new account via the Sign-Up page.

---

## 💳 Stripe Checkout (Test Mode)

Stripe is integrated in **Test Mode**.

- **Card Number:** `4242 4242 4242 4242`
- **Expiry:** Any future date
- **CVC:** `123`
