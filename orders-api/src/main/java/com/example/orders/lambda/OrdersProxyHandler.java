package com.example.orders.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.orders.exception.OrderNotFoundException;
import com.example.orders.exception.ValidationException;
import com.example.orders.model.CreateOrderRequest;
import com.example.orders.model.Order;
import com.example.orders.repository.InMemoryOrderRepository;
import com.example.orders.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * HTTP adapter for Architecture B (Part V): the whole Orders API behind a
 * Lambda proxy integration.
 *
 * <p>This handler MAY know about API Gateway proxy events, path and query
 * parameters, HTTP status codes, headers and JSON serialization of the HTTP
 * response. It delegates every business decision to {@link OrderService},
 * which stays AWS-free. Domain exceptions are translated to HTTP status codes
 * here, in a single place.</p>
 *
 * <p>Runs as a separate function (orders-proxy) using the same fat JAR as the
 * non-proxy dispatcher, so Architecture A keeps working untouched.</p>
 */
public class OrdersProxyHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OrderService service;

    /** No-arg constructor required by the Lambda runtime; seeds demo data. */
    public OrdersProxyHandler() {
        this.service = new OrderService(new InMemoryOrderRepository());
        seedDemoOrders();
    }

    /** Package-private constructor for tests: injects a prepared service. */
    OrdersProxyHandler(OrderService service) {
        this.service = service;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            return route(event);
        } catch (ValidationException e) {
            return error(400, "INVALID_INPUT", e.getMessage());
        } catch (OrderNotFoundException e) {
            return error(404, "ORDER_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            return error(500, "INTERNAL_ERROR", "Unexpected error. See server logs.");
        }
    }

    private APIGatewayProxyResponseEvent route(APIGatewayProxyRequestEvent event) throws Exception {
        String method = event.getHttpMethod();
        Map<String, String> pathParams = event.getPathParameters();
        Map<String, String> queryParams = event.getQueryStringParameters();
        String path = event.getPath();

        if (pathParams != null && pathParams.containsKey("id")) {
            if ("GET".equals(method)) {
                return getOrder(pathParams.get("id"));
            }
            if ("DELETE".equals(method)) {
                return deleteOrder(pathParams.get("id"));
            }
            return error(405, "METHOD_NOT_ALLOWED", method + " is not supported on /orders/{id}");
        }

        if (path != null && path.endsWith("/orders")) {
            if ("POST".equals(method)) {
                return createOrder(event.getBody());
            }
            if ("GET".equals(method)) {
                return listOrdersByCustomer(queryParams == null ? null : queryParams.get("customerId"));
            }
        }

        return error(404, "ROUTE_NOT_FOUND", "No operation for " + method + " " + path);
    }

    private APIGatewayProxyResponseEvent createOrder(String body) throws Exception {
        if (body == null || body.isBlank()) {
            throw new ValidationException("request body is required");
        }
        CreateOrderRequest request = MAPPER.readValue(body, CreateOrderRequest.class);
        Order order = service.createOrder(request);
        return json(201, MAPPER.writeValueAsString(order),
                Map.of("Location", "/orders/" + order.getId()));
    }

    private APIGatewayProxyResponseEvent getOrder(String id) throws Exception {
        Order order = service.getOrder(id);
        return json(200, MAPPER.writeValueAsString(order), Map.of());
    }

    private APIGatewayProxyResponseEvent listOrdersByCustomer(String customerId) throws Exception {
        List<Order> orders = service.getOrdersByCustomer(customerId);
        return json(200, MAPPER.writeValueAsString(orders), Map.of());
    }

    private APIGatewayProxyResponseEvent deleteOrder(String id) {
        service.deleteOrder(id);
        return new APIGatewayProxyResponseEvent().withStatusCode(204);
    }

    private APIGatewayProxyResponseEvent json(int status, String body, Map<String, String> extraHeaders) {
        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Content-Type", "application/json");
        if (extraHeaders != null) {
            headers.putAll(extraHeaders);
        }
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(headers)
                .withBody(body);
    }

    private APIGatewayProxyResponseEvent error(int status, String code, String message) {
        String body;
        try {
            body = MAPPER.writeValueAsString(Map.of("error", code, "message", message));
        } catch (Exception e) {
            body = "{\"error\":\"INTERNAL_ERROR\",\"message\":\"response serialization failure\"}";
        }
        return json(status, body, Map.of());
    }

    private void seedDemoOrders() {
        service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));
        service.createOrder(new CreateOrderRequest("CUST-42", "iPhone", 2));
        service.createOrder(new CreateOrderRequest("CUST-7", "Keyboard", 2));
    }
}
