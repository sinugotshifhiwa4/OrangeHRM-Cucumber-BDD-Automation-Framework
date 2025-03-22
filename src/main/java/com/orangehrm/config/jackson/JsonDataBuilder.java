package com.orangehrm.config.jackson;


import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;

public class JsonDataBuilder {

    private final Logger logger = LoggerUtils.getLogger(JsonDataBuilder.class);
    private String filePath;
    private boolean cacheEnabled = false; // Default value
    private String schemaPath;

    // No-argument constructor
    public JsonDataBuilder() {}


    /**
     * Sets the file path for the JSON file.
     *
     * @param filePath The path to the JSON file
     * @return This requestBuilder instance
     */
    public JsonDataBuilder setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    /**
     * Enables or disables caching.
     *
     * @param cacheEnabled Whether caching is enabled
     * @return This requestBuilder instance
     */
    public JsonDataBuilder setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        return this;
    }

    /**
     * Sets the schema path for JSON validation.
     *
     * @param schemaPath The path to the JSON schema
     * @return This requestBuilder instance
     */
    public JsonDataBuilder setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public String getSchemaPath() {
        return schemaPath;
    }


    /**
     * Builds a {@link JsonDataReader} instance with the configured parameters.
     *
     * @return A new {@link JsonDataReader} instance
     * @throws IllegalStateException If required parameters (e.g., filePath) are not set
     */
    public JsonDataReader build() {
        try {
            if (filePath == null || filePath.isEmpty()) {
                throw new IllegalStateException("filePath must be set");
            }
            return new JsonDataReader(this);
        } catch (IllegalStateException error) {
            ErrorHandler.logError(error, "build", "Failed to build JsonDataReader");
            throw new IllegalStateException("Failed to build JsonDataReader", error);
        }
    }

    /**
     * Static factory method to create a new {@link JsonDataBuilder} instance.
     *
     * @return A new {@link JsonDataBuilder} instance
     */
    public static JsonDataBuilder builder() {
        return new JsonDataBuilder();
    }
}