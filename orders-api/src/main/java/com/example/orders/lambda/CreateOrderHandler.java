package com.example.orders.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.orders.model.CreateOrderRequest;
import com.example.orders.model.Order;
import com.example.orders.repository.InMemoryOrderRepository;
import com.example.orders.service.OrderService;

/**
 * AWS Lambda entry point for the "create order" operation.
 *
 * <p>This is the ONLY class in the application that imports AWS types. It is a
 * thin adapter: the runtime deserializes the incoming JSON into
 * {@link CreateOrderRequest}, this handler calls {@link OrderService}, and the
 * runtime serializes the returned {@link Order} back to JSON. No JSON handling
 * happens in application code.</p>
 *
 * <p>The runtime instantiates this handler once per container and reuses it
 * across invocations (warm start), so the {@link OrderService} (and its
 * in-memory repository) is created once in the no-arg constructor.</p>
 */
public class CreateOrderHandler implements RequestHandler<CreateOrderRequest, Order> {

    private final OrderService service;

    /** No-arg constructor required by the Lambda runtime. */
    public CreateOrderHandler() {
        this.service = new OrderService(new InMemoryOrderRepository());
    }

    @Override
    public Order handleRequest(CreateOrderRequest input, Context context) {
        return service.createOrder(input);
    }
}
