package com.orangehrm.config.environments;

public class EnvironmentConfigConstants {

    // Base directory for all environment files
    private static final String ENV_DIRECTORY = "envs";

    private EnvironmentConfigConstants() {
    }

    public enum Environment {
        BASE("BASE"),
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
     * Environment file paths
     */
    public enum EnvironmentFilePath {
        BASE(".env"),
        DEV(".env.dev"),
        UAT(".env.uat"),
        PROD(".env.prod");

        private final String filename;

        EnvironmentFilePath(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }

        public String getFullPath() {
            return ENV_DIRECTORY + "/" + filename;
        }
    }

    /**
     * Environment secret keys
     */
    public enum EnvironmentSecretKey {
        DEV("DEV_SECRET_KEY"),
        UAT("UAT_SECRET_KEY"),
        PROD("PROD_SECRET_KEY");

        private final String keyName;

        EnvironmentSecretKey(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }
    }

    /**
     * Get the directory path for environment files
     *
     * @return The environment directory path
     */
    public static String getEnvironmentDirectoryPath() {
        return ENV_DIRECTORY;
    }
}