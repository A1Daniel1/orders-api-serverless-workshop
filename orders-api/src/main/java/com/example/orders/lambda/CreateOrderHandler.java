package com.example.orders.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.example.orders.model.CreateOrderRequest;
import com.example.orders.model.Order;
import com.example.orders.repository.InMemoryOrderRepository;
import com.example.orders.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * AWS Lambda entry point for the "create order" operation.
 *
 * <p>This is the ONLY class in the application that imports AWS types. It is a
 * thin adapter that translates between raw JSON (the Lambda event/response)
 * and the plain-Java domain: parse JSON, call {@link OrderService}, write JSON
 * back.</p>
 *
 * <p>The runtime instantiates this handler once per container and reuses it
 * across invocations (warm start), so the {@link OrderService} (and its
 * in-memory repository) is created once in the no-arg constructor.</p>
 */
public class CreateOrderHandler implements RequestStreamHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OrderService service;

    /** No-arg constructor required by the Lambda runtime. */
    public CreateOrderHandler() {
        this.service = new OrderService(new InMemoryOrderRepository());
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        CreateOrderRequest request = MAPPER.readValue(input, CreateOrderRequest.class);
        Order order = service.createOrder(request);
        MAPPER.writeValue(output, order);
    }
}
