package com.kitchen.order;

public class Order {
    private final String id;
    private final String name;
    private final String temp;
    private final int shelfLife;
    private final double decayRate;
    private volatile OrderStatus status;

    private long creationTime;
    private double shelfDecayModifier = 1.0;

    public Order(String id, String name, String temp, int shelfLife, double decayRate) {
        this.id = id;
        this.name = name;
        this.temp = temp.toLowerCase();
        this.shelfLife = shelfLife;
        this.decayRate = decayRate;
        this.creationTime = System.currentTimeMillis();
        this.status = OrderStatus.CREATED;
    }

    public synchronized void transitionTo(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void markPlacedOnShelf(double modifier) {
        this.shelfDecayModifier = modifier;
        this.creationTime = System.currentTimeMillis();
    }

    public double getCurrentValue() {
        long ageInSeconds = (System.currentTimeMillis() - creationTime) / 1000;
        double decay = decayRate * ageInSeconds * shelfDecayModifier;
        double value = (shelfLife - decay) / shelfLife;
        return Math.max(0.0, value);
    }

    public String getId() {
        return id;
    }

    public String getTemp() {
        return temp;
    }

    public int getShelfLife() {
        return shelfLife;
    }

    public double getDecayRate() {
        return decayRate;
    }
}
