package com.orangehrm.config.properties;

import com.orangehrm.config.AbstractConfigManager;
import com.orangehrm.utils.ErrorHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.orangehrm.utils.ErrorHandler.logPropertySource;
import static com.orangehrm.utils.ErrorHandler.validateParameters;

public class PropertyFileConfigManager extends AbstractConfigManager<PropertyFileConfigManager> {

    // Cache for EnvironmentConfigManager instances
    private static final Map<String, PropertyFileConfigManager> configManagerCache = new ConcurrentHashMap<>();

    private final Properties properties;
    private final Path configPath;

    public PropertyFileConfigManager(String configurationDisplayName, String propertiesFilePath) {
        super(configurationDisplayName, propertiesFilePath);
        this.properties = new Properties();
        this.configPath = Paths.get(propertiesFilePath);

        try {
            validateFilePath();
            loadProperties();
            logger.info("Configuration '{}' successfully loaded from: {}",
                    configurationDisplayName, propertiesFilePath);
        } catch (IOException e) {
            ErrorHandler.logError(e, "Constructor",
                    "Failed to initialize properties from file: " + propertiesFilePath);
            throw new ConfigurationException("Failed to initialize properties configuration", e);
        }
    }

    public static PropertyFileConfigManager loadConfiguration(String configurationDisplayName, String propertiesFileName) {
        try {
            return new PropertyFileConfigManager(configurationDisplayName, propertiesFileName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "loadConfiguration",
                    "Failed to create properties configuration: " + propertiesFileName);
            throw new ConfigurationException("Failed to load properties configuration", error);
        }
    }

    @Override
    public String getProperty(String key) {
        // First check system properties
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            logPropertySource(key, "system property", systemValue);
            return systemValue;
        }

        // Then check properties file
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            logger.warn("Property '{}' not found or empty in properties file", key);
            throw new ConfigurationException("Property '" + key + "' not found or empty in properties file");
        }

        return value;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        try {
            // First check system properties
            String systemValue = System.getProperty(key);
            if (systemValue != null) {
                logPropertySource(key, "system property", systemValue);
                return systemValue;
            }

            // Then check properties file
            String value = properties.getProperty(key);
            if (value == null || value.isEmpty()) {
                logger.info("Property '{}' not found, using default: '{}'", key, defaultValue);
                return defaultValue;
            }

            return value;
        } catch (Exception error) {
            ErrorHandler.logError(error, "getValue", "Failed to retrieve property: " + key);
            return defaultValue; // Return default on any error
        }
    }

    @Override
    public <ConversionType> Optional<ConversionType> getProperty(String key, Class<ConversionType> type) {
        try {
            // Check system properties first
            String systemValue = System.getProperty(key);
            String value = systemValue != null ? systemValue : properties.getProperty(key);

            if (value == null || value.isEmpty()) {
                logger.warn("Property '{}' not found or empty", key);
                return Optional.empty();
            }

            // Type conversion
            ConversionType result = getConversionType(type, value);
            logger.debug("Retrieved and converted property '{}' to type: {}", key, type.getSimpleName());
            return Optional.of(result);
        } catch (Exception error) {
            ErrorHandler.logError(error, "getValue",
                    "Failed to retrieve or convert property '" + key + "' to type: " + type.getSimpleName());
            return Optional.empty();
        }
    }

    @Override
    public void reload() {
        try {
            properties.clear();
            loadProperties();
            logger.info("Configuration '{}' reloaded successfully from: {}",
                    configurationDisplayName, configSource);
        } catch (IOException error) {
            ErrorHandler.logError(error, "reload", "Failed to reload properties file: " + configSource);
            throw new ConfigurationException("Failed to reload properties configuration", error);
        }
    }

    private void loadProperties() throws IOException {
        if (!Files.exists(configPath)) {
            throw new FileNotFoundException("Properties file not found: " + configPath);
        }

        try (FileInputStream inputStream = new FileInputStream(configPath.toFile())) {
            properties.load(inputStream);
            logger.debug("Loaded {} properties from file", properties.size());
        }
    }

    private void validateFilePath() {
        if (configPath == null || configSource == null || configSource.isEmpty()) {
            throw new IllegalArgumentException("Configuration file path cannot be null or empty");
        }
    }

    /**
     * Generates a consistent cache key from configuration parameters.
     */
    private static String generateCacheKey(String configurationDisplayName, String envName) {
        return configurationDisplayName + ":" + envName;
    }

    public static PropertyFileConfigManager getConfiguration(String configurationDisplayName, String envName) {
        validateParameters(configurationDisplayName, envName);

        String cacheKey = generateCacheKey(configurationDisplayName, envName);

        // Check cache
        PropertyFileConfigManager cachedConfig = configManagerCache.get(cacheKey);
        if (cachedConfig != null) {
            logger.info("Using cached configuration for environment: {}, file: {}",
                    configurationDisplayName, envName);
            return cachedConfig;
        }

        // Load configuration
        logger.info("Loading configuration for: {}", cacheKey);
        try {
            PropertyFileConfigManager newConfig = PropertyFileConfigManager.loadConfiguration(
                    configurationDisplayName, envName);

            configManagerCache.put(cacheKey, newConfig);
            return newConfig;
        } catch (Exception e) {
            logger.error("Error loading configuration for {}: {}", cacheKey, e.getMessage(), e);
            throw new IllegalStateException("Failed to load configuration for: " + cacheKey, e);
        }
    }

    /**
     * Clears all entries from the configuration cache
     */
    public static void clearConfigCache() {
        configManagerCache.clear();
        logger.info("Configuration cache cleared");
    }

    // Custom exception class for configuration errors
    public static class ConfigurationException extends RuntimeException {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}