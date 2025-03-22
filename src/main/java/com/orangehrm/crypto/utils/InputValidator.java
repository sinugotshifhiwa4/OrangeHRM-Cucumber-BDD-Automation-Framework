package com.orangehrm.crypto.utils;

import com.orangehrm.utils.ErrorHandler;

/**
 * Utility class for validating cryptographic input parameters.
 * Provides methods to validate inputs, sizes, and key parameters for cryptographic operations.
 */
public final class InputValidator {

    private InputValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates that the input is not null or empty.
     * Supports String and byte array validation.
     *
     * @param input     the input to validate
     * @param paramName the name of the parameter for error messages
     * @throws IllegalArgumentException if the input is null or empty
     * @throws RuntimeException if validation fails due to unexpected error
     */
    public static void validateInput(Object input, String paramName) {
        try {
            if (input == null ||
                    (input instanceof String s && s.isEmpty()) ||
                    (input instanceof byte[] b && b.length == 0)) {
                throw new IllegalArgumentException(paramName + " cannot be null or empty");
            }
        } catch (Exception error) {
            ErrorHandler.logError(error, "validateInput", "Failed to validate input");
            throw error;
        }
    }

    /**
     * Validates that the size parameter is positive.
     *
     * @param size      the size value to validate
     * @param parameter the name of the parameter for error messages
     * @throws IllegalArgumentException if the size is not positive
     * @throws RuntimeException if validation fails due to unexpected error
     */
    public static void validateSize(int size, String parameter) {
        try {
            if (size <= 0) {
                throw new IllegalArgumentException(parameter + " size must be positive");
            }
        } catch (Exception error) {
            ErrorHandler.logError(error, "validateSize", "Failed to validate size");
            throw error;
        }
    }

    /**
     * Validates that the key size is valid for AES encryption.
     * Valid sizes are 16 bytes (128 bits), 24 bytes (192 bits), or 32 bytes (256 bits).
     *
     * @param sizeInBytes the key size to validate in bytes
     * @throws IllegalArgumentException if the key size is not 16, 24, or 32 bytes
     * @throws RuntimeException if validation fails due to unexpected error
     */
    public static void validateKeySize(int sizeInBytes) {
        try {
            if (sizeInBytes != 16 && sizeInBytes != 24 && sizeInBytes != 32) {
                throw new IllegalArgumentException("AES key size must be 16, 24, or 32 bytes");
            }
        } catch (Exception error) {
            ErrorHandler.logError(error, "validateKeySize", "Failed to validate key size");
            throw error;
        }
    }

    public static void validateStringInput(String input, String inputType) {
        validateInput(input, inputType);
        if (input.isEmpty()) {
            throw new IllegalArgumentException(inputType + " cannot be empty");
        }
    }
}