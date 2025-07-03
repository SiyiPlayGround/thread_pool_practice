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

    // ✅ 双参构造器（兼容用法）
    public Courier(Order order, ShelfManager shelfManager) {
        this(order, shelfManager, null); // Delegate
    }

    // ✅ 三参构造器（用于回调统计）
    public Courier(Order order, ShelfManager shelfManager, KitchenSimulator simulator) {
        this.order = order;
        this.shelfManager = shelfManager;
        // Optional, used to update stats
    }

    @Override
    public void run() {
        try {
            // 🚚 Simulate delay (2–6 seconds)
            int delay = 2000 + (int) (Math.random() * 4000);
            Thread.sleep(delay);

            // 🏪 Try to pick up the order
            Order pickedUp = shelfManager.pickupOrder(order.getId());
            if (pickedUp != null) {
                double value = pickedUp.getCurrentValue();
                Logger.delivered(pickedUp.getId(), delay, value);

//                if (simulator != null) {
//                    simulator.incrementDeliveredCount(); // ✅ safe if synchronized
//                }
            } else {
                Logger.warn("[COURIER] Order %s already gone (possibly expired)", order.getId());

//                if (simulator != null) {
//                    simulator.incrementWastedCount();
//                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.error("[COURIER] Interrupted while delivering %s", order.getId());
        } catch (Exception ex) {
            Logger.error("[COURIER] Unexpected error for %s: %s", order.getId(), ex.getMessage());
        }
    }
}