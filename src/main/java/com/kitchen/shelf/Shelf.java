package com.kitchen.shelf;

import com.kitchen.order.Order;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a shelf for storing orders of a certain temperature type.
 */
public class Shelf {
    private final String name;              // e.g., "hot", "cold", "frozen", "overflow"
    private final int capacity;
    private final double decayModifier;     // 1.0 for normal shelves, 2.0 for overflow
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock(); // 🔐 用于控制并发修改

    public Shelf(String name, int capacity, double decayModifier) {
        this.name = name;
        this.capacity = capacity;
        this.decayModifier = decayModifier;
    }

    public boolean isFull() {
        return orders.size() >= capacity;
    }

    /**
     * Safely adds an order to the shelf if there's space.
     */
    public boolean addOrder(Order order) {
        lock.lock(); // 🔐 加锁保护检查+插入
        try {
            if (orders.size() >= capacity) {
                return false;
            }
            order.markPlacedOnShelf(decayModifier);
            orders.put(order.getId(), order);
            System.out.printf("[SHELF:%s] Placed order %s. Current size: %d%n", name, order.getId(), orders.size());
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Safely removes an order by ID.
     */
    public Order removeOrder(String orderId) {
        lock.lock();
        try {
            return orders.remove(orderId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Evicts any order (used when overflow shelf is full).
     */
    public Order evictAnyOrder() {
        lock.lock();
        try {
            for (Order o : orders.values()) {
                if (orders.remove(o.getId()) != null) {
                    return o;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public Collection<Order> getAllOrders() {
        return orders.values(); // ⚠️ 注意：返回的是 live view，遍历时外部代码应加锁
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