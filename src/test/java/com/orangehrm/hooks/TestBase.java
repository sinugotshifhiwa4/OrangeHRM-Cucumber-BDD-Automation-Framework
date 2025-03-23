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
import io.cucumber.java.*;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * AfterStep hook that captures and attaches a screenshot to the Cucumber scenario.
     * Captures screenshots only for failed scenarios to optimize test execution.
     * Utilizes direct byte capture for performance efficiency.
     */
    @AfterStep
    public void captureAndAttachFailureScreenshot(Scenario scenario) {

        // Skip screenshot capture if the scenario passed
        if (!scenario.isFailed()) {
            return;
        }

        String scenarioName = sanitizeScenarioName(scenario.getName());
        String timestamp = getCurrentTimestamp();
        String screenshotName = String.format("%s_FAILED_%s", scenarioName, timestamp);

        logger.info("Capturing failure screenshot for scenario: {}", scenario.getName());

        try {
            byte[] screenshotBytes = captureScreenshotAsBytes(screenshotName);

            if (screenshotBytes == null || screenshotBytes.length == 0) {
                logger.warn("Screenshot capture returned empty bytes for scenario: {}", scenario.getName());
                return;
            }

            scenario.attach(screenshotBytes, "image/png", screenshotName);
            logger.info("Successfully attached failure screenshot: {}", screenshotName);

        } catch (WebDriverException error) {
            ErrorHandler.logError(error, "captureScenarioScreenshot",
                    "WebDriver failed to capture screenshot for scenario: " + scenario.getName());
        } catch (Exception error) {
            ErrorHandler.logError(error, "captureScenarioScreenshot",
                    "Failed to capture or attach screenshot for scenario: " + scenario.getName());
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

    /**
     * Generates a formatted timestamp for consistent filename usage.
     *
     * @return A string representation of the current timestamp.
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    /**
     * Sanitizes scenario name for safe filename usage.
     *
     * @param name The original scenario name.
     * @return A sanitized version safe for filenames.
     */
    private String sanitizeScenarioName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "unnamed_scenario";
        }

        return name.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_-]", "")
                .substring(0, Math.min(name.length(), 100)); // Limit length to avoid filesystem issues
    }
}
