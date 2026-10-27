package com.example.orders.repository;

import com.example.orders.model.Order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory storage backed by a {@link Map}.
 *
 * <p>{@link LinkedHashMap} preserves insertion order, so
 * {@link #findByCustomerId(String)} returns results deterministically.</p>
 */
public class InMemoryOrderRepository {

    private final Map<String, Order> store = new LinkedHashMap<>();

    public void save(Order order) {
        store.put(order.getId(), order);
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Order> findByCustomerId(String customerId) {
        return store.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .toList();
    }

    /** Removes the order with the given id, returning the removed order, or null if absent. */
    public Order delete(String id) {
        return store.remove(id);
    }
}
