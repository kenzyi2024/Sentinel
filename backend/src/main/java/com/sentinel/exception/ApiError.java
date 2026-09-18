package com.sentinel.exception;

import java.time.Instant;
import java.util.Map;

/**
 * The uniform error body returned for every handled exception.
 *
 * @param timestamp   when the error occurred
 * @param status      HTTP status code
 * @param error       short status reason
 * @param message     human-readable detail
 * @param path        the request path
 * @param fieldErrors per-field validation messages (empty unless it was a validation failure)
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public ApiError {
        fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
    }
}
