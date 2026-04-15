# JunStudio 🎻 : Multi-Tenant Studio Management SaaS

> **The Problem:** Multi-teacher music academies often rely on fragmented, manual tools for scheduling, billing, and time-off management.
> **The Solution:** A centralized, multi-tenant Spring Boot application that automates 16-week semester generation, prevents calendar collisions across concurrent instructors, and handles credit-based scheduling.

Built with **Java and Spring Boot**, this platform handles the complete studio lifecycle: from student registration and payment processing, to algorithmic scheduling, automated email/PDF receipts, and live data reporting.

## 🚀 Key Features
* **Role-Based Access Control (RBAC):** Distinct UI dashboards and data access tiers for Admins, Teachers, and Students.
* **Smart Semester Scheduling Engine:** Students register for preferred days; upon approval, the backend algorithmically generates a 16-week calendar, intelligently skipping global holidays and teacher-specific blocked dates.
* **Dynamic Availability System:** Real-time schedule validation that prevents double-booking and respects both studio-wide closures and individual teacher time-off overrides.
* **Credit-Based Booking:** Integrated flow where students utilize pre-purchased lesson credits to book sessions, triggering automated PDF receipts and email confirmations.
* **Live Analytics Dashboard:** Interactive data visualization (via Chart.js) allowing admins to drill down into studio activity, pending requests, and cancellation rates.

## 💻 Tech Stack
* **Backend:** Java 17+, Spring Boot (Web, Data JPA, Security), Hibernate, MySQL
* **Frontend:** HTML5, Bootstrap 5, Thymeleaf, Chart.js
* **DevOps & Tools:** Docker, GitHub Actions (CI/CD), iText (PDF Generation), JavaMailSender

---

## 🧠 Architectural Decisions & Optimizations

### 1. Query Optimization & Memory Management
Initially, dashboard reporting relied on pulling full database tables into application memory (`.findAll()`) and filtering via Java Streams. Recognizing the severe OutOfMemory (OOM) risk as the dataset scales, the data access layer was refactored. The application now utilizes **custom JPQL (Java Persistence Query Language) queries** to offload complex multi-tenant filtering (e.g., matching sub-queries for assigned teachers) directly to the MySQL database, significantly reducing server RAM utilization.

### 2. Algorithmic "Smart Loop" Scheduling
Generating a 16-week semester isn't as simple as adding 7 days 16 times. The `WebController` utilizes `java.time.temporal.TemporalAdjusters` and a custom `while` loop to validate every future date against a `ScheduleOverride` table. If a generated date lands on a studio holiday or a teacher's scheduled time off, the algorithm automatically skips that week and extends the semester to ensure the student receives their exact allotment of lessons.

### 3. Multi-Tenant Data Isolation
Transitioned the database from a single-instructor model to a scalable multi-tenant architecture. The `User` model establishes an `assignedTeacher` relationship, and the `ScheduleOverride` model differentiates between global studio closures (`teacher_id = null`) and individual instructor sick days. This prevents one instructor's time off from inadvertently locking the entire studio calendar.

### 4. Seamless Server-Side to Client-Side Data Handoff
Leveraged Thymeleaf's natural templates to securely pass grouped database metrics directly into JavaScript JSON objects. This allows the Chart.js dashboard to dynamically swap between Donut and Bar charts based on user drill-down selections without requiring additional REST API calls.

---

## ☁️ DevOps & Continuous Integration

* **Docker Containerization:** The application is containerized using a multi-stage `Dockerfile`. It builds the artifact using Maven and packages the runtime into an `eclipse-temurin:17-jre-alpine` image, ensuring a secure, ultra-lightweight production footprint.
* **CI/CD Pipeline:** Configured a **GitHub Actions** workflow that triggers on every push/PR to the `main` branch. A cloud runner provisions a clean Ubuntu JDK 17 environment, resolves dependencies, and automatically executes the test suite to prevent regressions from reaching production.

## 🛠️ Local Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/junseo85/JunStudio.git](https://github.com/junseo85/JunStudio.git)
   
2. **Configure Database:**
Create an application-dev.properties file in src/main/resources and add your local MySQL credentials.
3.  **Build the project:**
```bash
mvn clean install
```
4.  **Run the application:**
```bash
mvn spring-boot:run
```
