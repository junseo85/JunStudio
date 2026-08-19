# JunStudio — Full Stack Cello Instruction Platform

JunStudio is a production-ready web application designed for music educators to manage **students**, **scheduling**, **payments**, and now **large media uploads (5–20 GB)** for lesson content and ML analysis workflows.

This project represents my transition from 20+ years of professional performance/teaching experience into full-stack software engineering.

---

## 🚀 Live Production Environment

- **Domain:** `celloJun.com`
- **Architecture:** AWS EC2 (Docker Compose) + RDS (MySQL) + Cloudflare  
  *(legacy branch/history includes AWS EKS/Fargate architecture)*

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
- Vanilla JS upload module (chunked/resumable uploader)

### Database
- Amazon RDS (MySQL)

### Cloud & DevOps
- AWS EC2
- Docker / Docker Compose
- Linux (Amazon Linux 2023)
- Cloudflare DNS + SSL/TLS

### Integrations
- **Payments:** Stripe API (Test Mode Integration)
- **Email:** Java Mail Sender (SMTP) for booking confirmations and user notifications
- **Storage:** S3-compatible object storage with multipart uploads (pre-signed URLs)

---

## 💎 Core Features

- **Student Dashboard:** Real-time view of upcoming cello lessons and practice materials
- **Automated Scheduling:** Synchronized booking flow with instant notifications
- **Secure Payments:** Stripe checkout for lesson packages with digital receipting
- **Teacher Admin Panel:** Centralized student/lesson/revenue management
- **Large File Uploads:** Resumable, chunked uploads for 5–20 GB media files
- **Upload Safety Pipeline:** Session validation, idempotent part updates, completeness checks, and stale-session cleanup

---

## 📦 New: Large File Upload System (5–20 GB)

JunStudio now supports **large media ingestion** without degrading page or API performance.

### Why this architecture?
Uploading 20GB files through the app server can cause memory pressure, request timeouts, and unstable UX.  
To prevent this, JunStudio uses **direct-to-storage multipart uploads**.

### Upload flow
1. Client creates an upload session via Spring Boot API
2. Server initializes multipart upload and returns session metadata
3. Client requests pre-signed URLs for part numbers
4. Browser uploads chunks directly to storage (parallel + retry/backoff)
5. Client registers uploaded part metadata (`etag`, size)
6. Server validates parts and finalizes multipart upload
7. Media metadata is persisted and marked for async processing

### Implemented hardening
- Part number validation (`1..totalParts`)
- Session state guards (`INIT/UPLOADING` only)
- Session expiration checks
- Idempotent part registration (safe retries)
- Finalize completeness verification (all parts present)
- Terminal-state protection (`COMPLETED`, `ABORTED`, `EXPIRED`)
- Scheduled cleanup for expired/stale multipart sessions
- Authenticated upload ownership via Spring Security principal

---

## 🏗️ Infrastructure & Architecture

The platform is built for production security, resilience, and cost-efficiency:

- **Application Runtime:** Containerized Spring Boot + frontend deployment on EC2
- **Data Persistence:** Managed Amazon RDS in private VPC boundaries
- **Edge Security:** Cloudflare proxy + SSL/TLS for secure public access
- **Credential Safety:** Runtime secret/config injection (no hardcoded credentials)
- **Object Storage Offload:** Large file payloads bypass app-server memory path

---

## 📧 Automated Communication System

JunStudio includes an automated notification system to keep teachers and students synchronized:

- **Trigger-Based Notifications:** Sends formatted emails upon registration, booking, and payment events
- **Credential Security:** Secrets managed outside source-controlled code
- **Async-Friendly Design:** Notification workloads can run asynchronously to preserve API responsiveness

---

## 🚀 Key Engineering Challenges & Solutions

### 1) Infrastructure Migration (EKS → EC2)
- **Problem:** EKS/Fargate overhead was too expensive for solo project economics.
- **Solution:** Migrated to EC2 + Docker Compose, reducing monthly infrastructure cost by **~95%** while preserving production-grade service delivery.

### 2) JVM Stability on Resource-Constrained Host
- **Problem:** Container memory contention on small instance sizes.
- **Solution:** Linux swap + JVM tuning stabilized memory-intensive startup/runtime behavior.

### 3) Large Upload Reliability & Performance
- **Problem:** Very large uploads risk timeout/restart and can degrade site performance.
- **Solution:** Implemented resumable multipart direct-upload architecture with part-level retry, idempotent updates, and finalize integrity checks.

---

## 🔑 Access & Testing

To review the platform functionality, use:

### User Roles

**Test Teacher Account**
- **Email:** `test@junstudio.com`
- **Password:** `test123`

**Student Account**
- Register via Sign-Up page.

---

## 💳 Stripe Checkout (Test Mode)

- **Card Number:** `4242 4242 4242 4242`
- **Expiry:** Any future date
- **CVC:** `123`

---

## 📂 Representative Upload Module Structure

```text
src/main/java/com/junstudio/upload/
  api/UploadController.java
  dto/UploadDtos.java
  service/UploadService.java
  service/UploadServiceImpl.java
  storage/StorageMultipartService.java
  storage/S3MultipartStorageService.java
  scheduler/UploadCleanupScheduler.java
  security/UploadAuthHelper.java

src/main/resources/db/migration/
  V1__large_upload_tables.sql

src/main/resources/static/
  upload.html
  js/uploader.js
```

---

## 📦 Local & Deployment Setup

### Prerequisites
- Docker & Docker Compose
- Java 17
- MySQL / RDS access
- S3-compatible bucket credentials
- Stripe + SMTP secrets

### Example run
```bash
git clone https://github.com/junseo85/JunStudio.git
cd JunStudio
docker-compose up -d --build
```

---

## 🧭 Notes

- Legacy architecture/history references to EKS remain for portfolio context.
- Active deployment strategy is cost-optimized EC2 container orchestration.
- Upload subsystem is designed for future ML/transcoding queue integration.