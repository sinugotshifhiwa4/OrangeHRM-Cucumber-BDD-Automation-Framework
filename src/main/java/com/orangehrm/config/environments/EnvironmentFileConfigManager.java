package com.orangehrm.config.environments;

import com.orangehrm.config.AbstractConfigManager;
import com.orangehrm.utils.Base64Utils;
import com.orangehrm.utils.ErrorHandler;
import io.github.cdimascio.dotenv.Dotenv;

import javax.crypto.SecretKey;
import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.orangehrm.utils.ErrorHandler.validateParameters;


public class EnvironmentFileConfigManager extends AbstractConfigManager<EnvironmentFileConfigManager> {

    // Cache for EnvironmentConfigManager instances
    private static final Map<String, EnvironmentFileConfigManager> configManagerCache = new ConcurrentHashMap<>();

    private Dotenv dotenv;

    public EnvironmentFileConfigManager(String configurationDisplayName, String envFileName) {
        super(configurationDisplayName, envFileName);
        try {
            loadEnvironment();
        } catch (Exception error) {
            logger.error("Failed to load environment '{}' with name '{}'", envFileName, configurationDisplayName);
            ErrorHandler.logError(error, "EnvironmentFileConfigManager Constructor", "Failed to load dotenv variables");
            throw error;
        }
    }

    private static EnvironmentFileConfigManager loadConfiguration(String configurationDisplayName, String envFileName) {
        try {
            return new EnvironmentFileConfigManager(configurationDisplayName, envFileName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "loadConfiguration", "Failed to create environment configuration");
            throw error;
        }
    }

    private void loadEnvironment() {
        this.dotenv = Dotenv.configure()
                .directory(EnvironmentConfigConstants.getEnvironmentDirectoryPath())
                .filename(configSource)
                .load();
    }

    @Override
    public String getProperty(String key) {
        try {
            // First check system environment variables
            String systemValue = System.getenv(key);
            if (systemValue != null) {
                ErrorHandler.logPropertySource(key, "system environment variable", systemValue);
                return systemValue;
            }

            // Then check dotenv file
            String value = dotenv.get(key);
            if (value == null || value.isEmpty()) {
                String message = String.format("Environment variable '%s' not found or empty in configuration '%s'",
                        key, configurationDisplayName);
                logger.error(message);
                throw new IllegalArgumentException(message);
            }

            return value;
        } catch (Exception error) {
            ErrorHandler.logError(error, "getProperty", "Failed to retrieve environment variable");
            throw error;
        }
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        try {
            // First check system environment variables
            String systemValue = System.getenv(key);
            if (systemValue != null) {
                ErrorHandler.logPropertySource(key, "system environment variable", systemValue);
                return systemValue;
            }

            // Then check dotenv file
            String value = dotenv.get(key, defaultValue);
            if (value.equals(defaultValue)) {
                logger.warn("Environment variable '{}' not found, using default '{}' in configuration '{}'",
                        key, defaultValue, configurationDisplayName);
            } else {
                logger.info("Retrieved environment variable '{}' from configuration '{}'", key, configurationDisplayName);
            }

            return value;
        } catch (Exception error) {
            ErrorHandler.logError(error, "getProperty", "Failed to retrieve environment variable with default");
            throw error;
        }
    }

    @Override
    public <ConversionType> Optional<ConversionType> getProperty(String key, Class<ConversionType> type) {
        try {
            // Check system environment variables first
            String systemValue = System.getenv(key);
            String value = systemValue != null ? systemValue : dotenv.get(key);

            if (value == null || value.isEmpty()) {
                logger.warn("Environment variable '{}' not found in configuration '{}'", key, configurationDisplayName);
                return Optional.empty();
            }

            // Type conversion
            ConversionType result = getConversionType(type, value);
            logger.debug("Retrieved and converted environment variable '{}' to type: {}", key, type.getSimpleName());
            return Optional.of(result);
        } catch (Exception error) {
            ErrorHandler.logError(error, "getProperty", "Failed to retrieve or convert environment variable");
            return Optional.empty();
        }
    }

    @Override
    public void reload() {
        try {
            loadEnvironment();
            logger.info("Environment configuration '{}' reloaded successfully", configurationDisplayName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "reload", "Failed to reload environment configuration");
            throw new RuntimeException(error);
        }
    }

    // Utility methods
    public SecretKey getSecretKey(String environmentSecretKey) {
        try {
            return Base64Utils.decodeSecretKey(getProperty(environmentSecretKey));
        } catch (Exception error) {
            ErrorHandler.logError(error, "getSecretKey", "Failed to retrieve secret key");
            throw error;
        }
    }

    /**
     * Retrieves configuration for the given environment, using cache when available.
     *
     * @param configurationDisplayName The display name for the configuration
     * @param envName The environment name
     * @return The environment configuration manager instance
     */
    public static EnvironmentFileConfigManager getConfiguration(String configurationDisplayName, String envName) {
        validateParameters(configurationDisplayName, envName);

        String cacheKey = generateCacheKey(configurationDisplayName, envName);

        // Check cache first to avoid unnecessary loading
        EnvironmentFileConfigManager cachedConfig = configManagerCache.get(cacheKey);
        if (cachedConfig != null) {
            logger.info("Using cached configuration for environment: {}, file: {}",
                    configurationDisplayName, envName);
            return cachedConfig;
        }

        // Load configuration if not in cache
        logger.info("Loading configuration for environment: {}, file: {}",
                configurationDisplayName, envName);
        try {
            EnvironmentFileConfigManager newConfig = EnvironmentFileConfigManager.loadConfiguration(
                    configurationDisplayName, envName);

            // Store in cache for future use
            configManagerCache.put(cacheKey, newConfig);
            return newConfig;
        } catch (Exception e) {
            logger.error("Unexpected error loading configuration for environment: {}, file: {}: {}",
                    configurationDisplayName, envName, e.getMessage(), e);
            throw new ConfigurationException("Unexpected error loading configuration for: " + cacheKey, e);
        }
    }

    /**
     * Generates a consistent cache key from configuration parameters.
     */
    private static String generateCacheKey(String configurationDisplayName, String envName) {
        return configurationDisplayName + ":" + envName;
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