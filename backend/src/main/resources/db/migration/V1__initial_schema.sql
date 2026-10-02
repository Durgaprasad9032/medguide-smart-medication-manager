-- ==============================================================================
-- MedGuide Database Schema - Migration V1
-- Target: MySQL 8+
-- Character Set: utf8mb4, Collation: utf8mb4_unicode_ci
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. USERS TABLE
-- Core identity and authentication accounts for PATIENT, DOCTOR, and ADMIN roles.
-- ------------------------------------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('PATIENT', 'DOCTOR', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'PENDING', 'SUSPENDED', 'DEACTIVATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- ------------------------------------------------------------------------------
-- 2. PATIENTS TABLE
-- Patient clinical profile, demographic information, and medical background.
-- ------------------------------------------------------------------------------
CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    date_of_birth DATE NULL,
    gender VARCHAR(20) NULL,
    phone_number VARCHAR(30) NULL,
    allergies TEXT NULL,
    chronic_conditions TEXT NULL,
    emergency_contact_name VARCHAR(150) NULL,
    emergency_contact_phone VARCHAR(30) NULL,
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_patients_user_id UNIQUE (user_id),
    CONSTRAINT fk_patients_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_patients_language CHECK (preferred_language IN ('en', 'te'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_patients_user_id ON patients(user_id);
CREATE INDEX idx_patients_preferred_language ON patients(preferred_language);

-- ------------------------------------------------------------------------------
-- 3. DOCTORS TABLE
-- Medical practitioner profiles, credentials, and verification lifecycle.
-- ------------------------------------------------------------------------------
CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    qualification VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NULL,
    clinic_hospital_name VARCHAR(200) NULL,
    clinic_address TEXT NULL,
    contact_number VARCHAR(30) NULL,
    doctor_code VARCHAR(30) NOT NULL,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    verification_documents_path VARCHAR(500) NULL,
    verification_notes TEXT NULL,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_doctors_user_id UNIQUE (user_id),
    CONSTRAINT uk_doctors_registration_number UNIQUE (registration_number),
    CONSTRAINT uk_doctors_doctor_code UNIQUE (doctor_code),
    CONSTRAINT fk_doctors_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_doctors_verification_status CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_doctors_user_id ON doctors(user_id);
CREATE INDEX idx_doctors_doctor_code ON doctors(doctor_code);
CREATE INDEX idx_doctors_verification_status ON doctors(verification_status);
CREATE INDEX idx_doctors_registration_number ON doctors(registration_number);

-- ------------------------------------------------------------------------------
-- 4. MEDICINES TABLE
-- Master medicine catalog with generic names, categories, and safety guidance.
-- ------------------------------------------------------------------------------
CREATE TABLE medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    generic_name VARCHAR(200) NOT NULL,
    brand_names TEXT NULL,
    category VARCHAR(100) NULL,
    strength VARCHAR(100) NULL,
    dosage_form VARCHAR(50) NULL,
    common_uses TEXT NULL,
    standard_instructions TEXT NULL,
    warnings TEXT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_medicines_name ON medicines(name);
CREATE INDEX idx_medicines_generic_name ON medicines(generic_name);
CREATE INDEX idx_medicines_category ON medicines(category);
CREATE INDEX idx_medicines_active ON medicines(active);

-- ------------------------------------------------------------------------------
-- 5. MEDICINE_LOCALIZATIONS TABLE
-- Localized medicine information for multilingual support (English & Telugu).
-- ------------------------------------------------------------------------------
CREATE TABLE medicine_localizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id BIGINT NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    localized_name VARCHAR(200) NOT NULL,
    localized_common_uses TEXT NULL,
    localized_how_to_take TEXT NULL,
    localized_warnings TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_medicine_localizations_pair UNIQUE (medicine_id, language_code),
    CONSTRAINT fk_med_localizations_medicine_id FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_med_localizations_language CHECK (language_code IN ('en', 'te'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_medicine_localizations_lookup ON medicine_localizations(medicine_id, language_code);

-- ------------------------------------------------------------------------------
-- 6. PRESCRIPTIONS TABLE
-- Uploaded prescription scans and doctor digital prescriptions with OCR/AI metadata.
-- ------------------------------------------------------------------------------
CREATE TABLE prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NULL,
    source VARCHAR(50) NOT NULL,
    image_storage_path VARCHAR(500) NULL,
    ocr_raw_text MEDIUMTEXT NULL,
    ai_extracted_json JSON NULL,
    ai_confidence_score DECIMAL(5, 4) NULL,
    notes TEXT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescriptions_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prescriptions_doctor_id FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_prescriptions_source CHECK (source IN ('UPLOADED_SCAN', 'DOCTOR_EPRESCRIPTION')),
    CONSTRAINT chk_prescriptions_status CHECK (status IN ('DRAFT', 'SENT', 'PENDING_REVIEW', 'CONFIRMED', 'FLAGGED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_prescriptions_patient_id ON prescriptions(patient_id);
CREATE INDEX idx_prescriptions_doctor_id ON prescriptions(doctor_id);
CREATE INDEX idx_prescriptions_source ON prescriptions(source);
CREATE INDEX idx_prescriptions_status ON prescriptions(status);
CREATE INDEX idx_prescriptions_created_at ON prescriptions(created_at);

-- ------------------------------------------------------------------------------
-- 7. PATIENT_MEDICATIONS TABLE
-- Active and historical medications scheduled for individual patients.
-- ------------------------------------------------------------------------------
CREATE TABLE patient_medications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    medicine_id BIGINT NULL,
    prescription_id BIGINT NULL,
    custom_medicine_name VARCHAR(200) NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    route VARCHAR(50) NULL,
    meal_timing VARCHAR(50) NULL,
    scheduled_times JSON NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    instructions TEXT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    source VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_patient_medications_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_patient_medications_medicine_id FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_patient_medications_prescription_id FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_patient_medications_status CHECK (status IN ('ACTIVE', 'PAUSED', 'STOPPED', 'COMPLETED')),
    CONSTRAINT chk_patient_medications_source CHECK (source IN ('MANUAL', 'PRESCRIPTION_SCAN', 'DOCTOR_EPRESCRIPTION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_patient_medications_patient_id ON patient_medications(patient_id);
CREATE INDEX idx_patient_medications_medicine_id ON patient_medications(medicine_id);
CREATE INDEX idx_patient_medications_prescription_id ON patient_medications(prescription_id);
CREATE INDEX idx_patient_medications_status ON patient_medications(status);

-- ------------------------------------------------------------------------------
-- 8. MEDICATION_LOGS TABLE
-- Event history of individual scheduled dose actions (Taken, Skipped, Snoozed, Missed).
-- ------------------------------------------------------------------------------
CREATE TABLE medication_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_medication_id BIGINT NOT NULL,
    scheduled_time DATETIME NOT NULL,
    action_time DATETIME NULL,
    status VARCHAR(50) NOT NULL,
    snooze_until DATETIME NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_medication_logs_dose UNIQUE (patient_medication_id, scheduled_time),
    CONSTRAINT fk_medication_logs_med_id FOREIGN KEY (patient_medication_id) REFERENCES patient_medications(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_medication_logs_status CHECK (status IN ('TAKEN', 'SKIPPED', 'MISSED', 'SNOOZED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_medication_logs_med_id ON medication_logs(patient_medication_id);
CREATE INDEX idx_medication_logs_scheduled_time ON medication_logs(scheduled_time);
CREATE INDEX idx_medication_logs_status ON medication_logs(status);

-- ------------------------------------------------------------------------------
-- 9. DOCTOR_PATIENT_LINKS TABLE
-- Explicit consented links established between patients and doctors.
-- ------------------------------------------------------------------------------
CREATE TABLE doctor_patient_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    invitation_code VARCHAR(50) NULL,
    linked_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_doctor_patient_link UNIQUE (doctor_id, patient_id),
    CONSTRAINT fk_doc_pat_links_doctor_id FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_doc_pat_links_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_doc_pat_links_status CHECK (status IN ('PENDING', 'ACTIVE', 'REVOKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_doc_pat_links_doctor_id ON doctor_patient_links(doctor_id);
CREATE INDEX idx_doc_pat_links_patient_id ON doctor_patient_links(patient_id);
CREATE INDEX idx_doc_pat_links_status ON doctor_patient_links(status);

-- ------------------------------------------------------------------------------
-- 10. REFRESH_TOKENS TABLE
-- Persisted cryptographic hashes of refresh tokens for secure token rotation.
-- ------------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    replaced_by_token_hash VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- ------------------------------------------------------------------------------
-- 11. DEVICE_TOKENS TABLE
-- FCM / Push notification registration tokens for active user devices.
-- ------------------------------------------------------------------------------
CREATE TABLE device_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_device_tokens_user_token UNIQUE (user_id, token(255)),
    CONSTRAINT fk_device_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_device_tokens_platform CHECK (platform IN ('ANDROID', 'IOS', 'WEB'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);
CREATE INDEX idx_device_tokens_active ON device_tokens(active);

-- ------------------------------------------------------------------------------
-- 12. ADMIN_AUDIT_LOGS TABLE
-- Immutable record of administrative operations and critical medical reviews.
-- ------------------------------------------------------------------------------
CREATE TABLE admin_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NULL,
    details JSON NULL,
    ip_address VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_audit_logs_actor_id FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_admin_audit_logs_actor ON admin_audit_logs(actor_user_id);
CREATE INDEX idx_admin_audit_logs_action ON admin_audit_logs(action);
CREATE INDEX idx_admin_audit_logs_entity ON admin_audit_logs(entity_type, entity_id);
CREATE INDEX idx_admin_audit_logs_created_at ON admin_audit_logs(created_at);
