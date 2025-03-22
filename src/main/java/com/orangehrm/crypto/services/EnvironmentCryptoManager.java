package com.orangehrm.crypto.services;

import com.orangehrm.config.environments.EnvironmentConfigConstants;
import com.orangehrm.config.properties.PropertyConfigConstants;
import com.orangehrm.config.properties.PropertyFileConfigManager;
import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.FileDirectoryManager;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CryptoException;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.orangehrm.config.environments.EnvironmentFileConfigManager.clearConfigCache;
import static com.orangehrm.config.environments.EnvironmentFileConfigManager.getConfiguration;
import static com.orangehrm.utils.ErrorHandler.validateParameters;

public class EnvironmentCryptoManager {

    private static final Logger logger = LoggerUtils.getLogger(EnvironmentCryptoManager.class);
    private static final String ENCRYPTED_LENGTH_THRESHOLD = "ENCRYPTED_LENGTH_THRESHOLD";
    private static final int ENCRYPTION_LENGTH_THRESHOLD = getEncryptedLengthThreshold();

    /**
     * Encrypt multiple environment variables at once
     *
     * @param configurationDisplayName Display name of the configuration
     * @param envName Environment name
     * @param environmentSecretKeyType Type of secret key to use
     * @param envVariables Variables to encrypt
     * @throws CryptoException If encryption fails
     * @throws IllegalArgumentException If any parameter is null or empty
     */
    public static void encryptEnvironmentVariables(
            String configurationDisplayName,
            String envName,
            String environmentSecretKeyType,
            String... envVariables
    ) throws CryptoException {
        validateParameters(configurationDisplayName, envName, environmentSecretKeyType);
        if (envVariables == null || envVariables.length == 0) {
            throw new IllegalArgumentException("Environment variables array cannot be null or empty");
        }

        try {
            for (String envVariable : envVariables) {
                if (envVariable == null || envVariable.trim().isEmpty()) {
                    logger.warn("Skipping null or empty environment variable name");
                    continue;
                }
                encryptEnvironmentVariables(configurationDisplayName, envName, environmentSecretKeyType, envVariable);
            }
        } catch (CryptoException error) {
            logger.error("Failed to encrypt multiple variables", error);
            throw error;
        } catch (Exception error) {
            logger.error("Unexpected error while encrypting variables", error);
            throw new CryptoException("Encryption failed due to unexpected error", error);
        }
    }

    /**
     * Encrypt a single environment variable
     *
     * @param configurationDisplayName Display name of the configuration
     * @param envName Environment name
     * @param environmentSecretKeyType Type of secret key to use
     * @param envVariable Variable to encrypt
     * @throws CryptoException If encryption fails
     * @throws IllegalArgumentException If any parameter is null or empty
     */
    public static void encryptEnvironmentVariables(
            String configurationDisplayName,
            String envName,
            String environmentSecretKeyType,
            String envVariable
    ) throws CryptoException {
        validateParameters(configurationDisplayName, envName, environmentSecretKeyType);
        if (envVariable == null || envVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("Environment variable name cannot be null or empty");
        }

        try {
            // Get current value
            String currentValue = getConfiguration(configurationDisplayName, envName).getProperty(envVariable);

            if (currentValue == null) {
                throw new IllegalArgumentException("Environment variable '" + envVariable + "' has null value");
            }

            if (isAlreadyEncrypted(currentValue)) {
                logger.info("Skipping encryption: Environment variable '{}' is already encrypted. Provide a plain-text value if re-encryption is required.", envVariable);
                return;
            }

            String encryptedValue = encryptValue(environmentSecretKeyType, currentValue);
            updateEnvironmentVariable(envName, envVariable, encryptedValue);
            logger.info("Variable '{}' encrypted successfully.", envVariable);

        } catch (CryptoException error) {
            // Just log and rethrow without wrapping to preserve the original exception
            logger.error("Failed to encrypt variable: {}", envVariable, error);
            throw error;
        } catch (Exception error) {
            logger.error("Unexpected error encrypting variable: {}", envVariable, error);
            throw new CryptoException("Encryption failed due to unexpected error", error);
        }
    }

