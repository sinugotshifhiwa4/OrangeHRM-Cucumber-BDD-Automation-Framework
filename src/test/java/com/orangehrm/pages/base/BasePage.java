package com.orangehrm.pages.base;

import com.orangehrm.config.properties.PropertyConfigConstants;
import com.orangehrm.config.properties.PropertyFileConfigManager;
import com.orangehrm.drivers.DriverFactory;
import com.orangehrm.drivers.dynamicWaits.FluentWaitUtils;
import com.orangehrm.utils.Base64Utils;
import com.orangehrm.utils.ErrorHandler;
import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.orangehrm.drivers.dynamicWaits.ExplicitWaitUtils.getWebDriverWait;
import static com.orangehrm.pages.base.DropdownHandler.*;


public class BasePage {

    private static final Logger logger = LoggerUtils.getLogger(BasePage.class);
    private static final String SCREENSHOT_DIRECTORY = "SCREENSHOT_DIR";
    private final DriverFactory driverFactory = DriverFactory.getInstance();


    public static boolean isElementVisible(WebElement element, String elementName) {
        try {
            getWebDriverWait().until(ExpectedConditions.visibilityOf(element));
            logger.info("Element '{}' is visible", elementName);
            return true;
        } catch (Exception error) {
            logger.error("Element not visible: {}", element, error);
            throw error;
        }
    }

    public static boolean isElementNotVisible(By element, String elementName) {
        try {
            getWebDriverWait().until(ExpectedConditions.invisibilityOfElementLocated(element));
            logger.info("Element '{}' is not visible", elementName);
            return true;
        } catch (Exception error) {
            logger.error("Element is still visible: {}", element, error);
            throw error;
        }
    }

    public static void waitForElementToBeVisible(WebElement element, String elementName) {
        try {
            getWebDriverWait().until(ExpectedConditions.visibilityOf(element));
            logger.info("Element '{}' is visible", elementName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "isElementVisible", "Element is not visible within timeout.");
            throw error;
        }
    }

    public static void waitForElementNotVisible(By element, String elementName) {
        try {
            getWebDriverWait().until(ExpectedConditions.invisibilityOfElementLocated(element));
            logger.info("Element '{}' is not visible as expected", elementName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "isElementNotVisible", "Element is still visible within timeout.");
            throw error;
        }
    }

    public static void assertElementRenderTime(WebElement element, String elementName) {
        long startTime = System.currentTimeMillis();

        try {
            getWebDriverWait().until(ExpectedConditions.visibilityOf(element));
            long endTime = System.currentTimeMillis();
            long loadTime = endTime - startTime;

            logger.info("Element '{}' rendered in {} ms", elementName, loadTime);

            if (loadTime > 3000) {
                logger.error("Element '{}' rendered in {} ms", elementName, loadTime);
                throw new TimeoutException("Element '" + elementName + "' took too long to render: " + loadTime + " ms");
            }
        } catch (Exception error) {
            logger.error("Error while checking image render time for '{}'", elementName, error);
            throw error;
        }
    }


    public void sendKeys(WebElement element, String value, String elementName) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            element.sendKeys(value);

            // Convert elementName to lowercase for case-insensitive comparison
            String lowercaseElementName = elementName.toLowerCase();

            // Mask the value if it's a sensitive field
            String logValue = lowercaseElementName.contains("username") ||
                    lowercaseElementName.contains("password") ||
                    lowercaseElementName.contains("credit card number") || // This can be customized based on the framework and sensitive information requirements.
                    lowercaseElementName.contains("address") ||
                    lowercaseElementName.contains("cvv")
                    ? "******" : value;

