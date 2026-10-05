package com.medguide.modules.user.domain;

/**
 * Standard application roles for MedGuide role-based authorization.
 */
public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
