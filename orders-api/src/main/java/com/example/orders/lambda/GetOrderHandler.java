package com.example.orders.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.orders.model.GetOrderRequest;
import com.example.orders.model.Order;
import com.example.orders.repository.InMemoryOrderRepository;
import com.example.orders.service.OrderService;

/**
 * AWS Lambda entry point for the "retrieve an order" operation.
 *
 * <p>Thin adapter and the only AWS-aware class for this operation: the runtime
 * deserializes the JSON body into {@link GetOrderRequest}, this handler calls
 * {@link OrderService}, and the runtime serializes the returned {@link Order}
 * back to JSON.</p>
 */
public class GetOrderHandler implements RequestHandler<GetOrderRequest, Order> {

    private final OrderService service;

    /** No-arg constructor required by the Lambda runtime. */
    public GetOrderHandler() {
        this(new OrderService(new InMemoryOrderRepository()));
    }

    /** Package-private for tests: allows seeding the repository beforehand. */
    GetOrderHandler(OrderService service) {
        this.service = service;
    }

    @Override
    public Order handleRequest(GetOrderRequest input, Context context) {
        return service.getOrder(input.getId());
    }
}
