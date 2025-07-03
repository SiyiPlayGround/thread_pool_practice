package com.kitchen.shelf;

import com.kitchen.order.Order;
import com.kitchen.util.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe manager for all shelves, including placement, pickup, expiration, and rebalancing.
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
     * Place an order onto its appropriate shelf. Fallback to overflow if needed.
     */
    public boolean placeOrder(Order order) {
        String temp = order.getTemp().toLowerCase();
        Shelf targetShelf = shelves.get(temp);

        if (targetShelf != null && targetShelf.addOrder(order)) {
            Logger.info("[SHELF:%s] Placed order %s. Current size: %d", temp, order.getId(), targetShelf.size());
            return true;
        }

        Shelf overflow = shelves.get("overflow");
        if (overflow.addOrder(order)) {
            Logger.info("[OVERFLOW] Placed %s. Current size: %d", order.getId(), overflow.size());
            return true;
        }

        // Overflow full → attempt eviction
        Order evicted = overflow.evictAnyOrder();
        if (evicted != null) {
            Logger.warn("[OVERFLOW] Evicted %s to make space for %s", evicted.getId(), order.getId());
            overflow.addOrder(order);
            return true;
        }

        Logger.error("[DROP] No space available. Dropped order %s", order.getId());
        return false;
    }

    /**
     * Pick up an order by ID from any shelf.
     */
    public Order pickupOrder(String orderId) {
        for (Shelf shelf : shelves.values()) {
            Order picked = shelf.removeOrder(orderId);
            if (picked != null) {
                Logger.info("[PICKUP] %s picked up from %s shelf", orderId, shelf.getName());
                return picked;
            }
        }
        return null;
    }

    /**
     * Periodically clear expired orders and rebalance overflow to primary shelves.
     */
    public void removeExpiredAndRebalance() {
        for (Shelf shelf : shelves.values()) {
            List<Order> toRemove = new ArrayList<>();
            for (Order order : shelf.getAllOrders()) {
                if (order.getCurrentValue() <= 0.0) {
                    toRemove.add(order);
                }
            }
            for (Order expired : toRemove) {
                shelf.removeOrder(expired.getId());
                Logger.warn("[EXPIRED] Removed %s from %s", expired.getId(), shelf.getName());
            }
        }

        // Rebalance from overflow
        Shelf overflow = shelves.get("overflow");
        List<Order> overflowOrders = new ArrayList<>(overflow.getAllOrders());

        for (Order o : overflowOrders) {
            Shelf primary = shelves.get(o.getTemp().toLowerCase());
            if (primary != null && !primary.isFull()) {
                overflow.removeOrder(o.getId());
                primary.addOrder(o);
                Logger.info("[REBALANCE] Moved %s from overflow to %s", o.getId(), primary.getName());
            }
        }
    }

    /**
     * Debug: get total orders across all shelves.
     */
    public int totalOrderCount() {
        return shelves.values().stream().mapToInt(Shelf::size).sum();
    }

    public Map<String, Shelf> getShelves() {
        return shelves;
    }
}
