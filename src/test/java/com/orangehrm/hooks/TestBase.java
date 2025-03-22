package com.orangehrm.hooks;

import com.orangehrm.config.environments.EnvironmentConfigConstants;
import com.orangehrm.config.properties.PropertyConfigConstants;
import com.orangehrm.config.properties.PropertyFileConfigManager;
import com.orangehrm.crypto.services.EnvironmentCryptoManager;
import com.orangehrm.drivers.BrowserFactory;
import com.orangehrm.drivers.DriverFactory;
import com.orangehrm.pages.base.BasePage;
import com.orangehrm.pages.portalPages.LoginPage;
import com.orangehrm.pages.portalPages.PimMenuPage;
import com.orangehrm.pages.portalPages.SideMenuPage;
import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import com.orangehrm.utils.constants.Encryption;
import com.orangehrm.utils.constants.GlobalConstants;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class TestBase extends BasePage {


    private static final Logger logger = LoggerUtils.getLogger(TestBase.class);

    // Get Driver Instance
    protected final DriverFactory driverFactory = DriverFactory.getInstance();
    protected BrowserFactory browserFactory;


    // Pages
    public LoginPage loginPage;
    public SideMenuPage sideMenuPage;
    public PimMenuPage pimMenuPage;

    @BeforeAll
    public static void initializeTestSuite() {
        try {
            logger.info("Test environment initialization completed successfully.");
        } catch (Exception error) {
            ErrorHandler.logError(error, "initializeTestSuite", "Failed to initialize test suite");
            throw error;
        }
    }


    @Before
    public void setup() {
        try {
            // Skip browser initialization when running crypto operations
            skipBrowserInitializationIfNeeded();

            logger.info("Test environment setup completed successfully.");
        } catch (Exception error) {
            ErrorHandler.logError(error, "setup", "Failed to setup");
            throw error;
        }
    }

    @After
    public void tearDown() {
        try {
            driverFactory.quitDriver();
            logger.info("Test tear down completed successfully.");
        } catch (Exception error) {
            ErrorHandler.logError(error, "tearDown", "Failed to tear down");
            throw error;
        }
    }

    @AfterAll
    public static void cleanUpTestSuite() {
        try {
            logger.info("Test environment cleanup completed successfully.");
        } catch (Exception error) {
            ErrorHandler.logError(error, "cleanUpTestSuite", "Failed to clean up test suite");
            throw error;
        }
    }

    private void skipBrowserInitializationIfNeeded() {
        if (!Boolean.getBoolean("skipBrowserInitialization")) {
            initializeBrowserComponents();
        } else {
            logger.info("Skipping browser initialization for encryption tests.");
        }
    }

    private void initializePages(WebDriver driver) {
        loginPage = new LoginPage(driver);
        sideMenuPage = new SideMenuPage(driver);
        pimMenuPage = new PimMenuPage(driver);
    }

    private void initializeBrowserComponents() {
        try {
            browserFactory = new BrowserFactory();

            String browser = PropertyFileConfigManager.getConfiguration(
                    PropertyConfigConstants.Environment.GLOBAL.getDisplayName(),
                    PropertyConfigConstants.PropertiesFilePath.GLOBAL.getFullPath()
            ).getProperty(GlobalConstants.getChromeBrowser());

            browserFactory.initializeBrowser(browser);

            if (!driverFactory.hasDriver()) {
                String errorMessage = "WebDriver initialization failed for thread: " + Thread.currentThread().threadId();
                logger.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }

            initializePages(driverFactory.getDriver());

            String url = PropertyFileConfigManager.getConfiguration(
                    PropertyConfigConstants.Environment.UAT.getDisplayName(),
                    PropertyConfigConstants.PropertiesFilePath.UAT.getFullPath()
            ).getProperty(GlobalConstants.getPortalBaseUrl());

            driverFactory.navigateToUrl(url);
        } catch (Exception error) {
            ErrorHandler.logError(error, "initializeBrowserComponents", "Failed to initialize browser components");
            throw error;
        }
    }

    public List<String> decryptCredentials() {
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
            return decryptedCredentials;
        } catch (Exception error) {
            ErrorHandler.logError(error, "decryptionCredentials", "Failed to decrypt credentials");
            throw error;
        }
    }
}
