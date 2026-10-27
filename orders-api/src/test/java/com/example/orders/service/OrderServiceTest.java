package com.example.orders.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.exception.OrderNotFoundException;
import com.example.orders.exception.ValidationException;
import com.example.orders.model.CreateOrderRequest;
import com.example.orders.model.Order;
import com.example.orders.repository.InMemoryOrderRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(new InMemoryOrderRepository());
    }

    @Test
    void createOrder_returnsOrderWithGeneratedIdAndInputFields() {
        Order order = service.createOrder(request("CUST-42", "MacBook Air", 1));

        assertNotNull(order.getId());
        assertEquals("CUST-42", order.getCustomerId());
        assertEquals("MacBook Air", order.getProduct());
        assertEquals(1, order.getQuantity());
    }

    @Test
    void createOrder_generatesSequentialIds() {
        Order first = service.createOrder(request("CUST-42", "MacBook Air", 1));
        Order second = service.createOrder(request("CUST-42", "iPhone", 2));

        assertEquals("ORD-001", first.getId());
        assertEquals("ORD-002", second.getId());
    }

    @Test
    void createOrder_persistsTheOrder() {
        Order created = service.createOrder(request("CUST-42", "MacBook Air", 1));

        assertEquals(created, service.getOrder(created.getId()));
    }

    @Test
    void createOrder_nullRequest_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.createOrder(null));
    }

    @Test
    void createOrder_missingCustomerId_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.createOrder(request(null, "MacBook Air", 1)));
    }

    @Test
    void createOrder_blankCustomerId_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.createOrder(request("  ", "MacBook Air", 1)));
    }

    @Test
    void createOrder_malformedCustomerId_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.createOrder(request("CUST-XX", "MacBook Air", 1)));
    }

    @Test
    void createOrder_missingProduct_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.createOrder(request("CUST-42", null, 1)));
    }

    @Test
    void createOrder_blankProduct_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.createOrder(request("CUST-42", "", 1)));
    }

    @Test
    void createOrder_zeroQuantity_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.createOrder(request("CUST-42", "MacBook Air", 0)));
    }

    @Test
    void createOrder_negativeQuantity_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.createOrder(request("CUST-42", "MacBook Air", -1)));
    }

    @Test
    void getOrder_unknownId_throwsOrderNotFoundException() {
        assertThrows(OrderNotFoundException.class, () -> service.getOrder("ORD-999"));
    }

    @Test
    void getOrder_blankId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.getOrder(" "));
    }

    @Test
    void getOrdersByCustomer_returnsOnlyMatchingOrders() {
        service.createOrder(request("CUST-42", "MacBook Air", 1));
        service.createOrder(request("CUST-42", "iPhone", 2));
        service.createOrder(request("CUST-77", "iPad", 1));

        List<Order> result = service.getOrdersByCustomer("CUST-42");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getCustomerId().equals("CUST-42")));
    }

    @Test
    void getOrdersByCustomer_noOrders_returnsEmptyList() {
        List<Order> result = service.getOrdersByCustomer("CUST-42");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getOrdersByCustomer_blankCustomerId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.getOrdersByCustomer(" "));
    }

    @Test
    void deleteOrder_existingOrder_removesIt() {
        service.createOrder(request("CUST-42", "MacBook Air", 1));

        service.deleteOrder("ORD-001");

        assertThrows(OrderNotFoundException.class, () -> service.getOrder("ORD-001"));
    }

    @Test
    void deleteOrder_unknownOrder_throwsOrderNotFoundException() {
        assertThrows(OrderNotFoundException.class, () -> service.deleteOrder("ORD-999"));
    }

    private static CreateOrderRequest request(String customerId, String product, int quantity) {
        return new CreateOrderRequest(customerId, product, quantity);
    }
}
