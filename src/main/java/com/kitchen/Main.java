
package com.kitchen;

import com.kitchen.simulator.KitchenSimulator;
import com.kitchen.util.Logger;

public class Main {
    public static void main(String[] args) {
        Logger.info("🚀 Launching Kitchen Simulator...");

        int orderCount = 2;
        String inputFilePath = "src/main/resources/orders.json";
        int deliveryTimeoutSec = 60;

        KitchenSimulator simulator = new KitchenSimulator(orderCount, inputFilePath, deliveryTimeoutSec);

        try {
            simulator.run();
            simulator.awaitTerminationAll(deliveryTimeoutSec);
        } catch (Exception e) {
            Logger.error("❌ Simulator failed: %s", e.toString());
        }

        Logger.info("📦 Summary:");
        Logger.info(" - Orders Placed:   %d", simulator.getTotalOrders());
        Logger.info(" - Orders Delivered:%d", simulator.getDeliveredCount());
        Logger.info(" - Orders Wasted:   %d", simulator.getWastedCount());

        Logger.info("🏁 Simulation completed.");
    }
}