    private static boolean isAlreadyEncrypted(String value) {
        return value != null && value.length() > ENCRYPTION_LENGTH_THRESHOLD;
    }

    private static String getEnvironmentVariable(String configurationDisplayName, String envName, String envVariable) {
        validateParameters(configurationDisplayName, envName, envVariable);

        try {
            String envValue = getConfiguration(configurationDisplayName, envName).getProperty(envVariable);
            if (envValue == null) {
                throw new IllegalArgumentException("Environment variable '" + envVariable + "' is null");
            }
            return envValue;
        } catch (Exception error) {
            logger.error("Failed to get environment variable: {}", envVariable, error);
            throw new RuntimeException("Failed to get environment variable: " + envVariable, error);
        }
    }

    private static String encryptValue(String environmentSecretKeyType, String envValue) throws CryptoException {
        if (environmentSecretKeyType == null || environmentSecretKeyType.trim().isEmpty()) {
            throw new IllegalArgumentException("Environment secret key type cannot be null or empty");
        }
        if (envValue == null) {
            throw new IllegalArgumentException("Value to encrypt cannot be null");
        }

        try {
            SecretKey secretKey = getSecretKey(
                    EnvironmentConfigConstants.Environment.BASE.getDisplayName(),
                    EnvironmentConfigConstants.EnvironmentFilePath.BASE.getFilename(),
                    environmentSecretKeyType);

            String encryptedValue = CryptoOperations.encrypt(secretKey, envValue);
            if (encryptedValue == null) {
                throw new IllegalArgumentException("Failed to encrypt value");
            }
            return encryptedValue;
        } catch (CryptoException error) {
            // Don't log the error here as it will be handled by the calling method
            throw error;
        } catch (Exception error) {
            logger.error("Failed to encrypt value", error);
            throw new CryptoException("Encryption failed due to unexpected error", error);
        }
    }

    /**
     * Save secret key in base environment file
     *
     * @param baseEnvironmentFilePath Path to base environment file
     * @param secretKeyVariable Variable name to store the key
     * @param encodedSecretKey Encoded secret key value
     * @throws IOException If file operations fail
     * @throws IllegalArgumentException If any parameter is null or empty
     */
    public static void saveSecretKeyInBaseEnvironment(
            String baseEnvironmentFilePath,
            String secretKeyVariable,
            String encodedSecretKey
    ) throws IOException {
        if (baseEnvironmentFilePath == null || baseEnvironmentFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Base environment file path cannot be null or empty");
        }
        if (secretKeyVariable == null || secretKeyVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret key variable name cannot be null or empty");
        }
        if (encodedSecretKey == null || encodedSecretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Encoded secret key cannot be null or empty");
        }

        try {
            ensureBaseEnvironmentFileExists();

            // Check if the secret key is already set
            if (isEnvironmentVariableSet(baseEnvironmentFilePath, secretKeyVariable)) {
                logger.info("The environment secret key '{}' already exists. Please remove it before updating.", secretKeyVariable);
                return; // Stop execution to prevent overwriting
            }

            // Update the environment variable if it doesn't already exist or is empty
            updateEnvironmentVariable(baseEnvironmentFilePath, secretKeyVariable, encodedSecretKey);
            logger.info("Secret key saved for variable '{}'", secretKeyVariable);

        } catch (IOException error) {
            logger.error("Failed to save secret key in base environment", error);
            throw error;
        } catch (Exception error) {
            logger.error("Unexpected error saving secret key", error);
            throw new IOException("Failed to save secret key due to unexpected error", error);
        }
    }

