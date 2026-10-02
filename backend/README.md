# MedGuide Backend

Multilingual Smart Medication Manager — Spring Boot REST API Service.

## Technology Stack

- **Java:** 21 LTS
- **Framework:** Spring Boot 3.3.4
- **Database:** MySQL 8+ (`utf8mb4` character set, `utf8mb4_unicode_ci` collation)
- **Database Migrations:** Flyway (`flyway-core`, `flyway-mysql`)
- **ORM / Persistence:** Spring Data JPA / Hibernate 6 (schema validation mode)
- **Build Tool:** Apache Maven 3.9+
- **Documentation:** Springdoc OpenAPI / Swagger v3
- **Monitoring:** Spring Boot Actuator
- **Security:** Spring Security (Foundational setup; full JWT & RBAC in Phase 3)

---

## Current Status: Phase 2 — MySQL Database & Flyway Migrations

> **Important Architecture & Database Rule:**
> Application tables, primary/foreign keys, indexes, and constraints are **strictly managed via Flyway versioned migrations** located at `src/main/resources/db/migration`.
> **DO NOT** manually create or alter application tables via MySQL Workbench or CLI. MySQL Workbench is used solely for initial database creation (`CREATE DATABASE medguide_db`), inspecting tables, and running diagnostic/audit queries.

---

## Database Setup & Configuration

### 1. Database Creation (One-time manual setup)
Run the following in MySQL Workbench or MySQL client once:
```sql
CREATE DATABASE medguide_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Environment Variables Configuration
Configure the connection through environment variables or a local `.env` file (copied from `.env.example`).

| Variable | Description | Default |
| :--- | :--- | :--- |
| `DB_HOST` | MySQL Server Hostname | `localhost` |
| `DB_PORT` | MySQL Server Port | `3306` |
| `DB_NAME` | Database Schema Name | `medguide_db` |
| `DB_USERNAME` | Database User | `root` |
| `DB_PASSWORD` | Database User Password | *(empty / prompt)* |
| `SERVER_PORT` | Backend HTTP Port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |

> **Security Warning:** Never commit `.env` or files containing real database passwords to version control.

---

## Flyway Migrations

Flyway automatically discovers and applies migration scripts on application startup.
- **Migration Location:** `src/main/resources/db/migration`
- **History Table:** `flyway_schema_history`
- **DDL Mode:** `spring.jpa.hibernate.ddl-auto=validate` (Hibernate validates matching entities and never overrides Flyway schema).

### Applied Migrations
- `V1__initial_schema.sql`: Initial MedGuide relational domain schema containing:
  1. `users` — Base authentication & role accounts (`PATIENT`, `DOCTOR`, `ADMIN`)
  2. `patients` — Patient medical background, allergies, language preference
  3. `doctors` — Medical practitioner credentials and verification status
  4. `medicines` — Central medicine catalog with generic names and warnings
  5. `medicine_localizations` — Multilingual catalog information (English, Telugu)
  6. `prescriptions` — Uploaded scans & digital prescriptions with OCR/AI metadata
  7. `patient_medications` — Patient active/paused medication regimens
  8. `medication_logs` — Scheduled dose event history (`TAKEN`, `SKIPPED`, `MISSED`, `SNOOZED`)
  9. `doctor_patient_links` — Consensual patient-doctor relationships
  10. `refresh_tokens` — Rotated token hashes for secure authentication
  11. `device_tokens` — FCM device registration tokens
  12. `admin_audit_logs` — Immutable audit log of administrative actions

---

## Running the Application

### 1. Run via Maven with Environment Variables
```powershell
$env:DB_PASSWORD="your_password"
mvn spring-boot:run
```

### 2. Run Tests
```powershell
$env:DB_PASSWORD="your_password"
mvn clean test
```

---

## Verifying Migrations in MySQL Workbench

Execute the following diagnostic queries in MySQL Workbench:

```sql
USE medguide_db;

-- 1. Verify all 12 domain tables + flyway_schema_history exist:
SHOW TABLES;

-- 2. Verify migration history:
SELECT installed_rank, version, description, type, script, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;

-- 3. Inspect table definitions:
DESCRIBE users;
DESCRIBE patients;
DESCRIBE doctors;
DESCRIBE medicines;
DESCRIBE prescriptions;
DESCRIBE patient_medications;
DESCRIBE medication_logs;
DESCRIBE doctor_patient_links;
DESCRIBE refresh_tokens;
DESCRIBE device_tokens;
DESCRIBE admin_audit_logs;
```

---

## Available Endpoints (Phase 2)

- **Health Check:** `GET http://localhost:8080/api/v1/health`
- **Actuator Health:** `GET http://localhost:8080/actuator/health`
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
