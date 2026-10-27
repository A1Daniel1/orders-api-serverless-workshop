package com.example.orders.exception;

/**
 * Thrown when a request contains invalid input (missing/blank fields,
 * malformed identifiers, non-positive quantity, etc.).
 *
 * <p>Plain Java: carries only a human-readable message. Mapping to an HTTP
 * status/code (e.g. {@code 400 INVALID_INPUT}) is a later adapter concern.</p>
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
