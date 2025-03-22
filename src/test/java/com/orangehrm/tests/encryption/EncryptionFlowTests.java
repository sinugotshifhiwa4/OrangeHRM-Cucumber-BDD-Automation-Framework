package com.orangehrm.tests.encryption;

import com.orangehrm.config.environments.EnvironmentConfigConstants;
import com.orangehrm.crypto.services.EnvironmentCryptoManager;
import com.orangehrm.crypto.services.SecureKeyGenerator;
import com.orangehrm.utils.Base64Utils;
import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import com.orangehrm.utils.constants.Encryption;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

public class EncryptionFlowTests {

    private static final Logger logger = LoggerUtils.getLogger(EncryptionFlowTests.class);

//    @Test
//    @Order(1)
//    @Tag("uat-encryption")
//    @DisplayName("Generate Secret Key for UAT Environment")
//    public void generateSecretKey() throws IOException {
//        try {
//            // Generate a new secret key
//            SecretKey generatedSecretKey = SecureKeyGenerator.generateSecretKey();
//
//            // Save the secret key in the base environment
//            EnvironmentCryptoManager.saveSecretKeyInBaseEnvironment(
//                    EnvironmentConfigConstants.EnvironmentFilePath.BASE.getFullPath(),
//                    EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
//                    Base64Utils.encodeSecretKey(generatedSecretKey)
//            );
//            logger.info("Secret key generation process completed");
//        } catch (Exception error) {
//            ErrorHandler.logError(error, "generateSecretKey", "Failed to generate secret key");
//            throw error;
//        }
//    }
//
//    @Test
//    @Order(2)
//    @Tag("uat-encryption")
//    @DisplayName("Encrypt Credentials for UAT Environment")
//    public void encryptCredentials() throws CryptoException {
//        try {
//            // Run Encryption
//            EnvironmentCryptoManager.encryptEnvironmentVariables(
//                    EnvironmentConfigConstants.Environment.UAT.getDisplayName(),
//                    EnvironmentConfigConstants.EnvironmentFilePath.UAT.getFilename(),
//                    EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
//                    Encryption.getPortalUsername(), Encryption.getPortalPassword()
//            );
//            logger.info("Encryption process completed");
//        } catch (Exception error) {
//            ErrorHandler.logError(error, "encryptCredentials", "Failed to encrypt credentials");
//            throw error;
//        }
//    }


    public static void main(String [] args) throws CryptoException, IOException {
        //generateSecretKey();
        //encryptCredentials();
        decryptionCredentials();
    }

    public static void generateSecretKey() throws IOException {
        try {
            // Generate a new secret key
            SecretKey generatedSecretKey = SecureKeyGenerator.generateSecretKey();

            // Save the secret key in the base environment
            EnvironmentCryptoManager.saveSecretKeyInBaseEnvironment(
                    EnvironmentConfigConstants.EnvironmentFilePath.BASE.getFullPath(),
                    EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
                    Base64Utils.encodeSecretKey(generatedSecretKey)
            );
            logger.info("Secret key generation process completed");
        } catch (Exception error) {
            ErrorHandler.logError(error, "generateSecretKey", "Failed to generate secret key");
            throw error;
        }
    }

    public static void encryptCredentials() throws CryptoException {
        try {
            // Run Encryption
            EnvironmentCryptoManager.encryptEnvironmentVariables(
                    EnvironmentConfigConstants.Environment.UAT.getDisplayName(),
                    EnvironmentConfigConstants.EnvironmentFilePath.UAT.getFilename(),
                    EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
                    Encryption.getPortalUsername(), Encryption.getPortalPassword()
            );
            logger.info("Encryption process completed");
        } catch (Exception error) {
            ErrorHandler.logError(error, "encryptCredentials", "Failed to encrypt credentials");
            throw error;
        }
    }

    public static void decryptionCredentials() {
        try {
            // Run Decryption
            List<String> decryptedCredentials = EnvironmentCryptoManager.decryptEnvironmentVariables(
                    EnvironmentConfigConstants.Environment.UAT.getDisplayName(),
                    EnvironmentConfigConstants.EnvironmentFilePath.UAT.getFilename(),
                    EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
                    Encryption.getPortalUsername(), Encryption.getPortalPassword()
            );

            System.out.println("Decrypted UserName: " + decryptedCredentials.get(0));
            System.out.println("Decrypted Password: " + decryptedCredentials.get(1));

            logger.info("Decryption process completed");
        } catch (Exception error) {
            ErrorHandler.logError(error, "decryptionCredentials", "Failed to decrypt credentials");
            throw error;
        }
    }
}