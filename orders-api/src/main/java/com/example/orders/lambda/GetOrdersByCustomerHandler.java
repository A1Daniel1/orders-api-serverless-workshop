package com.example.orders.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.orders.model.GetOrdersByCustomerRequest;
import com.example.orders.model.Order;
import com.example.orders.repository.InMemoryOrderRepository;
import com.example.orders.service.OrderService;

import java.util.List;

/**
 * AWS Lambda entry point for the "find orders by customer" operation.
 *
 * <p>Thin adapter and the only AWS-aware class for this operation: the runtime
 * deserializes the JSON body into {@link GetOrdersByCustomerRequest}, this
 * handler calls {@link OrderService}, and the runtime serializes the returned
 * {@code List<Order>} back to a JSON array.</p>
 */
public class GetOrdersByCustomerHandler implements RequestHandler<GetOrdersByCustomerRequest, List<Order>> {

    private final OrderService service;

    /** No-arg constructor required by the Lambda runtime. */
    public GetOrdersByCustomerHandler() {
        this(new OrderService(new InMemoryOrderRepository()));
    }

    /** Package-private for tests: allows seeding the repository beforehand. */
    GetOrdersByCustomerHandler(OrderService service) {
        this.service = service;
    }

    @Override
    public List<Order> handleRequest(GetOrdersByCustomerRequest input, Context context) {
        return service.getOrdersByCustomer(input.getCustomerId());
    }
}
