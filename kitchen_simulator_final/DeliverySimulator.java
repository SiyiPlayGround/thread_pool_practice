package com.kitchen.simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchen.lifecycle.OrderLifecycleManager;
import com.kitchen.order.Order;
import com.kitchen.order.OrderParser;
import com.kitchen.shelf.ShelfManager;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public void runSimulation(List<Order> orders) {
        for (Order order : orders) {
            pool.submit(() -> {
                boolean placed = lifecycleManager.processNewOrder(order);
                if (placed) {
                    pool.submit(new Courier(order, shelfManager));
                }
            });
        }
    }

    public static void main(String[] args) throws Exception {
        // Load order JSON from file
        ObjectMapper mapper = new ObjectMapper();
        List<Order> orders = mapper.readValue(
                new File("src/main/resources/orders.json"),
                new TypeReference<List<Order>>() {}
        );

        DeliverySimulator simulator = new DeliverySimulator(16);
        simulator.runSimulation(orders);
    }
}
