package com.example.orders.exception;

/**
 * Thrown when a well-formed request refers to an order that does not exist.
 *
 * <p>Plain Java: carries only a human-readable message. Mapping to an HTTP
 * status/code (e.g. {@code 404 ORDER_NOT_FOUND}) is a later adapter concern.</p>
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
