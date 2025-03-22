package com.orangehrm.config.properties;

public final class PropertyConfigConstants {

    // Base directory for properties files
    private static final String PROPERTIES_DIRECTORY = "src/main/resources/properties";

    private PropertyConfigConstants() {
    }

    public enum Environment {
        GLOBAL("GLOBAL"),
        DEV("DEV"),
        UAT("UAT"),
        PROD("PROD");

        private final String displayName;

        Environment(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Properties file paths
     */
    public enum PropertiesFilePath {
        GLOBAL("global-config.properties"),
        DEV("config-dev.properties"),
        UAT("config-uat.properties"),
        PROD("config-prod.properties");

        private final String filename;

        PropertiesFilePath(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }

        public String getFullPath() {
            return PROPERTIES_DIRECTORY + "/" + filename;
        }
    }

    /**
     * Get the directory path for properties files
     *
     * @return The properties directory path
     */
    public static String getPropDirectoryPath() {
        return PROPERTIES_DIRECTORY;
    }
}