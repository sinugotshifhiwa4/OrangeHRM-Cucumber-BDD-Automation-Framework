package com.orangehrm.crypto.utils;

public class CryptoConfigConstants {

    private CryptoConfigConstants() {
    }

    /**
     * Key size parameters for various cryptographic operations
     */
    public enum CryptoKeyParameters {
        GCM_TAG_KEY_SIZE(128),
        SECRET_KEY_SIZE(32),
        IV_SIZE(16),
        SALT_SIZE(32);

        private final int keySize;

        CryptoKeyParameters(int keySize) {
            this.keySize = keySize;
        }

        public int getKeySize() {
            return keySize;
        }
    }

    /**
     * Parameter settings for Argon2 password hashing
     */
    public enum CryptoArgon2Parameters {
        ITERATIONS(3),
        MEMORY(65536),  // 32768 -> 32 MB and 65536 -> 64 MB
        PARALLELISM(4);

        private final int parameterValue;

        CryptoArgon2Parameters(int parameterValue) {
            this.parameterValue = parameterValue;
        }

        public int getParameterValue() {
            return parameterValue;
        }
    }

    /**
     * Cryptographic algorithm names and transformations
     */
    public enum CryptoAlgorithmTypes {
        AES("AES"),
        AES_GCM_NO_PADDING("AES/GCM/NoPadding"),
        AES_CBC_PKCS5("AES/CBC/PKCS5Padding"),
        PBKDF2("PBKDF2WithHmacSHA256"),
        HMAC_SHA256("HmacSHA256");

        private final String algorithmName;

        CryptoAlgorithmTypes(String algorithmName) {
            this.algorithmName = algorithmName;
        }

        public String getAlgorithmName() {
            return algorithmName;
        }
    }
}