package com.kitchen.simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchen.lifecycle.OrderLifecycleManager;
import com.kitchen.order.Order;
import com.kitchen.shelf.ShelfManager;
import com.kitchen.util.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main simulator entry point. Loads orders, places them, and starts couriers.
 */
public class DeliverySimulator {

    private final ShelfManager shelfManager;
    private final OrderLifecycleManager lifecycleManager;
    private final ExecutorService pool;

    public DeliverySimulator(int poolSize) {
        this.shelfManager = new ShelfManager();
        this.lifecycleManager = new OrderLifecycleManager(shelfManager);
        this.pool = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * Submits both placement and delivery tasks for all orders.
     */
    public void runSimulation(List<Order> orders) {
        for (Order order : orders) {
            pool.submit(() -> {
                boolean placed = lifecycleManager.processNewOrder(order);
                if (placed) {
                    submitDelivery(order);
                }
            });
        }
    }

    /**
     * Submits a delivery task (with delay) for a placed order.
     */
    private void submitDelivery(Order order) {
        pool.submit(new Courier(order, shelfManager));
    }

    /**
     * Gracefully shuts down the executor pool.
     */
    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                Logger.warn("Timeout waiting for tasks to complete.");
            }
        } catch (InterruptedException e) {
            Logger.error("Interrupted during shutdown: %s", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}