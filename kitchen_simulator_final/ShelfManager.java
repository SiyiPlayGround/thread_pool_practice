package com.kitchen.shelf;

import com.kitchen.order.Order;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all shelves and handles order placement, pickup, and overflow eviction.
 */
public class ShelfManager {
    private final Map<String, Shelf> shelves;

    public ShelfManager() {
        this.shelves = new ConcurrentHashMap<>();
        shelves.put("hot", new Shelf("hot", 10, 1.0));
        shelves.put("cold", new Shelf("cold", 10, 1.0));
        shelves.put("frozen", new Shelf("frozen", 10, 1.0));
        shelves.put("overflow", new Shelf("overflow", 15, 2.0));
    }

    /**
     * Place an order on the best available shelf.
     */
    public synchronized boolean placeOrder(Order order) {
        String temp = order.getTemp();
        Shelf targetShelf = shelves.get(temp);

        if (targetShelf != null && targetShelf.addOrder(order)) {
            return true;
        }

        Shelf overflow = shelves.get("overflow");
        if (overflow.addOrder(order)) {
            System.out.printf("[INFO] Placed %s on overflow shelf%n", order.getId());
            return true;
        }

        // Overflow full: evict one
        Order evicted = overflow.evictAnyOrder();
        if (evicted != null) {
            System.out.printf("[WASTE] Overflow full, evicted %s%n", evicted.getId());
            overflow.addOrder(order);
            return true;
        } else {
            System.out.printf("[WASTE] Dropped %s, overflow full and no evictable order%n", order.getId());
            return false;
        }
    }

    /**
     * Attempt to remove an order from any shelf (used by courier pickup).
     */
    public synchronized Order pickupOrder(String orderId) {
        for (Shelf shelf : shelves.values()) {
            if (shelf.contains(orderId)) {
                return shelf.removeOrder(orderId);
            }
        }
        return null;
    }

    /**
     * Optional: remove all expired orders (based on current value <= 0).
     */
    public synchronized void removeExpiredAndRebalance() {
        long now = System.currentTimeMillis();
        List<String> expired = new ArrayList<>();
        for (Shelf shelf : shelves.values()) {
            for (Order o : shelf.getAllOrders()) {
                if (o.getCurrentValue() <= 0.0) {
                    expired.add(o.getId());
                    shelf.removeOrder(o.getId());
                    System.out.printf("[EXPIRED] Removed %s from %s%n", o.getId(), shelf.getName());
                }
            }
        }

        // Optional: rebalance overflow → main shelf
        Shelf overflow = shelves.get("overflow");
        List<Order> toRebalance = new ArrayList<>(overflow.getAllOrders());
        for (Order o : toRebalance) {
            Shelf target = shelves.get(o.getTemp());
            if (target != null && !target.isFull()) {
                overflow.removeOrder(o.getId());
                target.addOrder(o);
                System.out.printf("[REBALANCE] Moved %s from overflow to %s%n", o.getId(), target.getName());
            }
        }
    }
}
