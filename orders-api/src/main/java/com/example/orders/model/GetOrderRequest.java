package com.example.orders.model;

/**
 * Input shape for the {@code GET /orders/{id}} operation.
 *
 * <p>API Gateway (non-proxy integration, mapping template) sends a small JSON
 * body such as {@code {"id": "ORD-42"}}. Plain data carrier with no logic;
 * validation happens in {@code OrderService}.</p>
 */
public class GetOrderRequest {

    private String id;

    public GetOrderRequest() {
    }

    public GetOrderRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
