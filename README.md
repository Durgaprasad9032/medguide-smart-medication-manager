# MedGuide — Multilingual Smart Medication Manager

**MedGuide** is an intelligent, full-stack medication management and adherence platform designed to help patients safely manage their prescriptions, dosage schedules, reminders, and medical history with multilingual support (English & Telugu).

---

## System Overview

1. **Flutter Patient Mobile Application:** Mobile client for patients to manage medications, log doses (Taken, Skipped, Snoozed), view adherence reports, and scan prescriptions.
2. **Flutter Doctor Mobile Application:** Dedicated portal for verified doctors to view linked patient adherence, manage clinical treatments, and issue digital prescriptions.
3. **Flutter Web Admin Application:** Administrative control panel for user management, doctor verification audits, medicine catalog maintenance, and AI/OCR oversight.
4. **Java Spring Boot Backend:** High-performance REST API service built with Java 21, Spring Boot 3, Spring Security, and Spring Data JPA.
5. **MySQL Database:** Relational data store with strictly versioned schema migrations managed via Flyway.
6. **AI/OCR Prescription Assistance:** Automated optical character recognition and structured AI extraction pipeline for physical prescription processing with patient review gates.
7. **Multilingual Architecture:** Native support for English and Telugu UI localization and medicine information.

---

## Repository Structure

```
medguide-smart-medication-manager/
├── backend/                             # Java 21 Spring Boot REST API
├── clients/                             # Client applications (Phase 4+)
│   ├── patient_app/                     # Flutter Patient Mobile App
│   ├── doctor_app/                      # Flutter Doctor Mobile App
│   └── admin_web/                       # Flutter Web Admin Dashboard
├── docs/                                # Architecture & Technical Documentation
├── README.md                            # Project Overview & Quickstart
└── .gitignore
```

---

## Development Milestones

- [x] **Phase 0:** Repository Inspection, Environment Assessment, and Architecture Planning
- [x] **Phase 1:** Spring Boot Backend Foundation (Java 21, Spring Boot 3.3.4, OpenAPI/Swagger, Health Endpoint, Centralized Exception Handling)
- [x] **Phase 2:** MySQL Database and Flyway Migrations (`medguide_db`, 12 core relational tables, versioned schema migration `V1__initial_schema.sql`, JPA schema validation)
- [x] **Phase 3:** Authentication, JWT, and Role-Based Authorization (BCrypt, JJWT 0.12.6, SHA-256 Refresh Token Rotation & Reuse Detection, RBAC with PATIENT, DOCTOR, ADMIN)
- [ ] **Phase 4:** Flutter Architecture and Client Foundations
