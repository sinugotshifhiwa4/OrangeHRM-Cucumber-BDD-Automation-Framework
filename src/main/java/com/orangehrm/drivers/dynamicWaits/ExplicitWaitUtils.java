package com.orangehrm.drivers.dynamicWaits;

import com.orangehrm.config.properties.PropertyConfigConstants;
import com.orangehrm.config.properties.PropertyFileConfigManager;
import com.orangehrm.drivers.DriverFactory;
import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

public class ExplicitWaitUtils {

    private static final Logger logger = LoggerUtils.getLogger(ExplicitWaitUtils.class);
    private static final DriverFactory driverFactory = DriverFactory.getInstance();
    private static final String TIMEOUT_KEY = "DEFAULT_GLOBAL_TIMEOUT";

    private ExplicitWaitUtils() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    public static WebDriverWait getWebDriverWait() {
        try {
            return new WebDriverWait(driverFactory.getDriver(), Duration.ofSeconds(getDefaultTimeout()));
        } catch (Exception error) {
            ErrorHandler.logError(error, "getWebDriverWait", "Failed to get WebDriverWait");
            throw error;
        }
    }

    public static void waitForElementToBeVisible(WebElement element) {
        try {
            getWebDriverWait().until(ExpectedConditions.visibilityOf(element));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForElementToBeVisible", "Failed to wait for element to be visible");
            throw error;
        }
    }

    public static void waitForElementToBeClickable(WebElement element) {
        try {
            getWebDriverWait().until(ExpectedConditions.elementToBeClickable(element));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForElementToBeClickable", "Failed to wait for element to be clickable");
            throw error;
        }
    }

    public static void waitForElementNotToBeVisible(WebElement element) {
        try {
            getWebDriverWait().until(ExpectedConditions.invisibilityOf(element));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForElementNotToBeVisible", "Failed to wait for element to be not visible");
            throw error;
        }
    }

    public static void waitForPresenceOfElement(By locator) {
        try {
            getWebDriverWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForPresenceOfElement", "Failed to wait for element presence");
            throw error;
        }
    }

    public static void waitForTextToBePresent(WebElement element, String text) {
        try {
            getWebDriverWait().until(ExpectedConditions.textToBePresentInElement(element, text));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForTextToBePresent", "Failed to wait for text presence");
            throw error;
        }
    }

    public static void waitForAttributeToContain(WebElement element, String attribute, String value) {
        try {
            getWebDriverWait().until(ExpectedConditions.attributeContains(element, attribute, value));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForAttributeToContain", "Failed to wait for attribute change");
            throw error;
        }
    }

    public static void waitForPageTitle(String expectedTitle) {
        try {
            getWebDriverWait().until(ExpectedConditions.titleIs(expectedTitle));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForPageTitle", "Failed to wait for page title");
            throw error;
        }
    }

    /**
     * Waits for the page to completely load after navigation or refresh.
     */
    public static void waitForPageLoadComplete() {
        try {
            // Execute JavaScript to check if document is ready
            getWebDriverWait().until(
                    webDriver -> Objects.equals(((JavascriptExecutor) webDriver).executeScript("return document.readyState"), "complete")
            );
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForPageLoadComplete", "Failed to wait for page to fully load");
            throw new RuntimeException("Page load wait operation failed", error);
        }
    }

    /**
     * Waits for the page to completely load after navigation or refresh.
     *
     * @param driver WebDriver instance
     */
    public static void waitForPageLoadComplete(WebDriver driver) {
        try {
            // Execute JavaScript to check if document is ready
            getWebDriverWait().until(
                    webDriver -> Objects.equals(((JavascriptExecutor) webDriver).executeScript("return document.readyState"), "complete")
            );
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForPageLoadComplete", "Failed to wait for page to fully load");
            throw new RuntimeException("Page load wait operation failed", error);
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
}