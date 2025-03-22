package com.orangehrm.crypto.services;

import com.orangehrm.crypto.utils.CryptoConfigConstants;
import com.orangehrm.utils.Base64Utils;
import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.orangehrm.crypto.services.SecureKeyGenerator.generateIv;
import static com.orangehrm.crypto.services.SecureKeyGenerator.generateSalt;
import static com.orangehrm.crypto.utils.InputValidator.validateStringInput;

public class CryptoOperations {

    private static final Logger logger = LoggerUtils.getLogger(CryptoOperations.class);
    private CryptoOperations() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String encrypt(SecretKey key, String data) throws CryptoException {
        validateInput(key);
        validateStringInput(data, "Data");

        SecretKeySpec derivedKey = null;
        try {
            byte[] salt = generateSalt();
            byte[] iv = generateIv();
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            derivedKey = deriveKey(key, salt);
            Cipher cipher = initializeCipher(iv, derivedKey, Cipher.ENCRYPT_MODE);
            byte[] cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            EncryptionComponents components = new EncryptionComponents(salt, iv, cipherText);
            return Base64Utils.encodeArray(components.combine());
        } catch (Exception error) {
            ErrorHandler.logError(error, "encrypt", "Failed to encrypt data");
            throw new CryptoException("Encryption failed", error);
        } finally {
            clearKeyIfNotNull(derivedKey);
        }
    }

    public static String decrypt(SecretKey key, String encryptedData) throws CryptoException {
        validateInput(key);
        validateStringInput(encryptedData, "Encrypted Data");

        SecretKeySpec derivedKey = null;
        try {
            byte[] combined = Base64Utils.decodeToArray(encryptedData);
            EncryptionComponents components = EncryptionComponents.extract(combined);

            derivedKey = deriveKey(key, components.salt());
            Cipher cipher = initializeCipher(components.iv(), derivedKey, Cipher.DECRYPT_MODE);
            byte[] decryptedBytes = cipher.doFinal(components.cipherText());

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (AEADBadTagException error) {
            ErrorHandler.logError(error, "decrypt", "Tag mismatch: Incorrect key, IV, or ciphertext corruption.");
            throw new CryptoException("Decryption failed: Tag mismatch. Ensure correct key and IV are used.", error);
        } catch (Exception error) {
            ErrorHandler.logError(error, "decrypt", "Failed to decrypt data");
            throw new CryptoException("Decryption failed", error);
        } finally {
            clearKeyIfNotNull(derivedKey);
        }
    }

    public static CompletableFuture<String> decryptAsync(SecretKey key, String encryptedData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return decrypt(key, encryptedData);
            } catch (CryptoException error) {
                ErrorHandler.logError(error, "decryptAsync", "Failed to decrypt data");
                throw new RuntimeException("Failed to decrypt data", error);
            }
        });
    }

    private static SecretKeySpec deriveKey(SecretKey key, byte[] salt) {
        byte[] keyBytes = key.getEncoded();
        try {
            Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withSalt(salt)
                    .withIterations(CryptoConfigConstants.CryptoArgon2Parameters.ITERATIONS.getParameterValue())
                    .withMemoryAsKB(CryptoConfigConstants.CryptoArgon2Parameters.MEMORY.getParameterValue())
                    .withParallelism(CryptoConfigConstants.CryptoArgon2Parameters.PARALLELISM.getParameterValue())
                    .build();

            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(params);

            byte[] result = new byte[CryptoConfigConstants.CryptoKeyParameters.SECRET_KEY_SIZE.getKeySize()];
            generator.generateBytes(keyBytes, result);

            return new SecretKeySpec(result, CryptoConfigConstants.CryptoAlgorithmTypes.AES.getAlgorithmName());
        } catch (Exception error) {
            ErrorHandler.logError(error, "deriveKey", "Failed to derive key");
            throw new IllegalStateException("Failed to derive key", error);
        } finally {
            Arrays.fill(keyBytes, (byte) 0);  // Clear sensitive data
        }
    }

    private static Cipher initializeCipher(byte[] iv, SecretKeySpec key, int mode) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(CryptoConfigConstants.CryptoAlgorithmTypes.AES_GCM_NO_PADDING.getAlgorithmName());
            cipher.init(mode, key, new GCMParameterSpec(CryptoConfigConstants.CryptoKeyParameters.GCM_TAG_KEY_SIZE.getKeySize(), iv));
            return cipher;
        } catch (Exception error) {
            ErrorHandler.logError(error, "initializeCipher", "Failed to initialize cipher");
            throw error;
        }
    }

    private static void validateInput(Object input) {
        if (input == null) {
            throw new IllegalArgumentException("Secret Key" + " cannot be null");
        }
    }

    private static void clearKeyIfNotNull(SecretKeySpec key) {
        if (key != null) {
            try {
                byte[] encoded = key.getEncoded();
                if (encoded != null) {
                    Arrays.fill(encoded, (byte) 0);
                }
            } catch (Exception error) {
                // Log but continue, as this is cleanup code
                ErrorHandler.logError(error, "clearKeyIfNotNull", "Failed to clear key material");
            }
        }
    }

    private record EncryptionComponents(byte[] salt, byte[] iv, byte[] cipherText) {
        public static EncryptionComponents extract(byte[] combined) {
            try {
                int saltSize = CryptoConfigConstants.CryptoKeyParameters.SALT_SIZE.getKeySize();
                int ivSize = CryptoConfigConstants.CryptoKeyParameters.IV_SIZE.getKeySize();
                int cipherTextSize = combined.length - saltSize - ivSize;

                if (combined.length < saltSize + ivSize) {
                    logger.error("Combined byte array is too short.");
                    throw new IllegalArgumentException("Combined byte array is too short.");
                }

                ByteBuffer buffer = ByteBuffer.wrap(combined);
                byte[] salt = new byte[saltSize];
                byte[] iv = new byte[ivSize];
                byte[] cipherText = new byte[cipherTextSize];

                buffer.get(salt);
                buffer.get(iv);
                buffer.get(cipherText);

                return new EncryptionComponents(salt, iv, cipherText);
            } catch (Exception error) {
                ErrorHandler.logError(error, "extract", "Failed to extract components");
                throw new IllegalStateException("Failed to extract components", error);
            }
        }

        public byte[] combine() {
            try {
                return ByteBuffer.allocate(salt.length + iv.length + cipherText.length)
                        .put(salt)
                        .put(iv)
                        .put(cipherText)
                        .array();
            } catch (Exception error) {
                ErrorHandler.logError(error, "combine", "Failed to combine components");
                throw new IllegalStateException("Failed to combine components", error);
            }
        }
    }
}