            logger.info("Element '{}' has been sent with value '{}'", elementName, logValue);
        } catch (Exception error) {
            ErrorHandler.logError(error, "sendKeys", "Failed to send keys");
            throw error;
        }
    }

    public void sendKeys(WebElement element, int value, String elementName) {
        sendKeys(element, String.valueOf(value), elementName);
    }

    public void clickElement(WebElement element, String elementName) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            FluentWaitUtils.waitForElementToBeClickable(element);
            element.click();
            logger.info("Element '{}' has been clicked", elementName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "clickElement", "Failed to click element");
            throw error;
        }
    }

    public void clearElement(WebElement element, String elementName) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            element.clear();
            logger.info("Element '{}' has been cleared", elementName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "clearElement", "Failed to clear element");
            throw error;
        }
    }

    public void selectDropdownElement(WebElement element, String selectMethod, Object value, String elementName) {
        try {
            // Wait for the element to be visible and enabled
            FluentWaitUtils.waitForElementToBeVisible(element);
            FluentWaitUtils.waitForElementToBeEnabled(element);

            Select select = new Select(element);
            List<WebElement> options = select.getOptions();

            if (options.isEmpty()) {
                throw new IllegalStateException("Dropdown '" + elementName + "' has no options available");
            }

            String method = selectMethod.trim().toLowerCase();

            switch (method) {
                case "visibletext" -> selectByVisibleText(select, options, value, elementName);
                case "value" -> selectByValue(select, options, value, elementName);
                case "index" -> selectByIndex(select, options, value, elementName);
                default -> throw new IllegalArgumentException("Invalid select method: " + selectMethod);
            }
        } catch (Exception error) {
            ErrorHandler.logError(error, "selectDropdownElement",
                    "Failed to select dropdown element '" + elementName + "' using method: " + selectMethod + " with value: " + value);
            throw error;
        }
    }

    public void verifyNoDropdownOptionSelected(WebElement dropdown, List<String> predefinedOptions, String elementName) {
        Select select = new Select(dropdown);
        WebElement selectedOption = select.getFirstSelectedOption();

        if (selectedOption != null && predefinedOptions.contains(selectedOption.getText().trim())) {
            logger.error("A dropdown has a selected option: {}", selectedOption.getText());
            throw new AssertionError("A dropdown has a selected option: " + selectedOption.getText());
        } else {
            logger.info("Dropdown has no selected options from the predefined list.");
        }
    }


    private String validateString(Object value, String method) {
        if (!(value instanceof String strValue)) {
            throw new IllegalArgumentException("Expected a String for '" + method + "' method.");
        }
        return strValue;
    }

    private int validateInteger(Object value) {
        if (!(value instanceof Integer intValue)) {
            throw new IllegalArgumentException("Expected an Integer for 'index' method.");
        }
        return intValue;
    }

    public boolean isElementEnabled(WebElement element) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            return element.isEnabled() && !Objects.requireNonNull(element.getDomProperty("class")).contains("disabled");
        } catch (Exception e) {
            logger.warn("Element not enabled: {}", element, e);
            return false;
        }
    }

    public boolean isElementSelected(WebElement element) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            return element.isSelected();
        } catch (Exception e) {
            logger.warn("Failed to check if element is selected: {}", element, e);
            return false;
        }
    }

    public String getElementText(WebElement element) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            return element.getText().trim(); // Trim whitespace for cleaner output
        } catch (Exception error) {
            ErrorHandler.logError(error, "getElementText", "Failed to get element text");
            throw error;
        }
    }

    public boolean doesElementContainText(WebElement element, String expectedText, String elementName) {
        try {
            String actualText = getElementText(element);
            boolean containsText = actualText.contains(expectedText);

            if (!containsText) {
                logger.error("Element text does not contain expected text: '{}', but found: '{}'", expectedText, actualText);
                throw new AssertionError("Element text does not contain expected value. Expected: '"
                        + expectedText + "', but found: '" + actualText + "'");
            }
            logger.info("Element '{}' contains the correct text: '{}'", elementName, actualText);
            return true;
        } catch (Exception error) {
            ErrorHandler.logError(error, "doesElementContainText", "Failed to verify element text");
            throw error;
        }
    }


    public String getNormalizedText(WebElement element) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            String text = element.getText();
            return text.replaceAll("\\s+", " ").trim(); // Normalize spaces
        } catch (Exception error) {
            ErrorHandler.logError(error, "getNormalizedText", "Failed to get normalized element text");
            throw error;
        }
    }


    public String getInputText(WebElement element) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            return Objects.requireNonNull(element.getDomProperty("value")).trim();
        } catch (Exception error) {
            ErrorHandler.logError(error, "getInputText", "Failed to get text from input field");
            throw error;
        }
    }

    public boolean isInputFieldNotEmpty(WebElement element, String elementName) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);

            String fieldValue = element.getDomProperty("value");
            if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                logger.info("Input field '{}' is not empty", elementName);
                return true;
            } else {
                logger.warn("Input field '{}' is empty!", elementName);
                throw new AssertionError("Input field " + elementName + " is empty!");
            }
        } catch (Exception error) {
            ErrorHandler.logError(error, "isInputFieldNotEmpty", "Failed to validate input field: " + elementName);
            throw error;
        }
    }


    public List<String> getTextsFromElements(List<WebElement> elements) {
        try {
            return elements.stream()
                    .map(WebElement::getText)
                    .map(String::trim)
                    .filter(text -> !text.isEmpty()) // Exclude empty strings
                    .collect(Collectors.toList());
        } catch (Exception error) {
            ErrorHandler.logError(error, "getTextsFromElements", "Failed to get texts from elements list");
            throw error;
        }
    }

    public String getHiddenElementText(WebElement element) {
        try {
            return Objects.requireNonNull(element.getDomProperty("textContent")).trim();
        } catch (Exception error) {
            ErrorHandler.logError(error, "getHiddenElementText", "Failed to get hidden element text");
            throw error;
        }
    }

    public String getPlaceholderText(WebElement element) {
        try {
            return Objects.requireNonNull(element.getDomProperty("placeholder")).trim();
        } catch (Exception error) {
            ErrorHandler.logError(error, "getPlaceholderText", "Failed to get placeholder text");
            throw error;
        }
    }


    public void waitForModalToBeVisible(WebElement modalElement) {
        try {
            getWebDriverWait().until(ExpectedConditions.visibilityOf(modalElement));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForModalToBeVisible", "Modal is not visible within timeout.");
            throw error;
        }
    }

    public void waitForModalToBeHidden(WebElement modalElement) {
        try {
            getWebDriverWait().until(ExpectedConditions.invisibilityOf(modalElement));
        } catch (Exception error) {
            ErrorHandler.logError(error, "waitForModalToBeHidden", "Modal is still visible within timeout.");
            throw error;
        }
    }

    public boolean isModalVisible(WebElement modalElement) {
        try {
            return getWebDriverWait().until(ExpectedConditions.visibilityOf(modalElement)) != null;
        } catch (Exception error) {
            logger.warn("Modal not visible: {}", modalElement, error);
            return false;
        }
    }

    public void closeModal(WebElement closeButton) {
        try {
            FluentWaitUtils.waitForElementToBeClickable(closeButton);
            closeButton.click();
        } catch (Exception error) {
            ErrorHandler.logError(error, "closeModal", "Failed to close modal.");
            throw error;
        }
    }

    public void switchToIframe(WebElement iframeElement) {
        try {
            getWebDriverWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeElement));
        } catch (Exception error) {
            ErrorHandler.logError(error, "switchToIframe", "Failed to switch to iframe.");
            throw error;
        }
    }

    public void switchToWindow(String windowHandle) {
        try {
            if (windowHandle == null || windowHandle.isEmpty()) {
                ErrorHandler.logError(new IllegalArgumentException("Invalid window handle provided"),
                        "switchToWindow", "Invalid window handle: " + windowHandle);
                return;
            }

            Set<String> windowHandles = driverFactory.getDriver().getWindowHandles();
            if (!windowHandles.contains(windowHandle)) {
                ErrorHandler.logError(new NoSuchWindowException("Window handle not found: " + windowHandle),
                        "switchToWindow", "Window handle not found: " + windowHandle);
                return;
            }

            driverFactory.getDriver().switchTo().window(windowHandle);
            logger.info("Switched to window successfully: {}", windowHandle);

        } catch (NoSuchWindowException error) {
            ErrorHandler.logError(error, "switchToWindow", "No such window exists: " + windowHandle);
            throw error;
        } catch (Exception error) {
            ErrorHandler.logError(error, "switchToWindow", "Failed to switch to window: " + windowHandle);
            throw error;
        }
    }

    public void switchToDefaultContent() {
        try {
            DriverFactory.getInstance().getDriver().switchTo().defaultContent();
        } catch (Exception error) {
            ErrorHandler.logError(error, "switchToDefaultContent", "Failed to switch back to main content.");
            throw error;
        }
    }

    public String captureScreenshot(String screenshotName) {
        try {
            // Generate timestamp and formatted filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s.png", screenshotName, timestamp);

            // Define structured screenshot directory
            String screenshotDir = PropertyFileConfigManager.getConfiguration(
                    PropertyConfigConstants.Environment.GLOBAL.getDisplayName(),
                    PropertyConfigConstants.PropertiesFilePath.GLOBAL.getFullPath())
                    .getProperty(SCREENSHOT_DIRECTORY);


            Path destinationPath = Path.of(screenshotDir, fileName);

            // Take the screenshot
            File screenshot = ((TakesScreenshot) driverFactory.getDriver()).getScreenshotAs(OutputType.FILE);

            return saveScreenshot(destinationPath, screenshot);

        } catch (IOException error) {
            ErrorHandler.logError(error, "captureScreenshot", "Failed to capture screenshot: " + screenshotName);
            throw new RuntimeException("Error capturing screenshot: " + screenshotName, error);
        }
    }

    private String saveScreenshot(Path destinationPath, File screenshot) throws IOException {
        try {
            // Ensure the directory exists before saving the screenshot
            Files.createDirectories(destinationPath.getParent());
            Files.copy(screenshot.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Screenshot successfully saved at: {}", destinationPath);

            // Convert to Base64 for reporting and return
            return Base64Utils.encodeArray(Files.readAllBytes(destinationPath));
        } catch (IOException ioError) {
            ErrorHandler.logError(ioError, "saveScreenshot",
                    "I/O error occurred while saving screenshot at: " + destinationPath);
            throw new IOException("Failed to save screenshot at: " + destinationPath, ioError);
        } catch (Exception error) {
            ErrorHandler.logError(error, "saveScreenshot",
                    "Unexpected error occurred while processing screenshot: " + screenshot.getName());
            throw new IOException("Unexpected error while processing screenshot: " + screenshot.getName(), error);
        }
    }

    public void uploadFile(WebElement uploadElement, String filePath) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(uploadElement);
            uploadElement.sendKeys(filePath);
        } catch (Exception error) {
            ErrorHandler.logError(error, "uploadFile", "Failed to upload file");
            throw error;
        }
    }

    public void acceptAlert() {
        try {
            getWebDriverWait().until(ExpectedConditions.alertIsPresent()).accept();
        } catch (Exception error) {
            ErrorHandler.logError(error, "acceptAlert", "Failed to accept alert");
            throw error;
        }
    }

    public void dismissAlert() {
        try {
            getWebDriverWait().until(ExpectedConditions.alertIsPresent()).dismiss();
        } catch (Exception error) {
            ErrorHandler.logError(error, "dismissAlert", "Failed to dismiss alert");
            throw error;
        }
    }

    public String getAlertText() {
        try {
            return getWebDriverWait().until(ExpectedConditions.alertIsPresent()).getText();
        } catch (Exception error) {
            ErrorHandler.logError(error, "getAlertText", "Failed to retrieve alert text");
            throw error;
        }
    }

    public void sendKeysToAlert(String text) {
        try {
            getWebDriverWait().until(ExpectedConditions.alertIsPresent()).sendKeys(text);
        } catch (Exception error) {
            ErrorHandler.logError(error, "sendKeysToAlert", "Failed to send keys to alert");
            throw error;
        }
    }

    public void navigateBack() {
        try {
            driverFactory.getDriver().navigate().back();
        } catch (Exception error) {
            ErrorHandler.logError(error, "navigateBack", "Failed to navigate back");
            throw error;
        }
    }

    public void navigateForward() {
        try {
            driverFactory.getDriver().navigate().forward();
        } catch (Exception error) {
            ErrorHandler.logError(error, "navigateForward", "Failed to navigate forward");
            throw error;
        }
    }

    /**
     * Refreshes the current browser page and handles any exceptions that might occur.
     *
     * @param driver WebDriver instance to perform the refresh operation
     * @throws RuntimeException If page refresh operation fails
     */
    public void refreshBrowserPage(WebDriver driver) {
        try {
            driver.navigate().refresh();
        } catch (Exception error) {
            ErrorHandler.logError(error, "refreshBrowserPage", "Failed to refresh the current browser page");
            throw new RuntimeException("Browser page refresh operation failed", error);
        }
    }

    public void scrollPageUp() {
        try {
            getJsExecutor().executeScript("window.scrollTo(0, 0)");
        } catch (Exception error) {
            ErrorHandler.logError(error, "scrollPageUp", "Failed to scroll page up");
            throw error;
        }
    }

    public void scrollPageDown() {
        try {
            getJsExecutor().executeScript("window.scrollTo(0, document.body.scrollHeight)");
        } catch (Exception error) {
            ErrorHandler.logError(error, "scrollPageDown", "Failed to scroll page down");
            throw error;
        }
    }

    public void scrollToElement(WebElement element) {
        try {
            getJsExecutor().executeScript("arguments[0].scrollIntoView(true);", element);
        } catch (Exception error) {
            ErrorHandler.logError(error, "scrollToElement", "Failed to scroll to element");
            throw error;
        }
    }

    public void hoverOverElement(WebElement element) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            getActions().moveToElement(element).perform();
        } catch (Exception error) {
            ErrorHandler.logError(error, "hoverOverElement", "Failed to hover over element");
            throw error;
        }
    }

    public void dragAndDrop(WebElement source, WebElement target) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(source);
            FluentWaitUtils.waitForElementToBeVisible(target);
            getActions().dragAndDrop(source, target).perform();
        } catch (Exception error) {
            ErrorHandler.logError(error, "dragAndDrop", "Failed to drag and drop file");
            throw error;
        }
    }

    private Actions getActions() {
        try {
            WebDriver driver = driverFactory.getDriver();
            return new Actions(driver);
        } catch (Exception error) {
            ErrorHandler.logError(error, "getActions", "Failed to get actions");
            throw error;
        }
    }

    private JavascriptExecutor getJsExecutor() {
        try {
            WebDriver driver = driverFactory.getDriver();
            return (JavascriptExecutor) driver;
        } catch (Exception error) {
            ErrorHandler.logError(error, "getJsExecutor", "Failed to get javascript executor");
            throw error;
        }
    }

    public static void logAndThrowIfEmptyIfEmpty(String valueName) {
        logger.error("{} Value cannot be empty", valueName);
        throw new IllegalArgumentException(valueName + " Value cannot be empty");
    }

    public void pressUpArrowKey(WebElement element, String elementName) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            FluentWaitUtils.waitForElementToBeClickable(element);
            element.sendKeys(Keys.ARROW_UP);
            logger.info("Element '{}' has been clicked", elementName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "pressUpArrowKey", "Failed to click element");
            throw error;
        }
    }

    public void pressDownArrowKey(WebElement element, String elementName) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            FluentWaitUtils.waitForElementToBeClickable(element);
            element.sendKeys(Keys.ARROW_DOWN);
            logger.info("Element '{}' has been clicked", elementName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "pressDownArrowKey", "Failed to click element");
            throw error;
        }
    }

    public void pressEnterKey(WebElement element, String elementName) {
        try {
            FluentWaitUtils.waitForElementToBeVisible(element);
            FluentWaitUtils.waitForElementToBeClickable(element);
            element.sendKeys(Keys.ENTER);
            logger.info("Element '{}' has been clicked", elementName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "pressEnterKey", "Failed to click element");
            throw error;
        }
    }
}
