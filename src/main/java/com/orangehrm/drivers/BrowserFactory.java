package com.orangehrm.drivers;

import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import static com.orangehrm.drivers.BrowserOptionsUtils.*;


public class BrowserFactory {
    private final Logger logger = LoggerUtils.getLogger(BrowserFactory.class);

    public synchronized void initializeBrowser(String browserName, String... arguments) {
        try {
            WebDriver driver = createDriver(browserName, arguments);
            DriverFactory driverFactory = DriverFactory.getInstance();

            if (driverFactory != null) {
                driverFactory.setDriver(driver);
            } else {
                logger.warn("DriverFactory instance is null. WebDriver will not be stored.");
            }

            logger.info("Initialized {} browser", browserName);
        } catch (IllegalArgumentException e) {
            logger.warn("Unsupported browser requested: {}", browserName);
            throw new RuntimeException("Unsupported browser: " + browserName, e);
        } catch (Exception error) {
            ErrorHandler.logError(error, "initializeBrowser", "Initialization failed");
            throw new RuntimeException("Browser initialization failed", error);
        }
    }

    private WebDriver createDriver(String browserName, String... arguments) {
        try {
            return switch (browserName.toLowerCase()) {
                case "chrome" -> new ChromeDriver(getChromeOptions(arguments));
                case "firefox" -> new FirefoxDriver(getFirefoxOptions(arguments));
                case "edge" -> new EdgeDriver(getEdgeOptions(arguments));
                default -> throw new IllegalArgumentException("Unsupported browser: " + browserName);
            };
        } catch (Exception error) {
            ErrorHandler.logError(error, "createDriver", "Initialization failed");
            throw new RuntimeException("Browser initialization failed", error);
        }
    }
}
