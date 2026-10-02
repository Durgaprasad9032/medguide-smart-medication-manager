package com.medguide.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response structure returned across all MedGuide API error scenarios.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean success;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Instant timestamp;
    private final Map<String, String> validationErrors;

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this.success = false;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
        this.validationErrors = validationErrors;
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, null);
    }

    public static ErrorResponse withValidation(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ErrorResponse(status, error, message, path, validationErrors);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
