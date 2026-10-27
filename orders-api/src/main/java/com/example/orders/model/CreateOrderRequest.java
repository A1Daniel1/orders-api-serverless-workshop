package com.example.orders.model;

/**
 * Raw input for creating an order.
 *
 * <p>This is a plain data carrier with no validation logic. The no-arg
 * constructor and setters also make it straightforward to deserialize from
 * JSON in a later phase. {@code OrderService} is responsible for enforcing the
 * validation rules.</p>
 */
public class CreateOrderRequest {

    private String customerId;
    private String product;
    private int quantity;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String customerId, String product, int quantity) {
        this.customerId = customerId;
        this.product = product;
        this.quantity = quantity;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
