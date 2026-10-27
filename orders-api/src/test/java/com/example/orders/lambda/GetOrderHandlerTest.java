package com.example.orders.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.orders.exception.OrderNotFoundException;
import com.example.orders.exception.ValidationException;
import com.example.orders.model.GetOrderRequest;
import com.example.orders.model.Order;
import com.example.orders.repository.InMemoryOrderRepository;
import com.example.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetOrderHandlerTest {

    private InMemoryOrderRepository repository;
    private GetOrderHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
        handler = new GetOrderHandler(new OrderService(repository));
    }

    @Test
    void handleRequest_returnsExistingOrder() {
        Order order = new Order("ORD-001", "CUST-42", "MacBook Air", 1);
        repository.save(order);

        Order result = handler.handleRequest(new GetOrderRequest("ORD-001"), null);

        assertEquals(order, result);
    }

    @Test
    void handleRequest_unknownId_throwsOrderNotFoundException() {
        assertThrows(OrderNotFoundException.class,
                () -> handler.handleRequest(new GetOrderRequest("ORD-999"), null));
    }

    @Test
    void handleRequest_blankId_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> handler.handleRequest(new GetOrderRequest(" "), null));
    }
}
