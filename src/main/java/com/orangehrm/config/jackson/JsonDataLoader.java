package com.orangehrm.config.jackson;

import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class JsonDataLoader {
    private static final Logger logger = LoggerUtils.getLogger(JsonDataLoader.class);

    // Map to store multiple JSON readers
    private static final ConcurrentHashMap<String, JsonDataReader> jsonReaders = new ConcurrentHashMap<>();

    // For backward compatibility
    private static JsonDataReader defaultReaderInstance;
    private static boolean isInitialized = false;

    public static synchronized void initializeJsonData(String jsonDataFilePath, Boolean cacheEnabled, String schemaFilePath) {
        try {
            JsonDataReader reader = JsonDataReader.builder()
                    .setFilePath(jsonDataFilePath)
                    .setCacheEnabled(cacheEnabled)
                    .setSchemaPath(schemaFilePath)
                    .build();

            jsonReaders.put(jsonDataFilePath, reader);

            // Maintain the first loaded file as default for backward compatibility
            if (!isInitialized) {
                defaultReaderInstance = reader;
                isInitialized = cacheEnabled;
            }

            logger.info("Successfully loaded JSON feature data from '{}'", jsonDataFilePath);

        } catch (Exception error) {
            ErrorHandler.logError(error, "initializeJsonData", "Failed to load JSON data from " + jsonDataFilePath);
            throw new JsonDataLoaderException("Failed to load JSON data", error);
        }
    }

    /**
     * Gets a specific JsonDataReader instance by file path.
     *
     * @param jsonFilePath Path to the JSON data file.
     * @return The JsonDataReader instance for the specified file.
     * @throws IllegalStateException if the specified JSON data is not initialized.
     */
    public static JsonDataReader getJsonReader(String jsonFilePath) {
        JsonDataReader reader = jsonReaders.get(jsonFilePath);
        if (reader == null) {
            throw new IllegalStateException("JSON data not initialized for path: " + jsonFilePath);
        }
        return reader;
    }

    /**
     * Gets the default JsonDataReader instance (first loaded file).
     * Maintained for backward compatibility.
     */
    public static JsonDataReader getJsonReaderInstance() {
        if (!isInitialized || defaultReaderInstance == null) {
            throw new IllegalStateException("Default JSON reader not initialized. Call initializeJsonData() first.");
        }
        return defaultReaderInstance;
    }

    /**
     * Checks if a specific JSON file is loaded.
     */
    public static boolean isJsonDataLoaded(String jsonFilePath) {
        return jsonReaders.containsKey(jsonFilePath);
    }

    /**
     * Checks if any JSON data is loaded (backward compatibility).
     */
    public static boolean isJsonDataLoaded() {
        return isInitialized && defaultReaderInstance != null;
    }

    /**
     * Resets all loaded JSON data and cleans up resources.
     */
    public static synchronized void resetJsonData() {
        try {
            for (JsonDataReader reader : jsonReaders.values()) {
                try {
                    reader.close();
                } catch (Exception e) {
                    logger.warn("Error while closing JsonDataReader: {}", e.getMessage());
                }
            }
            jsonReaders.clear();
            defaultReaderInstance = null;
            isInitialized = false;
            logger.info("All JSON data has been reset");
        } catch (Exception error) {
            ErrorHandler.logError(error, "resetJsonData", "Failed to reset JSON data");
            throw new JsonDataLoaderException("Failed to reset JSON data", error);
        }
    }

    /**
     * Custom exception for JsonDataLoader errors.
     */
    public static class JsonDataLoaderException extends RuntimeException {
        public JsonDataLoaderException(String message) {
            super(message);
        }

        public JsonDataLoaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}