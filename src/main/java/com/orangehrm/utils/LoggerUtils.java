package com.orangehrm.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class LoggerUtils {

    // Cache to store loggers and reduce overhead of repeated logger creation
    private static final ConcurrentHashMap<String, Logger> LOGGER_CACHE = new ConcurrentHashMap<>();

    private LoggerUtils() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    /**
     * Gets a logger for the specified class, using cache to improve performance.
     *
     * @param clazz The class to get the logger for
     * @return Logger instance for the class
     */
    public static Logger getLogger(Class<?> clazz) {
        String className = clazz.getName();
        return LOGGER_CACHE.computeIfAbsent(className, LogManager::getLogger);
    }

    /**
     * Clears the logger cache. Useful in rare cases where memory needs to be freed.
     */
    public static void clearLoggerCache() {
        LOGGER_CACHE.clear();
    }
}