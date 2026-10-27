package com.example.orders.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.model.Order;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryOrderRepositoryTest {

    private InMemoryOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Test
    void saveThenFindById_returnsTheOrder() {
        Order order = new Order("ORD-001", "CUST-42", "MacBook Air", 1);
        repository.save(order);

        assertEquals(Optional.of(order), repository.findById("ORD-001"));
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertEquals(Optional.empty(), repository.findById("ORD-999"));
    }

    @Test
    void findByCustomerId_returnsOnlyMatchingOrdersInInsertionOrder() {
        Order a = new Order("ORD-001", "CUST-42", "MacBook Air", 1);
        Order b = new Order("ORD-002", "CUST-77", "iPad", 1);
        Order c = new Order("ORD-003", "CUST-42", "iPhone", 2);
        repository.save(a);
        repository.save(b);
        repository.save(c);

        assertEquals(List.of(a, c), repository.findByCustomerId("CUST-42"));
    }

    @Test
    void findByCustomerId_noMatches_returnsEmptyList() {
        assertTrue(repository.findByCustomerId("CUST-42").isEmpty());
    }

    @Test
    void delete_existingId_returnsAndRemovesTheOrder() {
        Order order = new Order("ORD-001", "CUST-42", "MacBook Air", 1);
        repository.save(order);

        assertEquals(order, repository.delete("ORD-001"));
        assertEquals(Optional.empty(), repository.findById("ORD-001"));
    }

    @Test
    void delete_unknownId_returnsNull() {
        assertNull(repository.delete("ORD-999"));
    }
}
