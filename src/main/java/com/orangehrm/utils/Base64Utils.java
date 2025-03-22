package com.orangehrm.utils;

import com.orangehrm.crypto.utils.CryptoConfigConstants;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.orangehrm.crypto.utils.InputValidator.validateInput;


/**
 * Utility class for Base64 encoding and decoding operations.
 * Provides methods for handling strings, byte arrays, and secret keys with proper validation and error handling.
 * All methods perform input validation and use UTF-8 encoding for string operations.
 */
public final class Base64Utils {

    private static final String BYTE_ARRAY_PARAMETER = "Byte array";
    private static final String STRING_PARAMETER = "String";
    private static final String ENCODED_KEY_PARAMETER = "Encoded key";
    private static final String SECRET_KEY_PARAMETER = "Secret Key";

    private Base64Utils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Encodes a byte array to Base64 string.
     *
     * @param data the byte array to encode
     * @return the Base64 encoded string
     * @throws IllegalArgumentException if the input is null or empty
     * @throws RuntimeException if encoding fails
     */
    public static String encodeArray(byte[] data) {
        validateInput(data, BYTE_ARRAY_PARAMETER);
        try {
            return Base64.getEncoder().encodeToString(data);
        } catch (Exception error) {
            ErrorHandler.logError(error, "encodeArray", "Failed to encode byte array to base64");
            throw error;
        }
    }

    /**
     * Decodes a Base64 string to byte array.
     *
     * @param base64String the Base64 string to decode
     * @return the decoded byte array
     * @throws IllegalArgumentException if the input is null, empty, or invalid Base64
     * @throws RuntimeException if decoding fails
     */
    public static byte[] decodeToArray(String base64String) {
        validateInput(base64String, STRING_PARAMETER);
        try {
            return Base64.getDecoder().decode(base64String);
        } catch (Exception error) {
            ErrorHandler.logError(error, "decodeToArray", "Failed to decode base64 to byte array");
            throw error;
        }
    }

    /**
     * Encodes a string to Base64 using UTF-8 encoding.
     *
     * @param data the string to encode
     * @return the Base64 encoded string
     * @throws IllegalArgumentException if the input is null or empty
     * @throws RuntimeException if encoding fails
     */
    public static String encodeString(String data) {
        validateInput(data, STRING_PARAMETER);
        try {
            return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
            ErrorHandler.logError(error, "encodeString", "Failed to encode string to base64");
            throw error;
        }
    }

    /**
     * Decodes a Base64 string back to a regular string using UTF-8 encoding.
     *
     * @param base64String the Base64 string to decode
     * @return the decoded string
     * @throws IllegalArgumentException if the input is null, empty, or invalid Base64
     * @throws RuntimeException if decoding fails
     */
    public static String decodeToString(String base64String) {
        validateInput(base64String, STRING_PARAMETER);
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception error) {
            ErrorHandler.logError(error, "decodeToString", "Failed to decode base64 to string");
            throw error;
        }
    }

    /**
     * Encodes a SecretKey to Base64 string.
     *
     * @param secretKey the SecretKey to encode
     * @return the Base64 encoded string
     * @throws IllegalArgumentException if the input is null
     * @throws RuntimeException if encoding fails
     */
    public static String encodeSecretKey(SecretKey secretKey) {
        validateInput(secretKey, SECRET_KEY_PARAMETER);
        try {
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception error) {
            ErrorHandler.logError(error, "encodeSecretKey", "Failed to encode secret key");
            throw error;
        }
    }

    /**
     * Decodes a Base64 string to a SecretKey using AES algorithm.
     *
     * @param encodedKey the Base64 encoded key string
     * @return the decoded SecretKey
     * @throws IllegalArgumentException if the input is null, empty, or invalid Base64
     * @throws RuntimeException if decoding fails
     */
    public static SecretKey decodeSecretKey(String encodedKey) {
        validateInput(encodedKey, ENCODED_KEY_PARAMETER);
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            return new SecretKeySpec(decodedKey, CryptoConfigConstants.CryptoAlgorithmTypes.AES.getAlgorithmName());
        } catch (Exception error) {
            ErrorHandler.logError(error, "decodeSecretKey", "Failed to decode secret key");
            throw error;
        }
    }
}