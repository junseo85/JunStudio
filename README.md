# JunStudio — Full Stack Cello Instruction Platform

JunStudio is a production-ready web application designed for music educators to manage **students**, **scheduling**, **payments**, and now **large media uploads (5–20 GB)** for lesson content and ML analysis workflows.

This project represents my transition from 20+ years of professional performance/teaching experience into full-stack software engineering.

---

## 🚀 Live Production Environment

- **Domain:** `celloJun.com`
- **Architecture:** AWS EC2 (Docker Compose) + containerized MySQL + Cloudflare
  *(an alternative RDS configuration is available)*
  *(legacy branch/history includes AWS EKS/Fargate architecture)*

---

## 🛠️ Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security (session authentication and role-based access)
- Hibernate / JPA

### Frontend
- Thymeleaf templates
- JavaScript and CSS
- Vanilla JS upload module (chunked/resumable uploader)

### Database
- MySQL (Compose container or external RDS)

### Cloud & DevOps
- AWS EC2
- Docker / Docker Compose
- Linux (Amazon Linux 2023)
- Cloudflare DNS + SSL/TLS

### Practice Coach (instant practice feedback)
Logged-in students can load sheet music, record or upload their playing, and get note-by-note pitch/rhythm feedback at `/practice-coach`. The Spring Boot controller proxies requests to the separate, private [practice_helper](https://github.com/junseo85/practice_helper) Python service with login and CSRF protection. Compose connects the services using `PRACTICE_HELPER_URL`; outside Compose the default is `http://localhost:5000`. If the service is down, the page reports that it is unavailable.

The default helper image supports audio analysis and saved score JSON. Image/PDF scanning requires the optional OMR build below. The first scan downloads additional software and model checkpoints and can take several minutes; resource-constrained instances may struggle. OMR is intended for single-staff, single-melody music, and its results should be checked for accuracy.

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
- **Data Persistence:** MySQL in a persistent Compose volume; an external RDS configuration is also available
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
- **Solution:** Migrated to EC2 + Docker Compose, reducing monthly hosting cost from approximately $150 to $25 (about 83%).

### 2) JVM Stability on Resource-Constrained Host
- **Problem:** Container memory contention on small instance sizes.
- **Solution:** Linux swap + JVM tuning stabilized memory-intensive startup/runtime behavior.

### 3) Large Upload Reliability & Performance
- **Problem:** Very large uploads risk timeout/restart and can degrade site performance.
- **Solution:** Implemented resumable multipart direct-upload architecture with part-level retry, idempotent updates, and finalize integrity checks.

---

## 🔑 Access & Testing

Student accounts can be created through the Sign-Up page. Teacher and admin
accounts must be provisioned securely; no default admin is created.

---

## 💳 Stripe Checkout (Test Mode)

- **Card Number:** `4242 4242 4242 4242`
- **Expiry:** Any future date
- **CVC:** `123`

---

## 📂 Representative Upload Module Structure

```text
src/main/java/com/music/JunStudio/upload/
  api/UploadController.java
  dto/UploadDtos.java
  service/UploadService.java
  service/UploadServiceImpl.java
  storage/StorageMultipartService.java
  storage/S3MultipartStorageService.java
  scheduler/UploadCleanupScheduler.java
  security/UploadAuthHelper.java

src/main/resources/db/migration/
  V1_large_upload_tables.sql

src/main/resources/static/
  js/uploader.js
src/main/resources/templates/
  upload.html
```

---

## 📦 Local & Deployment Setup

### Prerequisites
- Docker & Docker Compose
- Java 17
- Access to the private `practice_helper` repository
- S3-compatible bucket credentials
- Stripe + SMTP secrets

### MySQL container deployment
Clone `practice_helper` on its `listen_js` branch alongside JunStudio (GitHub
authentication is required), and set `MYSQL_ROOT_PASSWORD`, `MAIL_USER`,
`MAIL_PASS`, and optionally `STRIPE_API_KEY` in a protected, untracked `.env`
file in the JunStudio directory. Docker Compose passes only variables declared
in the Compose file to the app; `STRIPE_KEY` in `.env` is not passed through
by the default configuration.

```bash
git clone -b EC2 https://github.com/junseo85/JunStudio.git
git clone -b listen_js git@github.com:junseo85/practice_helper.git
cd JunStudio
chmod 600 .env
docker compose up -d --build
```

For an existing deployment, back up the database first and use its current
MySQL root password. Changing `MYSQL_ROOT_PASSWORD` in `.env` does not change
the password in an existing volume. Check the existing Compose project and
volume names before deploying; never use `down -v` unless you intend to delete
the database volume.

The default app port is `8080`. To preserve a server's port 80 mapping and
enable optional sheet scanning, create a **server-local**, untracked override
file, for example `compose.ec2-local.yml`:

```yaml
services:
  app:
    ports: !override
      - "80:8080"
  practice-helper:
    build:
      context: ../practice_helper
      args:
        INSTALL_OMR: "true"
```

Use a Compose version supporting `!override` and Buildx 0.17 or newer. Depending
on the server, the command may be `docker-compose` rather than `docker compose`.
Do not commit the local override or credentials. Validate and build before
restarting the app and helper without recreating MySQL:

```bash
docker compose -f docker-compose.yml -f compose.ec2-local.yml config --quiet
docker compose -f docker-compose.yml -f compose.ec2-local.yml build
docker compose -f docker-compose.yml -f compose.ec2-local.yml up -d --no-build --no-deps practice-helper app
docker compose -f docker-compose.yml -f compose.ec2-local.yml ps
curl -s -o /dev/null -w "HTTP status: %{http_code}\n" http://localhost/
```

The current MySQL container may have different port bindings from the default
Compose file until it is recreated. Restrict database access in the EC2 security
group and inspect the actual container bindings; avoid exposing port 3306
publicly. Rebuilding the helper may be needed after updating its source. Its
non-root container user needs read access to copied application files.

### EC2 deployment with RDS
Use `docker-compose.rds.yml` instead of the default Compose file. Place
`DB_URL` (a MySQL JDBC URL), `DB_USER`, `DB_PASS`, `MAIL_USER`, `MAIL_PASS`,
and `STRIPE_API_KEY` in the protected, untracked `.env` file. The driver
defaults to `com.mysql.cj.jdbc.Driver`; `DB_DRIVER_CLASS_NAME` or the legacy
`DB_DRIVERCLASS_NAME` can override it. The `.env` file is passed to the app
container, including any other settings needed by the deployment.

Clone the `listen_js` branch of `practice_helper` alongside this repository
before building, and ensure
the EC2 instance can reach RDS. Back up the database before deploying: the app
uses Hibernate schema updates. From the JunStudio directory, run:

```bash
docker compose -f docker-compose.rds.yml up -d --build
docker compose -f docker-compose.rds.yml logs --tail=100 app
```

The RDS configuration does not start or delete a local MySQL container or volume
and does not migrate its data. Switching databases requires a separate migration
and verification. Do not use `--remove-orphans` without first checking what it
would remove.

---

## 🧭 Notes

- Legacy architecture/history references to EKS remain for portfolio context.
- Active deployment strategy is cost-optimized EC2 container orchestration.
- Upload subsystem is designed for future ML/transcoding queue integration.

### Credential remediation for existing deployments

- Restrict public access until the known default admin and documented test
  teacher accounts have been disabled or securely reset in the live database.
  Reset all accounts created with the former shared default password as well.
  Removing the seeder and shared default does not change existing records.
- Rotate any exposed database, Stripe, mail, AWS, and other credentials at
  their providers. Update the deployed secrets and inspect account access logs.
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