    private static void ensureBaseEnvironmentFileExists() throws IOException {
        try {
            FileDirectoryManager.createDirIfNotExists(EnvironmentConfigConstants.getEnvironmentDirectoryPath());
            FileDirectoryManager.createFileIfNotExists(
                    EnvironmentConfigConstants.getEnvironmentDirectoryPath(),
                    EnvironmentConfigConstants.EnvironmentFilePath.BASE.getFilename()
            );
        } catch (IOException error) {
            logger.error("Failed to ensure environment file exists", error);
            throw error;
        }
    }

    private static boolean isEnvironmentVariableSet(String environmentFilePath, String key) throws IOException {
        if (environmentFilePath == null || environmentFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Environment file path cannot be null or empty");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        try {
            List<String> envLines = Files.readAllLines(Paths.get(environmentFilePath));
            return envLines.stream()
                    .anyMatch(line -> line.startsWith(key + "=") && line.length() > (key.length() + 1));
        } catch (IOException error) {
            logger.error("Failed to check environment variable: {}", key, error);
            throw error;
        }
    }

    /**
     * Update an environment variable in the specified file
     *
     * @param filePath Path to the environment file
     * @param envVariable Variable name to update
     * @param value Value to set
     * @throws IllegalArgumentException If any parameter is null or empty
     */
    private static void updateEnvironmentVariable(String filePath, String envVariable, String value) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        if (envVariable == null || envVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("Environment variable name cannot be null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        try {
            Path resolvedPath = resolveEnvironmentFilePath(filePath);

            List<String> updatedLines = updateEnvironmentLines(Files.readAllLines(resolvedPath), envVariable, value);
            Files.write(resolvedPath, updatedLines);

            // Invalidate cache if this path could affect configurations
            clearConfigCache();

            logger.info("Environment variable '{}' updated in {}", envVariable, resolvedPath);
        } catch (IOException error) {
            ErrorHandler.logError(error, "updateEnvironmentVariable", "Failed to update environment variable: " + envVariable);
            throw new RuntimeException(error);
        }
    }

    /**
     * Resolves an environment file path, handling relative paths appropriately
     */
    private static Path resolveEnvironmentFilePath(String filePath) {
        Path path = Paths.get(filePath);

        // Resolve against environment directory only if the path is relative and has no parent
        if (!path.isAbsolute() && path.getParent() == null) {
            Path environmentDir = Paths.get(EnvironmentConfigConstants.getEnvironmentDirectoryPath());
            path = environmentDir.resolve(path).normalize();
        }

        return path;
    }

    private static List<String> updateEnvironmentLines(
            List<String> existingLines,
            String envVariable,
            String value
    ) {
        try {
            boolean[] isUpdated = {false};

            List<String> updatedLines = existingLines.stream()
                    .map(line -> {
                        if (line.startsWith(envVariable + "=")) {
                            isUpdated[0] = true;
                            return envVariable + "=" + value;
                        }
                        return line;
                    })
                    .collect(Collectors.toList());

            if (!isUpdated[0]) {
                updatedLines.add(envVariable + "=" + value);
            }

            return updatedLines;
        } catch (Exception error) {
            logger.error("Failed to update environment lines", error);
            throw new RuntimeException("Failed to update environment lines", error);
        }
    }

    /**
     * Decrypt multiple environment variables
     *
     * @param configurationDisplayName Display name of the configuration
     * @param envName Environment name
     * @param environmentSecretKeyType Type of secret key to use
     * @param requiredKeys Keys to decrypt
     * @return List of decrypted values
     * @throws IllegalArgumentException If any parameter is invalid
     */
    public static List<String> decryptEnvironmentVariables(
            String configurationDisplayName,
            String envName,
            String environmentSecretKeyType,
            String... requiredKeys
    ) {
        validateParameters(configurationDisplayName, envName, environmentSecretKeyType);

        if (requiredKeys == null || requiredKeys.length == 0) {
            return Collections.emptyList();
        }

        try {
            SecretKey secretKey = getSecretKey(
                    EnvironmentConfigConstants.Environment.BASE.getDisplayName(),
                    EnvironmentConfigConstants.EnvironmentFilePath.BASE.getFilename(),
                    environmentSecretKeyType);

            return Arrays.stream(requiredKeys)
                    .filter(key -> key != null && !key.trim().isEmpty())
                    .map(key -> decryptSingleKey(configurationDisplayName, envName, secretKey, key))
                    .collect(Collectors.toList());
        } catch (Exception error) {
            ErrorHandler.logError(error, "decryptEnvironmentVariables", "Failed to decrypt environment variables");
            throw new RuntimeException("Failed to decrypt environment variables", error);
        }
    }

