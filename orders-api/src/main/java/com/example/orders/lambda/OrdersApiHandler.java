package com.example.orders.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.orders.exception.ValidationException;
import com.example.orders.model.CreateOrderRequest;
import com.example.orders.model.Order;
import com.example.orders.model.OrdersApiRequest;
import com.example.orders.repository.InMemoryOrderRepository;
import com.example.orders.service.OrderService;

import java.util.List;

/**
 * Single Lambda entry point for the whole Orders API (DEC-001).
 *
 * <p>This is the ONLY AWS-aware class. It is a thin dispatcher: it routes on
 * the {@code operation} field to the matching {@link OrderService} method. No
 * business rules and no HTTP concepts live here. Seeding of demo data is a
 * lambda-layer concern and also lives here, never in {@link OrderService}.</p>
 *
 * <p>The return type is {@link Object} because the operations produce
 * different shapes ({@link Order} vs {@code List<Order>}); the runtime
 * serializes the value by its actual class.</p>
 */
public class OrdersApiHandler implements RequestHandler<OrdersApiRequest, Object> {

    private final OrderService service;

    /** No-arg constructor required by the Lambda runtime; seeds demo data. */
    public OrdersApiHandler() {
        this.service = new OrderService(new InMemoryOrderRepository());
        seedDemoOrders();
    }

    @Override
    public Object handleRequest(OrdersApiRequest input, Context context) {
        String operation = input.getOperation();
        if (operation == null || operation.isBlank()) {
            throw new ValidationException("unknown operation: " + operation);
        }
        return switch (operation) {
            case "createOrder" -> createOrder(input);
            case "getOrder" -> getOrder(input);
            case "getOrdersByCustomer" -> getOrdersByCustomer(input);
            case "deleteOrder" -> deleteOrder(input);
            default -> throw new ValidationException("unknown operation: " + operation);
        };
    }

    private Order createOrder(OrdersApiRequest input) {
        // Absent quantity (null) maps to 0 so OrderService's ">= 1" rule rejects it.
        int quantity = input.getQuantity() == null ? 0 : input.getQuantity();
        return service.createOrder(new CreateOrderRequest(input.getCustomerId(), input.getProduct(), quantity));
    }

    private Order getOrder(OrdersApiRequest input) {
        return service.getOrder(input.getId());
    }

    private List<Order> getOrdersByCustomer(OrdersApiRequest input) {
        return service.getOrdersByCustomer(input.getCustomerId());
    }

    /** Successful deletion returns null: the DELETE integration response maps it to an empty 204 body. */
    private Object deleteOrder(OrdersApiRequest input) {
        service.deleteOrder(input.getId());
        return null;
    }

    private void seedDemoOrders() {
        service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));
        service.createOrder(new CreateOrderRequest("CUST-42", "iPhone", 2));
        service.createOrder(new CreateOrderRequest("CUST-7", "Keyboard", 2));
    }
}
