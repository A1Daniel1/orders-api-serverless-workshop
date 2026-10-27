package com.example.orders.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.orders.exception.ValidationException;
import com.example.orders.model.CreateOrderRequest;
import com.example.orders.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateOrderHandlerTest {

    private CreateOrderHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateOrderHandler();
    }

    @Test
    void handleRequest_createsOrderWithGeneratedId() {
        CreateOrderRequest input = new CreateOrderRequest("CUST-42", "MacBook Air", 1);

        Order order = handler.handleRequest(input, null);

        assertNotNull(order.getId());
        assertEquals("CUST-42", order.getCustomerId());
        assertEquals("MacBook Air", order.getProduct());
        assertEquals(1, order.getQuantity());
    }

    @Test
    void handleRequest_reusesSameServiceAcrossInvocations() {
        Order first = handler.handleRequest(new CreateOrderRequest("CUST-42", "MacBook Air", 1), null);
        Order second = handler.handleRequest(new CreateOrderRequest("CUST-42", "iPhone", 2), null);

        assertEquals("ORD-001", first.getId());
        assertEquals("ORD-002", second.getId());
    }

    @Test
    void handleRequest_invalidInput_throwsValidationException() {
        CreateOrderRequest input = new CreateOrderRequest("CUST-42", "", 1);

        assertThrows(ValidationException.class, () -> handler.handleRequest(input, null));
    }
}
