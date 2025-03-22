package com.orangehrm.crypto.services;

import com.orangehrm.crypto.utils.CryptoConfigConstants;
import com.orangehrm.utils.ErrorHandler;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import static com.orangehrm.crypto.utils.InputValidator.validateKeySize;
import static com.orangehrm.crypto.utils.InputValidator.validateSize;

/**
 * Utility class for generating cryptographic keys, initialization vectors, and salts.
 * This class uses SecureRandom for generating cryptographically strong random values.
 */
public final class SecureKeyGenerator {

    private static final ThreadLocal<SecureRandom> SECURE_RANDOM = ThreadLocal.withInitial(SecureRandom::new);
    private static final String IV_PARAMETER = "IV";
    private static final String SALT_PARAMETER = "Salt";

    private SecureKeyGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates a random initialization vector (IV) using the default size.
     *
     * @return byte array containing the generated IV
     * @throws RuntimeException if IV generation fails
     */
    public static byte[] generateIv() {
        try {
            return generateIv(CryptoConfigConstants.CryptoKeyParameters.IV_SIZE.getKeySize());
        } catch (Exception error) {
            ErrorHandler.logError(error, "generateIv", "Failed to generate IV");
            throw error;
        }
    }

    /**
     * Generates a random initialization vector (IV) with a specified size.
     *
     * @param size the desired size of the IV in bytes
     * @return byte array containing the generated IV
     * @throws IllegalArgumentException if size is invalid
     * @throws RuntimeException if IV generation fails
     */
    public static byte[] generateIv(int size) {
        try {
            validateSize(size, IV_PARAMETER);
            return generateRandomBytes(size);
        } catch (Exception error) {
            ErrorHandler.logError(error, "generateIv", "Failed to generate IV");
            throw error;
        }
    }

    /**
     * Generates a random salt using the default size.
     *
     * @return byte array containing the generated salt
     * @throws RuntimeException if salt generation fails
     */
    public static byte[] generateSalt() {
        try {
            return generateSalt(CryptoConfigConstants.CryptoKeyParameters.SALT_SIZE.getKeySize());
        } catch (Exception error) {
            ErrorHandler.logError(error, "generateSalt", "Failed to generate salt");
            throw error;
        }
    }

    /**
     * Generates a random salt with a specified size.
     *
     * @param size the desired size of the salt in bytes
     * @return byte array containing the generated salt
     * @throws IllegalArgumentException if size is invalid
     * @throws RuntimeException if salt generation fails
     */
    public static byte[] generateSalt(int size) {
        try {
            validateSize(size, SALT_PARAMETER);
            return generateRandomBytes(size);
        } catch (Exception error) {
            ErrorHandler.logError(error, "generateSalt", "Failed to generate salt");
            throw error;
        }
    }

    /**
     * Generates a new SecretKey using the default size (256 bits/32 bytes).
     *
     * @return SecretKey instance for use with AES encryption
     * @throws RuntimeException if key generation fails
     */
    public static SecretKey generateSecretKey() {
        try {
            return generateSecretKey(CryptoConfigConstants.CryptoKeyParameters.SECRET_KEY_SIZE.getKeySize());
        } catch (Exception error) {
            ErrorHandler.logError(error, "generateSecretKey", "Failed to generate secret key");
            throw error;
        }
    }

    /**
     * Generates a new SecretKey with a specified size.
     *
     * @param sizeInBytes the desired size of the key in bytes
     * @return SecretKey instance for use with AES encryption
     * @throws IllegalArgumentException if size is invalid
     * @throws RuntimeException if key generation fails
     */
    public static SecretKey generateSecretKey(int sizeInBytes) {
        try {
            validateKeySize(sizeInBytes);
            byte[] keyBytes = generateRandomBytes(sizeInBytes);
            return new SecretKeySpec(keyBytes, CryptoConfigConstants.CryptoAlgorithmTypes.AES.getAlgorithmName());
        } catch (Exception error) {
            ErrorHandler.logError(error, "generateSecretKey", "Failed to generate secret key");
            throw error;
        }
    }

    /**
     * Generates random bytes using SecureRandom.
     *
     * @param size the number of random bytes to generate
     * @return byte array containing random bytes
     * @throws IllegalArgumentException if size is negative
     * @throws RuntimeException if random byte generation fails
     */
    private static byte[] generateRandomBytes(int size) {
        try {
            if (size < 0) {
                throw new IllegalArgumentException("Size must be non-negative");
            }

            byte[] bytes = new byte[size];
            SECURE_RANDOM.get().nextBytes(bytes);
            return bytes;
        } catch (Exception error) {
            ErrorHandler.logError(error, "generateRandomBytes", "Failed to generate random bytes");
            throw error;
        }
    }
}