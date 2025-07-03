package com.kitchen.simulator;

import com.kitchen.order.Order;
import com.kitchen.shelf.ShelfManager;
import com.kitchen.util.Logger;

/**
 * Simulates a courier picking up the order after a random delay.
 */
public class Courier implements Runnable {
    private final Order order;
    private final ShelfManager shelfManager;
    private final KitchenSimulator simulator; // 可为 null

    // 原有的双参构造器
    public Courier(Order order, ShelfManager shelfManager) {
        this(order, shelfManager, null); // 委托给三参构造器
    }
Z
    // 新增的三参构造器（带统计回调）
    public Courier(Order order, ShelfManager shelfManager, KitchenSimulator simulator) {
        this.order = order;
        this.shelfManager = shelfManager;
        this.simulator = simulator;
    }

    @Override
    public void run() {
        try {
            // Simulate delivery delay (between 2–6 seconds)
            int delay = 2000 + (int) (Math.random() * 4000);
            Thread.sleep(delay);

            Order pickedUp = shelfManager.pickupOrder(order.getId());
            if (pickedUp != null) {
                double value = pickedUp.getCurrentValue();
                Logger.delivered(pickedUp.getId(), delay, value);
                if (simulator != null) {
                    simulator.incrementDeliveredCount(); // 更新统计
                }
            } else {
                Logger.warn("Courier arrived but order %s was already gone", order.getId());
                if (simulator != null) {
                    simulator.incrementWastedCount(); // 更新统计
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.error("Courier interrupted while delivering %s", order.getId());
        } catch (Exception ex) {
            Logger.error("Unexpected error delivering %s: %s", order.getId(), ex.getMessage());
        }
    }
}