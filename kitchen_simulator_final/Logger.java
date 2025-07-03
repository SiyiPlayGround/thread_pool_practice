package com.kitchen.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void log(String level, String message) {
        String timestamp = formatter.format(new Date());
        System.out.printf("[%s] [%s] %s%n", timestamp, level, message);
    }

    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    public static void warn(String format, Object... args) {
        warn(String.format(format, args));
    }

    public static void error(String format, Object... args) {
        error(String.format(format, args));
    }
}
