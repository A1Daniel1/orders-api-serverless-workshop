package com.example.orders.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.exception.ValidationException;
import com.example.orders.model.GetOrdersByCustomerRequest;
import com.example.orders.model.Order;
import com.example.orders.repository.InMemoryOrderRepository;
import com.example.orders.service.OrderService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetOrdersByCustomerHandlerTest {

    private InMemoryOrderRepository repository;
    private GetOrdersByCustomerHandler handler;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
        handler = new GetOrdersByCustomerHandler(new OrderService(repository));
    }

    @Test
    void handleRequest_returnsOnlyMatchingOrders() {
        repository.save(new Order("ORD-001", "CUST-42", "MacBook Air", 1));
        repository.save(new Order("ORD-002", "CUST-77", "iPad", 1));
        repository.save(new Order("ORD-003", "CUST-42", "iPhone", 2));

        List<Order> result = handler.handleRequest(new GetOrdersByCustomerRequest("CUST-42"), null);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getCustomerId().equals("CUST-42")));
    }

    @Test
    void handleRequest_noOrders_returnsEmptyList() {
        List<Order> result = handler.handleRequest(new GetOrdersByCustomerRequest("CUST-42"), null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void handleRequest_blankCustomerId_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> handler.handleRequest(new GetOrdersByCustomerRequest(" "), null));
    }
}
