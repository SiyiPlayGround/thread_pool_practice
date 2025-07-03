package com.kitchen.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Parses a list of orders from a JSON file.
 */
public class OrderParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Load orders from a JSON file.
     *
     * @param file JSON file containing an array of orders
     * @return list of Order objects
     * @throws IOException if file read fails
     */
    public static List<Order> parseFromFile(File file) throws IOException {
        return mapper.readValue(file, new TypeReference<List<Order>>() {});
    }
}
