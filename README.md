JunStudio - Full Stack Cello Instruction Platform
JunStudio is a production-ready web application designed for music educators to manage students, scheduling, and payments. This project represents my career transition from 20+ years of professional cello performance and teaching at Washington University in St. Louis into the field of Software Engineering.

🚀 Live Production Environment
Domain: cellojun.com
Architecture: AWS EKS (Kubernetes) + RDS (MySQL) + Cloudflare

🛠️ Tech Stack
Backend: Java 17, Spring Boot, Spring Security (JWT), Hibernate/JPA

Frontend: React.js, Tailwind CSS, Framer Motion

Database: Managed Amazon RDS (MySQL)

Cloud & DevOps: AWS EKS (Fargate), Docker, Kubernetes, AWS Load Balancer Controller

Infrastructure Security: Cloudflare DNS, SSL/TLS Encryption

Payments: Stripe API (Test Mode Integration)

Email Service: Java Mail Sender (SMTP) via Gmail API for automated lesson confirmations and user notifications.

💎 Core Features
Student Dashboard: Real-time view of upcoming cello lessons and practice materials.

Automated Scheduling: Synchronized calendar system with instant email confirmations for both instructor and student.

Secure Payments: Integrated Stripe checkout for lesson packages with automated digital receipting.

Teacher Admin Panel: Centralized hub for managing student rosters and tracking studio revenue.

🏗️ Infrastructure & Architecture
The platform is built on a professional-grade cloud stack designed for high availability and security:

Orchestration: Deployed on Amazon EKS using Fargate profiles for serverless pod execution, eliminating the need to manage underlying EC2 instances.

Networking: Traffic is routed through an AWS Network Load Balancer (NLB) and secured at the edge by Cloudflare, providing optimized global latency and DDoS protection.

Data Persistence: Utilizes a managed Amazon RDS instance configured within a private VPC subnet to ensure data isolation and security.

Configuration Management: Implemented Kubernetes Secrets to securely inject production credentials (RDS, Stripe, SMTP) at runtime, keeping the source code clean of sensitive data.

📧 Automated Communication System
The platform features an automated notification system to keep teachers and students synchronized:

Trigger-Based Notifications: Automatically sends professionally formatted HTML emails upon successful registration, lesson scheduling, and payment confirmation.

Production Security: Leverages Google App Passwords and Kubernetes Secrets to manage SMTP authentication, ensuring email credentials are never exposed in the application environment.

Asynchronous Execution: (Optional: If you used @Async) Utilizes Spring Boot’s asynchronous processing to ensure the user interface remains responsive while the email is being dispatched.

🧠 Key Engineering Challenges
Cross-VPC Connectivity & Security
During the migration from a containerized local database to Amazon RDS, I encountered a SocketTimeoutException caused by VPC isolation. I successfully diagnosed the network layer conflict and re-architected the infrastructure to align the EKS cluster and RDS instance within the same Virtual Private Cloud. I then implemented granular Security Group Ingress rules to facilitate a secure "handshake" between the application and data layers.

Zero-Downtime Deployments
I utilized Kubernetes Rolling Updates and kubectl rollout management to ensure that environment variable updates and code changes could be deployed without service interruption, maintaining a constant "Live" state for student users.

🔑 Access & Testing
To review the platform's functionality, please use the following credentials:

User Roles:
Admin Account:

Email: admin@junstudio.com

Password: admin123

Student Account: You may create a new account via the Sign-Up page.

💳 Stripe Checkout (Test Mode):
The payment system is integrated with Stripe in Test Mode.

Card Number: 4242 4242 4242 4242

Expiry: Any future date

CVC: 123