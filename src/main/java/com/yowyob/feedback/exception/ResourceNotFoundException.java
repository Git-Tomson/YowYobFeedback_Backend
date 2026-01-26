package com.yowyob.feedback.exception;

/**
 * Exception thrown when a requested resource is not found in the database.
 * Results in HTTP 404 Not Found response.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-24
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
