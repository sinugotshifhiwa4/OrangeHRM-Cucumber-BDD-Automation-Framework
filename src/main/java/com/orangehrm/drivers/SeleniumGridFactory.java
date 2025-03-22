package com.orangehrm.drivers;

import com.orangehrm.config.properties.PropertyConfigConstants;
import com.orangehrm.config.properties.PropertyFileConfigManager;
import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.orangehrm.drivers.BrowserOptionsUtils.*;


public class SeleniumGridFactory {

    private static final Logger logger = LoggerUtils.getLogger(SeleniumGridFactory.class);
    private static final String SELENIUM_GRID_URL = "SELENIUM_GRID_URL";

    public void initializeRemoteBrowser(String browserName, String... arguments) {
        try {
            WebDriver driver = createRemoteDriver(browserName, arguments);
            DriverFactory driverFactory = DriverFactory.getInstance();

            if (driverFactory != null) {
                driverFactory.setDriver(driver);
            } else {
                logger.warn("DriverFactory instance is null. WebDriver will not be stored.");
            }

            logger.info("Initialized remote {} browser on Selenium Grid", browserName);
        } catch (IllegalArgumentException e) {
            logger.warn("Unsupported browser requested: {}", browserName);
            throw new RuntimeException("Unsupported browser: " + browserName, e);
        } catch (Exception error) {
            ErrorHandler.logError(error, "initializeRemoteBrowser", "Initialization failed");
            throw new RuntimeException("Remote browser initialization failed", error);
        }
    }

    private WebDriver createRemoteDriver(String browserName, String... arguments) {
        try {

            URL gridUrl = URI.create(getSeleniumGridUrl()).toURL();
            DesiredCapabilities capabilities = new DesiredCapabilities();

            switch (browserName.toLowerCase()) {
                case "chrome":
                    ChromeOptions chromeOptions = getChromeOptions(arguments);
                    capabilities.merge(chromeOptions);
                    return new RemoteWebDriver(gridUrl, capabilities);
                case "firefox":
                    FirefoxOptions firefoxOptions = getFirefoxOptions(arguments);
                    capabilities.merge(firefoxOptions);
                    return new RemoteWebDriver(gridUrl, capabilities);
                case "edge":
                    EdgeOptions edgeOptions = getEdgeOptions(arguments);
                    capabilities.merge(edgeOptions);
                    return new RemoteWebDriver(gridUrl, capabilities);
                default:
                    throw new IllegalArgumentException("Unsupported browser: " + browserName);
            }
        } catch (MalformedURLException e) {
            logger.error("Invalid Selenium Grid URL: {}", getSeleniumGridUrl(), e);
            throw new RuntimeException("Invalid Selenium Grid URL", e);
        } catch (Exception error) {
            ErrorHandler.logError(error, "createRemoteDriver", "Initialization failed");
            throw new RuntimeException("Remote browser initialization failed", error);
        }
    }

    private static String getSeleniumGridUrl() {
        try {
            return PropertyFileConfigManager.getConfiguration(
                            PropertyConfigConstants.Environment.GLOBAL.getDisplayName(),
                            PropertyConfigConstants.PropertiesFilePath.GLOBAL.getFullPath())
                    .getProperty(SELENIUM_GRID_URL);
        } catch (Exception error) {
            ErrorHandler.logError(error, "getSeleniumGridUrl", "Failed to retrieve Selenium Grid URL");
            throw error;
        }
    }
}
