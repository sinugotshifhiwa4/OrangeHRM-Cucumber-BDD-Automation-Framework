package com.orangehrm.config;

import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public abstract class AbstractConfigManager<T extends AbstractConfigManager<T>> {

    protected static final Logger logger = LoggerUtils.getLogger(AbstractConfigManager.class);
    protected final String configurationDisplayName;
    protected final String configSource;

    protected AbstractConfigManager(String configurationDisplayName, String configSource) {
        this.configurationDisplayName = configurationDisplayName;
        this.configSource = configSource;
    }

    /**
     * Get a value with type conversion
     *
     * @param type Desired return type
     * @return Converted value of the specified type
     */
    protected <ConversionType> ConversionType getConversionType(Class<ConversionType> type, String value) {
        try {
            Object convertedValue = switch (type.getSimpleName()) {
                case "String" -> value;
                case "Integer" -> Integer.parseInt(value);
                case "Boolean" -> Boolean.parseBoolean(value);
                case "Double" -> Double.parseDouble(value);
                case "Long" -> Long.parseLong(value);
                default -> throw new UnsupportedOperationException("Unsupported type conversion");
            };

            @SuppressWarnings("unchecked")
            ConversionType result = (ConversionType) convertedValue;
            return result;
        } catch (Exception error) {
            ErrorHandler.logError(error, "getConversionType", "Failed to convert value");
            throw error;
        }
    }

    /**
     * Get a raw string value from the configuration
     *
     * @param key Configuration key
     * @return The value as a string
     * @throws IllegalArgumentException if key not found
     */
    public abstract String getProperty(String key);

    /**
     * Get a raw string value from the configuration with a default
     *
     * @param key Configuration key
     * @param defaultValue Default value to return if key not found
     * @return The value as a string
     */
    public abstract String getProperty(String key, String defaultValue);

    /**
     * Get a typed value from the configuration
     *
     * @param key Configuration key
     * @param type Desired return type
     * @param <ConversionType> Type to convert to
     * @return Optional containing the converted value
     */
    public abstract <ConversionType> Optional<ConversionType> getProperty(
            String key,
            Class<ConversionType> type);

    /**
     * Reload the configuration from its source
     */
    public abstract void reload();
}
