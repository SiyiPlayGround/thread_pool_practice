package com.kitchen.lifecycle;

import com.kitchen.order.Order;
import com.kitchen.shelf.ShelfManager;

/**
 * Manages the full lifecycle of an order:
 * - placement
 * - potential shelf overflow handling
 * - decay monitoring (handled in ShelfManager)
 */
public class OrderLifecycleManager {
    private final ShelfManager shelfManager;

    public OrderLifecycleManager(ShelfManager shelfManager) {
        this.shelfManager = shelfManager;
    }

    /**
     * Places the order into the shelf system and logs the result.
     *
     * @param order Incoming order
     * @return true if placed successfully, false if discarded
     */
    public boolean processNewOrder(Order order) {
        boolean success = shelfManager.placeOrder(order);
        if (!success) {
            System.out.printf("[DISCARDED] %s could not be placed on any shelf\n", order.getId());
        }
        return success;
    }
}