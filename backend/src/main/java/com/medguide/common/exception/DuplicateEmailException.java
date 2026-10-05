package com.medguide.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when attempting to register an account with an email that is already registered.
 */
public class DuplicateEmailException extends ApiException {

    public DuplicateEmailException(String email) {
        super(String.format("An account with email '%s' already exists", email), HttpStatus.CONFLICT);
    }
}
