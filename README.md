
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
```bash
# Clone the optimized branch
git clone -b EC2 https://github.com/junseo85/JunStudio.git
cd JunStudio

# Start the environment
docker-compose up -d --build
```

