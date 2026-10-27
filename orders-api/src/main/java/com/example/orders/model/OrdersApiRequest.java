package com.example.orders.model;

/**
 * Single request payload for the Orders API dispatcher (one Lambda function).
 *
 * <p>API Gateway (non-proxy integration, mapping template) sends a small JSON
 * body. The {@code operation} field selects which {@code OrderService} method
 * to call; the remaining fields are optional depending on the operation.
 * {@code quantity} is {@link Integer} so it can be absent in GET operations.
 * Plain data carrier with no logic.</p>
 */
public class OrdersApiRequest {

    private String operation;
    private String id;
    private String customerId;
    private String product;
    private Integer quantity;

    public OrdersApiRequest() {
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
