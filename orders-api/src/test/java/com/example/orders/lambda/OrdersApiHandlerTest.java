package com.example.orders.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.exception.OrderNotFoundException;
import com.example.orders.exception.ValidationException;
import com.example.orders.model.Order;
import com.example.orders.model.OrdersApiRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrdersApiHandlerTest {

    private OrdersApiHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrdersApiHandler();
    }

    @Test
    void createOrder_returnsOrderWithGeneratedIdAndFields() {
        OrdersApiRequest req = req("createOrder");
        req.setCustomerId("CUST-42");
        req.setProduct("MacBook Air");
        req.setQuantity(1);

        Object result = handler.handleRequest(req, null);

        assertTrue(result instanceof Order);
        Order order = (Order) result;
        assertNotNull(order.getId());
        assertTrue(order.getId().startsWith("ORD-"));
        assertEquals("CUST-42", order.getCustomerId());
        assertEquals("MacBook Air", order.getProduct());
        assertEquals(1, order.getQuantity());
    }

    @Test
    void createOrder_missingQuantity_throwsValidationException() {
        OrdersApiRequest req = req("createOrder");
        req.setCustomerId("CUST-42");
        req.setProduct("MacBook Air");
        // quantity left null

        assertThrows(ValidationException.class, () -> handler.handleRequest(req, null));
    }

    @Test
    void createOrder_blankProduct_throwsValidationException() {
        OrdersApiRequest req = req("createOrder");
        req.setCustomerId("CUST-42");
        req.setProduct("");
        req.setQuantity(1);

        assertThrows(ValidationException.class, () -> handler.handleRequest(req, null));
    }

    @Test
    void getOrder_returnsSeededOrder() {
        OrdersApiRequest req = req("getOrder");
        req.setId("ORD-001");

        Object result = handler.handleRequest(req, null);

        assertTrue(result instanceof Order);
        Order order = (Order) result;
        assertEquals("ORD-001", order.getId());
        assertEquals("CUST-42", order.getCustomerId());
        assertEquals("MacBook Air", order.getProduct());
        assertEquals(1, order.getQuantity());
    }

    @Test
    void getOrder_unknownId_throwsOrderNotFoundException() {
        OrdersApiRequest req = req("getOrder");
        req.setId("ORD-999");

        assertThrows(OrderNotFoundException.class, () -> handler.handleRequest(req, null));
    }

    @Test
    void getOrder_blankId_throwsValidationException() {
        OrdersApiRequest req = req("getOrder");
        req.setId(" ");

        assertThrows(ValidationException.class, () -> handler.handleRequest(req, null));
    }

    @Test
    void getOrdersByCustomer_returnsOnlyMatchingOrders() {
        OrdersApiRequest req = req("getOrdersByCustomer");
        req.setCustomerId("CUST-42");

        Object result = handler.handleRequest(req, null);

        assertTrue(result instanceof List);
        List<?> orders = (List<?>) result;
        assertEquals(2, orders.size());
        assertTrue(orders.stream()
                .allMatch(o -> o instanceof Order && ((Order) o).getCustomerId().equals("CUST-42")));
    }

    @Test
    void getOrdersByCustomer_singleCustomer_returnsOneOrder() {
        OrdersApiRequest req = req("getOrdersByCustomer");
        req.setCustomerId("CUST-7");

        Object result = handler.handleRequest(req, null);

        assertTrue(result instanceof List);
        assertEquals(1, ((List<?>) result).size());
    }

    @Test
    void getOrdersByCustomer_noOrders_returnsEmptyList() {
        OrdersApiRequest req = req("getOrdersByCustomer");
        req.setCustomerId("CUST-999");

        Object result = handler.handleRequest(req, null);

        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void getOrdersByCustomer_blankCustomerId_throwsValidationException() {
        OrdersApiRequest req = req("getOrdersByCustomer");
        req.setCustomerId(" ");

        assertThrows(ValidationException.class, () -> handler.handleRequest(req, null));
    }

    @Test
    void unknownOperation_throwsValidationException() {
        OrdersApiRequest req = req("updateOrder");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> handler.handleRequest(req, null));
        assertTrue(ex.getMessage().contains("unknown operation"));
    }

    @Test
    void missingOperation_throwsValidationException() {
        OrdersApiRequest req = req(null);

        assertThrows(ValidationException.class, () -> handler.handleRequest(req, null));
    }

    @Test
    void createThenGet_returnsTheCreatedOrder() {
        OrdersApiRequest create = req("createOrder");
        create.setCustomerId("CUST-42");
        create.setProduct("iPad");
        create.setQuantity(3);
        Order created = (Order) handler.handleRequest(create, null);

        OrdersApiRequest get = req("getOrder");
        get.setId(created.getId());

        assertEquals(created, handler.handleRequest(get, null));
    }

    @Test
    void deleteOrder_seededOrder_returnsNullForEmpty204AndOrderIsGone() {
        OrdersApiRequest delete = req("deleteOrder");
        delete.setId("ORD-001");

        assertNull(handler.handleRequest(delete, null));
        assertThrows(OrderNotFoundException.class, () -> handler.handleRequest(getOrder("ORD-001"), null));
    }

    @Test
    void deleteOrder_unknownOrder_throwsOrderNotFoundException() {
        OrdersApiRequest delete = req("deleteOrder");
        delete.setId("ORD-999");

        assertThrows(OrderNotFoundException.class, () -> handler.handleRequest(delete, null));
    }

    private static OrdersApiRequest getOrder(String id) {
        OrdersApiRequest r = req("getOrder");
        r.setId(id);
        return r;
    }

    private static OrdersApiRequest req(String operation) {
        OrdersApiRequest r = new OrdersApiRequest();
        r.setOperation(operation);
        return r;
    }
}
