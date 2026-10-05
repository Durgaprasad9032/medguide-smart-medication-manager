package com.medguide.modules.user.domain;

/**
 * Account lifecycle status states.
 * - ACTIVE: User is active and permitted to authenticate.
 * - PENDING: Account registration is pending activation or practitioner verification.
 *            Permitted to authenticate to view registration status/profile.
 * - SUSPENDED: Administrative restriction. Authentication is strictly rejected.
 * - DEACTIVATED: Account closed. Authentication is strictly rejected.
 */
public enum UserStatus {
    ACTIVE,
    PENDING,
    SUSPENDED,
    DEACTIVATED;

    public boolean canAuthenticate() {
        return this == ACTIVE || this == PENDING;
    }
}
