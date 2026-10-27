package com.example.orders.model;

/**
 * Input shape for the {@code GET /orders?customerId=X} operation.
 *
 * <p>API Gateway (non-proxy integration, mapping template) sends a small JSON
 * body such as {@code {"customerId": "CUST-42"}}. Plain data carrier with no
 * logic; validation happens in {@code OrderService}.</p>
 */
public class GetOrdersByCustomerRequest {

    private String customerId;

    public GetOrdersByCustomerRequest() {
    }

    public GetOrdersByCustomerRequest(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
