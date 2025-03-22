package com.orangehrm.drivers;

import com.orangehrm.drivers.dynamicWaits.ImplicitWaitUtils;
import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

public class DriverFactory {

    private static final Logger logger = LoggerUtils.getLogger(DriverFactory.class);
    private static final DriverFactory instance = new DriverFactory();

    private static final ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<>();

    private DriverFactory() {}

    public static DriverFactory getInstance() {
        return instance;
    }

    public WebDriver getDriver() {
        if (threadLocalDriver.get() == null) {
            logger.error("ThreadLocal driver not initialized for thread '{}'", Thread.currentThread().getName());
            throw new IllegalStateException("WebDriver is not initialized for thread: "
                    + Thread.currentThread().threadId());
        }
        return threadLocalDriver.get();
    }


    public void setDriver(WebDriver driver) {
        try {
            threadLocalDriver.set(driver);
            configureDriver();
        } catch (Exception error) {
            ErrorHandler.logError(error, "setDriver", "Failed to set driver");
            throw error;
        }
    }

    public void removeDriver() {
        threadLocalDriver.remove();
    }

    public void quitDriver() {
        try {
            WebDriver driver = threadLocalDriver.get();
            if (driver != null) {
                driver.quit();  // Terminate the driver instance
                threadLocalDriver.remove();  // Clear the ThreadLocal
                logger.info("Driver quit and removed successfully for thread: {}", Thread.currentThread().threadId());
            }
        } catch (Exception error) {
            ErrorHandler.logError(error, "quitDriver", "Failed to quit driver");
            throw error;
        }
    }



    public void navigateToUrl(String url) {
        try{
            getDriver().get(url);
            logger.info("Navigated to URL: {}", url);
        } catch (Exception error){
            ErrorHandler.logError(error, "navigateToUrl", "Failed to navigate to url");
            throw error;
        }
    }

    public void navigateToUrlWithHistory(String url) {
        try {
            getDriver().navigate().to(url);
        } catch (Exception error) {
            ErrorHandler.logError(error, "navigateToUrlWithHistory", "Failed to navigate to url");
            throw error;
        }
    }

    public void configureDriver() {
        try {
            getDriver().manage().window().maximize();
            ImplicitWaitUtils.applyImplicitWait(getDriver());
        } catch (Exception error){
            ErrorHandler.logError(error, "configureDriver", "Failed to configure driver");
            throw error;
        }
    }

    public boolean hasDriver() {
        try {
            return threadLocalDriver.get() != null;
        } catch (Exception error){
            ErrorHandler.logError(error, "hasDriver", "Failed to check driver");
            throw error;
        }
    }
}
