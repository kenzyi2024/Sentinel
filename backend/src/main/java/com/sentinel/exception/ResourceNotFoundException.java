package com.sentinel.exception;

/**
 * Thrown when a requested entity (run, scenario, policy, …) does not exist. Mapped to HTTP 404 by
 * {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String type, String id) {
        return new ResourceNotFoundException(type + " not found: " + id);
    }
}
