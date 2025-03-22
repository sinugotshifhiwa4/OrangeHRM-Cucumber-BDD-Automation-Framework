package com.orangehrm.drivers.dynamicWaits;

import com.orangehrm.config.properties.PropertyConfigConstants;
import com.orangehrm.config.properties.PropertyFileConfigManager;
import com.orangehrm.drivers.DriverFactory;
import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.NoSuchElementException;

public class FluentWaitUtils {

    private static final Logger logger = LoggerUtils.getLogger(FluentWaitUtils.class);
    private static final DriverFactory driverFactory = DriverFactory.getInstance();
    private static final String TIMEOUT_KEY = "DEFAULT_GLOBAL_TIMEOUT";
    private static final String POLLING_KEY = "POLLING_TIMEOUT";

    private FluentWaitUtils() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    private static Wait<WebDriver> fluentWaitInstance;

    private static Wait<WebDriver> getFluentWait() {
        if (fluentWaitInstance == null) {

            fluentWaitInstance = new FluentWait<>(driverFactory.getDriver())
                    .withTimeout(Duration.ofSeconds(getDefaultTimeout()))
                    .pollingEvery(Duration.ofMillis(getPollingTimeout()))
                    .ignoring(WebDriverException.class);
        }
        return fluentWaitInstance;
    }

    public static void waitForElementToBeVisible(WebElement element) {
        try {
            getFluentWait().until(ExpectedConditions.visibilityOf(element));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForElementToBeVisible", "Failed to wait for element to be visible");
            throw error;
        }
    }

    public static void waitForElementToBeClickable(WebElement element) {
        try {
            getFluentWait().until(ExpectedConditions.elementToBeClickable(element));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForElementToBeClickable", "Failed to wait for element to be clickable");
            throw error;
        }
    }

    public static void waitForElementNotToBeVisible(WebElement element) {
        try {
            getFluentWait().until(ExpectedConditions.invisibilityOf(element));
        } catch (NoSuchElementException error) {
            logger.info("Element is not visible as expected.");
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForElementNotToBeVisible", "Failed to wait for element to be not visible");
            throw error;
        }
    }

    public static void waitForPresenceOfElement(By locator) {
        try {
            getFluentWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForPresenceOfElement", "Failed to wait for element presence");
            throw error;
        }
    }

    public static void waitForTextToBePresent(WebElement element, String text) {
        try {
            getFluentWait().until(ExpectedConditions.textToBePresentInElement(element, text));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForTextToBePresent", "Failed to wait for text presence");
            throw error;
        }
    }

    public static void waitForAttributeToContain(WebElement element, String attribute, String value) {
        try {
            getFluentWait().until(ExpectedConditions.attributeContains(element, attribute, value));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForAttributeToContain", "Failed to wait for attribute change");
            throw error;
        }
    }

    public static void waitForPageTitle(String expectedTitle) {
        try {
            getFluentWait().until(ExpectedConditions.titleIs(expectedTitle));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForPageTitle", "Failed to wait for page title");
            throw error;
        }
    }

    public static void waitForElementToBeEnabled(WebElement element) {
        try {
            getFluentWait().until(driver -> element.isEnabled());
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForElementToBeEnabled", "Failed to wait for element to be enabled");
            throw error;
        }
    }

    public static void waitForElementToBeDisabled(WebElement element) {
        try {
            getFluentWait().until(driver -> !element.isEnabled());
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForElementToBeDisabled", "Failed to wait for element to be disabled");
            throw error;
        }
    }

    private static int getDefaultTimeout() {
        try {
            return PropertyFileConfigManager.getConfiguration(
                            PropertyConfigConstants.Environment.GLOBAL.getDisplayName(),
                            PropertyConfigConstants.PropertiesFilePath.GLOBAL.getFullPath())
                    .getProperty(TIMEOUT_KEY, Integer.class)
                    .orElse(60);

        } catch (Exception error) {
            ErrorHandler.logError(error, "getTimeout", "Failed to retrieve timeout value");
            throw error;
        }
    }

    private static int getPollingTimeout() {
        try {
            return PropertyFileConfigManager.getConfiguration(
                            PropertyConfigConstants.Environment.GLOBAL.getDisplayName(),
                            PropertyConfigConstants.PropertiesFilePath.GLOBAL.getFullPath())
                    .getProperty(POLLING_KEY, Integer.class)
                    .orElse(1000);
        } catch (Exception error) {
            ErrorHandler.logError(error, "getPollingTimeout", "Failed to retrieve polling timeout value");
            throw error;
        }
    }
}