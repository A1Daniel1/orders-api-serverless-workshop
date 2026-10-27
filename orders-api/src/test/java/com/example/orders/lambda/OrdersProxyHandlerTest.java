package com.example.orders.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrdersProxyHandlerTest {

    private OrdersProxyHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrdersProxyHandler();
    }

    @Test
    void postOrders_createsOrder_returns201WithLocationHeader() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/orders")
                .withBody("{\"customerId\":\"CUST-42\",\"product\":\"MacBook Air\",\"quantity\":1}");

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(201, response.getStatusCode());
        assertEquals("/orders/ORD-004", response.getHeaders().get("Location"));
        assertTrue(response.getBody().contains("\"customerId\":\"CUST-42\""));
    }

    @Test
    void postOrders_missingBody_returns400() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/orders");

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("INVALID_INPUT"));
    }

    @Test
    void postOrders_zeroQuantity_returns400() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/orders")
                .withBody("{\"customerId\":\"CUST-42\",\"product\":\"Monitor\",\"quantity\":0}");

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("INVALID_INPUT"));
    }

    @Test
    void getOrderById_existing_returns200() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/orders/ORD-001")
                .withPathParameters(java.util.Map.of("id", "ORD-001"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"id\":\"ORD-001\""));
    }

    @Test
    void getOrderById_unknown_returns404WithErrorDocument() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/orders/ORD-999")
                .withPathParameters(java.util.Map.of("id", "ORD-999"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("ORDER_NOT_FOUND"));
        assertTrue(response.getBody().contains("Order ORD-999 does not exist"));
    }

    @Test
    void getOrdersByCustomer_returnsMatchingOrdersAsJsonArray() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/orders")
                .withQueryStringParameters(java.util.Map.of("customerId", "CUST-42"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().startsWith("["));
        assertTrue(response.getBody().contains("ORD-001"));
        assertTrue(response.getBody().contains("ORD-002"));
    }

    @Test
    void getOrdersByCustomer_unknownCustomer_returns200WithEmptyArray() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/orders")
                .withQueryStringParameters(java.util.Map.of("customerId", "CUST-100"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(200, response.getStatusCode());
        assertEquals("[]", response.getBody());
    }

    @Test
    void getOrdersByCustomer_missingParameter_returns400() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/orders");

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("INVALID_INPUT"));
    }

    @Test
    void deleteOrder_existing_returns204WithNoBody() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath("/orders/ORD-002")
                .withPathParameters(java.util.Map.of("id", "ORD-002"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteOrder_unknown_returns404() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath("/orders/ORD-999")
                .withPathParameters(java.util.Map.of("id", "ORD-999"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("ORDER_NOT_FOUND"));
    }

    @Test
    void unsupportedRoute_returns404() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("PUT")
                .withPath("/orders/ORD-001")
                .withPathParameters(java.util.Map.of("id", "ORD-001"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, null);

        assertEquals(405, response.getStatusCode());
        assertTrue(response.getBody().contains("METHOD_NOT_ALLOWED"));
    }
}
