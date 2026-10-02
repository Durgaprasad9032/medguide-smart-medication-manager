# MedGuide Backend

Multilingual Smart Medication Manager — Spring Boot REST API Service.

## Technology Stack

- **Java:** 21 LTS
- **Framework:** Spring Boot 3.3.4
- **Build Tool:** Apache Maven 3.9+
- **Documentation:** Springdoc OpenAPI / Swagger v3
- **Monitoring:** Spring Boot Actuator
- **Security:** Spring Security (Foundational setup; full JWT & RBAC in Phase 3)
- **Database Layer:** Spring Data JPA & Flyway (Foundation included; MySQL integration in Phase 2)

---

## Current Status: Phase 1 — Backend Foundation

> **Note on Phase Boundaries:**
> - **Phase 1 (Current):** Foundational Spring Boot setup, health endpoints, centralized exception handling, standard API responses, CORS configuration, and Swagger/OpenAPI documentation.
> - **Phase 2 (Upcoming):** MySQL 8+ database connection, JPA entities, and Flyway schema migrations.
> - **Phase 3 (Upcoming):** JWT authentication, user registration/login, password hashing, and role-based access control (`PATIENT`, `DOCTOR`, `ADMIN`).

---

## Configuration

Configuration templates are defined in `.env.example`.

For local development, the application loads `src/main/resources/application.yml` and `src/main/resources/application-dev.yml`.

### Key Environment Variables (see `.env.example`)
- `SERVER_PORT`: Application HTTP port (default: `8080`)
- `SPRING_PROFILES_ACTIVE`: Active Spring profile (default: `dev`)
- `CORS_ALLOWED_ORIGINS`: Allowed origins for Flutter web & mobile clients

---

## Running the Application

### 1. Build and Run via Maven

To run locally using Maven:

```bash
cd backend
mvn spring-boot:run
```

Or build the executable jar and run:

```bash
mvn clean package
java -jar target/medguide-backend-0.0.1-SNAPSHOT.jar
```

### 2. Running Automated Tests

```bash
cd backend
mvn clean test
```

---

## Available Endpoints (Phase 1)

### 1. Base API Path
All application REST endpoints follow the prefix:
```
/api/v1/
```

### 2. Health Check
- **Endpoint:** `GET /api/v1/health`
- **Description:** Verifies that the MedGuide backend service is up and running.
- **Sample Response:**
  ```json
  {
    "status": "UP",
    "service": "MedGuide Backend"
  }
  ```

### 3. Swagger / OpenAPI 3 UI
Interactive API documentation is accessible in your browser at:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 4. Actuator Health
- **Actuator Health:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## Error Handling Foundation

All REST errors return a standardized JSON format via `GlobalExceptionHandler`:

```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/...",
  "timestamp": "2026-10-02T08:45:00.000Z",
  "validationErrors": {
    "field": "Validation message"
  }
}
```
Stack traces and internal database credentials are never exposed in API responses.
