# JunStudio 🎻

JunStudio is a comprehensive, multi-tenant scheduling and management platform engineered specifically for the complex operational needs of individual music instructors and multi-teacher music studios.

Built with **Java and Spring Boot**, the application handles the complete studio lifecycle: from student registration and Stripe payment processing, to algorithmic semester scheduling, automated email/PDF receipts, and live data reporting.

## 🚀 Key Features

* **Role-Based Access Control (RBAC):** Distinct dashboards and permission levels for Admins, Teachers, and Students.
* **Smart Semester Scheduling Engine:** Students register for preferred days; upon teacher approval, the backend algorithmically generates a full 16-week semester, intelligently skipping global holidays and teacher-specific blocked dates.
* **Dynamic Availability System:** Real-time schedule validation that prevents double-booking and respects both studio-wide closures and individual teacher time-off overrides.
* **Credit-Based Booking & Billing:** Integrated payment flow where students purchase lesson credits. Includes automated generation of downloadable PDF receipts and email confirmations.
* **Practice Video Platform:** A dedicated media gallery allowing students to upload performance videos for asynchronous instructor feedback.
* **Live Analytics Dashboard:** Interactive data visualization (via Chart.js) allowing admins to drill down into studio activity, pending requests, and cancellation rates by individual users.

## 💻 Tech Stack

**Backend:**
* Java 17+
* Spring Boot (Web, Data JPA, Security)
* Hibernate / MySQL
* iText / Lowagie (Dynamic PDF Generation)
* JavaMailSender (Automated SMTP Emailing)

**Frontend:**
* HTML5 / CSS3
* Thymeleaf (Server-side templating)
* Bootstrap 5 (Responsive UI)
* JavaScript & Chart.js (Data visualization)
### Continuous Integration (CI/CD Pipeline)
To maintain code quality and prevent regressions, this project utilizes **GitHub Actions** for automated continuous integration.

On every push and pull request to the `main` branch, a cloud runner is spun up to:
1. Provision a clean Ubuntu environment with JDK 17.
2. Resolve and cache all Maven dependencies.
3. Automatically compile the Spring Boot application and execute the test suite.
   This guarantees that only stable, compiling code is ever merged into the production branch.

## 🧠 Engineering Highlights

### The "Smart Loop" Schedule Generator
Generating a 16-week semester isn't as simple as adding 7 days 16 times. The `WebController` utilizes `java.time.temporal.TemporalAdjusters` and a custom `while` loop to validate every future date against a `ScheduleOverride` database table. If a generated date lands on a studio holiday or a teacher's scheduled time off, the algorithm automatically skips that week and extends the semester to ensure the student receives their exact allotment of lessons.

### Multi-Tenant Database Architecture
Transitioned the database from a single-instructor model to a scalable multi-tenant system. The `User` model includes an `assignedTeacher` relationship, and the `ScheduleOverride` model differentiates between global studio closures (`teacher_id = null`) and individual instructor sick days, preventing one instructor's time off from locking the entire studio calendar.

### Seamless Server-Side to Client-Side Data Handoff
Leveraged Thymeleaf's natural templates to securely pass grouped database metrics (calculated via Java Streams) directly into JavaScript JSON objects. This allows the Chart.js dashboard to dynamically swap between Donut and Bar charts based on user drill-down selections without requiring additional REST API calls.

## 🛠️ Local Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/junseo85/JunStudio.git](https://github.com/junseo85/JunStudio.git)