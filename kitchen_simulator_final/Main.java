package com.kitchen;

import com.kitchen.simulator.KitchenSimulator;
import com.kitchen.util.Logger;

public class Main {
    public static void main(String[] args) {
        Logger.info("🚀 Launching Kitchen Simulator...");

        int orderCount = 100;
        String inputFilePath = "src/main/resources/orders_large.json";
        int deliveryTimeoutSec = 60;

        KitchenSimulator simulator = new KitchenSimulator(orderCount, inputFilePath, deliveryTimeoutSec);
        simulator.run();

        Logger.info("🏁 All systems shut down. Simulation completed successfully.");
    }
}
