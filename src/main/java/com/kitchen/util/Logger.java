package com.kitchen.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Thread-safe logger with consistent format and optional structured logging.
 */
public class Logger {

    // Thread-local formatter to ensure thread safety
    private static final ThreadLocal<SimpleDateFormat> formatter =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm:ss.SSS"));

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void delivered(String orderId, int delayMs, double finalValue) {
        info("[DELIVERED] %s after %d ms | final value=%.2f", orderId, delayMs, finalValue);
    }

    /**
     * Generic log entry method.
     */
    public static void log(String level, String message) {
        String timestamp = formatter.get().format(new Date());
        System.out.printf("[%s] [%s] %s%n", timestamp, level, message);
    }

    // Format overloads
    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    public static void warn(String format, Object... args) {
        warn(String.format(format, args));
    }

    public static void error(String format, Object... args) {
        error(String.format(format, args));
    }

    public static void log(String level, String format, Object... args) {
        log(level, String.format(format, args));
    }
}
