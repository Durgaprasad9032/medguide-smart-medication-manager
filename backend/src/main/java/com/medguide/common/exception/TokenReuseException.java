package com.medguide.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a previously revoked refresh token is presented again, indicating potential token theft or replay.
 */
public class TokenReuseException extends ApiException {

    public TokenReuseException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