    /**
     * Decrypt a single environment variable
     *
     * @param configurationDisplayName Display name of the configuration
     * @param envName Environment name
     * @param environmentSecretKeyType Type of secret key to use
     * @param requiredKey Key to decrypt
     * @return Decrypted value
     * @throws IllegalArgumentException If any parameter is invalid
     */
    public static String decryptEnvironmentVariable(
            String configurationDisplayName,
            String envName,
            String environmentSecretKeyType,
            String requiredKey
    ) {
        validateParameters(configurationDisplayName, envName, environmentSecretKeyType);
        if (requiredKey == null || requiredKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Required key cannot be null or empty");
        }

        try {
            SecretKey secretKey = getSecretKey(
                    EnvironmentConfigConstants.Environment.BASE.getDisplayName(),
                    EnvironmentConfigConstants.EnvironmentFilePath.BASE.getFilename(),
                    environmentSecretKeyType);
            return decryptSingleKey(configurationDisplayName, envName, secretKey, requiredKey);
        } catch (Exception error) {
            logger.error("Failed to decrypt key: {}", requiredKey, error);
            throw new RuntimeException("Failed to decrypt key: " + requiredKey, error);
        }
    }

    private static String decryptSingleKey(String configurationDisplayName, String envName, SecretKey secretKey, String key) {
        try {
            String encryptedValue = getConfiguration(configurationDisplayName, envName).getProperty(key);
            if (encryptedValue == null) {
                throw new IllegalArgumentException("Environment variable '" + key + "' has null value");
            }
            return CryptoOperations.decrypt(secretKey, encryptedValue);
        } catch (CryptoException error) {
            logger.error("Failed to decrypt key: {}", key, error);
            throw new RuntimeException("Failed to decrypt key: " + key, error);
        }
    }

    /**
     * Get a secret key from the environment configuration
     *
     * @param configurationDisplayName Display name of the configuration
     * @param envName Environment name
     * @param environmentSecretKey Key identifying the secret key
     * @return The secret key
     * @throws IllegalArgumentException If any parameter is invalid
     */
    public static SecretKey getSecretKey(String configurationDisplayName, String envName, String environmentSecretKey) {
        validateParameters(configurationDisplayName, envName, environmentSecretKey);

        try {
            return getConfiguration(configurationDisplayName, envName).getSecretKey(environmentSecretKey);
        } catch (Exception error) {
            logger.error("Failed to retrieve secret key", error);
            throw new RuntimeException("Failed to retrieve secret key", error);
        }
    }

    /**
     * Gets the encrypted length threshold from configuration.
     * If not specified in configuration, returns the default value of 90.
     *
     * @return the encrypted length threshold value
     */
    public static int getEncryptedLengthThreshold() {
        try {
            Optional<Integer> optionalThreshold = PropertyFileConfigManager.getConfiguration(
                            PropertyConfigConstants.Environment.GLOBAL.getDisplayName(),
                            PropertyConfigConstants.PropertiesFilePath.GLOBAL.getFullPath())
                    .getProperty(ENCRYPTED_LENGTH_THRESHOLD, Integer.class);
            return optionalThreshold.orElse(90);
        } catch (Exception error) {
            logger.error("Failed to get encrypted length threshold, using default value of 90", error);
            return 90; // Default value if there's an error
        }
    }
}