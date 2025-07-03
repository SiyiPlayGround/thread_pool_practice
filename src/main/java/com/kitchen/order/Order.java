package com.kitchen.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a food order in the system.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private final String id;
    private final String name;
    private final String temp; // hot, cold, frozen
    private final int shelfLife;
    private final double decayRate;

    private long creationTime; // when placed on shelf
    private double shelfDecayModifier = 1.0; // 1 for normal shelf, 2 for overflow

    // ✅ Add a Jackson constructor
    @JsonCreator
    public Order(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("temp") String temp,
            @JsonProperty("shelfLife") int shelfLife,
            @JsonProperty("decayRate") double decayRate
    ) {
        this.id = id;
        this.name = name;
        this.temp = temp.toLowerCase();
        this.shelfLife = shelfLife;
        this.decayRate = decayRate;
        this.creationTime = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) life=%ds, decayRate=%.2f", id, name, temp, shelfLife, decayRate);
    }
}