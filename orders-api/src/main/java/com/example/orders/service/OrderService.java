package com.example.orders.service;

import com.example.orders.exception.OrderNotFoundException;
import com.example.orders.exception.ValidationException;
import com.example.orders.model.CreateOrderRequest;
import com.example.orders.model.Order;
import com.example.orders.repository.InMemoryOrderRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Application logic for orders: validates input, generates identifiers,
 * builds {@link Order}s and delegates persistence to the repository.
 *
 * <p>Contains no HTTP, AWS or storage-specific concerns.</p>
 */
public class OrderService {

    private static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile("CUST-\\d+");

    private final InMemoryOrderRepository repository;
    private final AtomicInteger idSequence = new AtomicInteger(1);

    public OrderService(InMemoryOrderRepository repository) {
        this.repository = repository;
    }

    public Order createOrder(CreateOrderRequest request) {
        validateRequest(request);

        String id = nextId();
        Order order = new Order(id, request.getCustomerId(), request.getProduct(), request.getQuantity());
        repository.save(order);
        return order;
    }

    public Order getOrder(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("id must not be blank");
        }
        return repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " does not exist"));
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new ValidationException("customerId must not be blank");
        }
        return repository.findByCustomerId(customerId);
    }

    public void deleteOrder(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("id must not be blank");
        }
        if (repository.delete(id) == null) {
            throw new OrderNotFoundException("Order " + id + " does not exist");
        }
    }

    private void validateRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (request.getCustomerId() == null || request.getCustomerId().isBlank()) {
            throw new ValidationException("customerId must not be blank");
        }
        if (!CUSTOMER_ID_PATTERN.matcher(request.getCustomerId()).matches()) {
            throw new ValidationException("customerId must match CUST-<digits>");
        }
        if (request.getProduct() == null || request.getProduct().isBlank()) {
            throw new ValidationException("product must not be blank");
        }
        if (request.getQuantity() < 1) {
            throw new ValidationException("quantity must be at least 1");
        }
    }

    private String nextId() {
        return String.format("ORD-%03d", idSequence.getAndIncrement());
    }
}
