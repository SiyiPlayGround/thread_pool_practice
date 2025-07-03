package com.kitchen.shelf;

import com.kitchen.order.Order;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a shelf for storing orders of a certain temperature type.
 */
public class Shelf {
    private final String name;              // e.g., "hot", "cold", "frozen", "overflow"
    private final int capacity;
    private final double decayModifier;     // 1.0 for normal shelves, 2.0 for overflow
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public Shelf(String name, int capacity, double decayModifier) {
        this.name = name;
        this.capacity = capacity;
        this.decayModifier = decayModifier;
    }

    public boolean isFull() {
        return orders.size() >= capacity;
    }

    public boolean addOrder(Order order) {
        if (isFull()) {
            return false;
        }
        order.markPlacedOnShelf(decayModifier);
        orders.put(order.getId(), order);
        System.out.printf("[SHELF:%s] Placed order %s. Current size: %d%n", name, order.getId(), orders.size());
        return true;
    }

    public synchronized Order removeOrder(String orderId) {
        return orders.remove(orderId);
    }

    public Order evictAnyOrder() {
        for (Order o : orders.values()) {
            orders.remove(o.getId());
            return o;
        }
        return null;
    }

    public Collection<Order> getAllOrders() {
        return orders.values();
    }

    public String getName() {
        return name;
    }

    public double getDecayModifier() {
        return decayModifier;
    }

    public int size() {
        return orders.size();
    }

    public boolean contains(String orderId) {
        return orders.containsKey(orderId);
    }
}
