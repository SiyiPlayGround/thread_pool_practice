package com.kitchen.simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchen.lifecycle.OrderLifecycleManager;
import com.kitchen.order.Order;
import com.kitchen.shelf.ShelfManager;
import com.kitchen.util.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KitchenSimulator {
    private final int ingestionRatePerSecond;
    private final String orderFilePath;
    private final int maxThreads;

    private ExecutorService executor;
    private int totalOrders;
    private int deliveredCount = 0;
    private int wastedCount = 0;

    public KitchenSimulator(int ingestionRatePerSecond, String orderFilePath, int maxThreads) {
        this.ingestionRatePerSecond = ingestionRatePerSecond;
        this.orderFilePath = orderFilePath;
        this.maxThreads = maxThreads;
    }

    public void run() {
        Logger.info("Launching Kitchen Simulator...");

        List<Order> orders = loadOrdersFromJson(orderFilePath);
        if (orders == null || orders.isEmpty()) {
            Logger.error("No orders found. Exiting.");
            return;
        }

        this.totalOrders = orders.size();
        ShelfManager shelfManager = new ShelfManager();
        OrderLifecycleManager lifecycleManager = new OrderLifecycleManager(shelfManager);
        executor = Executors.newFixedThreadPool(maxThreads);
        CountDownLatch latch = new CountDownLatch(orders.size());

        int delay = 1000 / ingestionRatePerSecond;
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            int finalI = i;
            executor.submit(() -> {
                try {
                    Thread.sleep(finalI * delay);

                    boolean success = lifecycleManager.processNewOrder(order);
                    if (success) {
                        executor.submit(() -> {
                            try {
                                new Courier(order, shelfManager, this).run();
                            } finally {
                                latch.countDown();
                            }
                        });
                    } else {
                        incrementWastedCount();
                        latch.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        Logger.info("All orders submitted. Waiting for delivery...");
        try {
            latch.await();
        } catch (InterruptedException e) {
            Logger.error("Interrupted while waiting.");
            Thread.currentThread().interrupt();
        }

        Logger.info("Simulation run() finished.");
    }

    public ExecutorService getExecutorService() {
        return executor;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public int getDeliveredCount() {
        return deliveredCount;
    }

    public int getWastedCount() {
        return wastedCount;
    }

    public synchronized void incrementDeliveredCount() {
        deliveredCount++;
    }

    public synchronized void incrementWastedCount() {
        wastedCount++;
    }

    private List<Order> loadOrdersFromJson(String path) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File(path), new TypeReference<List<Order>>() {});
        } catch (Exception e) {
            Logger.error("Failed to load orders.json: %s", e.getMessage());
            return null;
        }
    }
}
