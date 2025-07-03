
package com.kitchen.simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchen.lifecycle.OrderLifecycleManager;
import com.kitchen.order.Order;
import com.kitchen.shelf.ShelfManager;
import com.kitchen.util.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

public class KitchenSimulator {
    private final int ingestionRatePerSecond;
    private final String orderFilePath;
    private final int deliveryTimeoutSec;

    private ExecutorService ingestionPool;
    private ScheduledExecutorService courierPool;
    private int totalOrders;
    private int delivered;
    private int wasted;

    public KitchenSimulator(int ingestionRatePerSecond, String orderFilePath, int deliveryTimeoutSec) {
        this.ingestionRatePerSecond = ingestionRatePerSecond;
        this.orderFilePath = orderFilePath;
        this.deliveryTimeoutSec = deliveryTimeoutSec;
    }

    public void run() {
        Logger.info("Launching Kitchen Simulator...");

        List<Order> orders = loadOrdersFromJson(orderFilePath);
        if (orders == null || orders.isEmpty()) {
            Logger.error("No orders found. Exiting.");
            return;
        }

        totalOrders = orders.size();
        ShelfManager shelfManager = new ShelfManager();
        OrderLifecycleManager lifecycleManager = new OrderLifecycleManager(shelfManager);
        int cores = Runtime.getRuntime().availableProcessors();
        int threads = cores * 2;
        ingestionPool = new ThreadPoolExecutor(
                threads, threads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1000),  // 更大队列避免拒绝执行
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略改成退化执行
        );

//        ingestionPool = new ThreadPoolExecutor(
//                8, 8, 0L, TimeUnit.MILLISECONDS,
//                new ArrayBlockingQueue<>(100),
//                new ThreadPoolExecutor.AbortPolicy());

        courierPool = Executors.newScheduledThreadPool(32);
        CountDownLatch latch = new CountDownLatch(totalOrders);
        int delayMs =  1000 / ingestionRatePerSecond;

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            int submitDelay = i * delayMs;

            ingestionPool.submit(() -> {
                try {
                    Thread.sleep(submitDelay);
                    boolean placed = lifecycleManager.processNewOrder(order);
                    if (placed) {
                        int deliveryDelay = 2000 + (int) (Math.random() * 4000);
                        courierPool.schedule(() -> {
                            Order picked = shelfManager.pickupOrder(order.getId());
                            if (picked != null) {
                                synchronized (this) { delivered++; }
                                Logger.delivered(picked.getId(), deliveryDelay, picked.getCurrentValue());
                            } else {
                                synchronized (this) { wasted++; }
                                Logger.warn("Courier came but %s was gone.", order.getId());
                            }
                            latch.countDown();
                        }, deliveryDelay, TimeUnit.MILLISECONDS);
                    } else {
                        synchronized (this) { wasted++; }
                        Logger.warn("Failed to place %s on any shelf.", order.getId());
                        latch.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        try {
            Logger.info("Waiting for all deliveries (timeout=%ds)...", deliveryTimeoutSec);
            boolean finished = latch.await(deliveryTimeoutSec, TimeUnit.SECONDS);
            if (!finished) {
                Logger.warn("Delivery simulation timed out.");
            }
        } catch (InterruptedException e) {
            Logger.error("Interrupted during wait: %s", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void shutdownAll() {
        if (ingestionPool != null && !ingestionPool.isShutdown()) {
            ingestionPool.shutdown();
        }
        if (courierPool != null && !courierPool.isShutdown()) {
            courierPool.shutdown();
        }
    }

    public void awaitTerminationAll(long timeoutSec) {
        shutdownAll();
        try {
            if (ingestionPool != null) {
                ingestionPool.awaitTermination(timeoutSec, TimeUnit.SECONDS);
            }
            if (courierPool != null) {
                courierPool.awaitTermination(timeoutSec, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Logger.error("Interrupted while waiting: %s", e.getMessage());
            Thread.currentThread().interrupt();
        }
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

    public ExecutorService getExecutorService() {
        return ingestionPool;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public int getDeliveredCount() {
        return delivered;
    }

    public int getWastedCount() {
        return wasted;
    }
}
