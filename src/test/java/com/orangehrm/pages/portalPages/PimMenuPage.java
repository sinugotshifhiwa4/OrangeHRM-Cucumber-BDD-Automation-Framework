package com.orangehrm.pages.portalPages;

import com.orangehrm.hooks.TestBase;
import com.orangehrm.utils.ErrorHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class PimMenuPage extends TestBase {

    private final WebDriver driver;

    By pimMenu = By.xpath("//div//a[contains(@href, 'pim') and contains(normalize-space(), 'PIM')]");
    By addButton = By.xpath("//button[@type='button' and contains(normalize-space(), 'Add')]");
    By firstNameInput = By.cssSelector("input[name='firstName']");
    By middleNameInput = By.cssSelector("input[name='middleName']");
    By lastNameInput = By.cssSelector("input[name='lastName']");
    By generatedEmployeeIdInput = By.xpath("(//input[@class='oxd-input oxd-input--active'])[2]");
    By activateCreateLoginDetailsCheckbox = By.xpath("//input[@type='checkbox']/following-sibling::span[contains(@class, 'oxd-switch-input')]");
    By UsernameInput = By.xpath("(//input[@class='oxd-input oxd-input--active'])[3]");
    By passwordInput = By.xpath("(//input[@type='password'])[1]");
    By confirmPasswordInput = By.xpath("(//input[@type='password'])[2]");
    By statusEnabled = By.xpath("//label[input[@type='radio' and @value='1']]");
    By statusDisabled = By.xpath("//label[input[@type='radio' and @value='2']]");
    By saveButton = By.xpath("//button[@type='submit']");
    By cancelButton = By.xpath("(//button[@type='button'])[5]");


    public PimMenuPage(WebDriver driver) {
        this.driver = driver;
    }

    public void verifyPimMenuIsVisible() {
        try{
            waitForElementToBeVisible(driver.findElement(pimMenu), "PIM Menu");
        } catch (Exception error){
            ErrorHandler.logError(error, "verifyPimMenuIsVisible", "Failed to validate presence of PIM menu");
            throw error;
        }
    }

    public void clickPimMenu() {
        try{
            clickElement(driver.findElement(pimMenu), "PIM Menu");
        } catch (Exception error){
            ErrorHandler.logError(error, "clickPimMenu", "Failed to click PIM menu");
            throw error;
        }
    }

    public void clickAddButton() {
        try{
            clickElement(driver.findElement(addButton), "Add Button");
        } catch (Exception error){
            ErrorHandler.logError(error, "clickAddButton", "Failed to click add button");
            throw error;
        }
    }

    public void fillFirstName(String firstName) {
        try{
            sendKeys(driver.findElement(firstNameInput), firstName, "First Name");
        } catch (Exception error){
            ErrorHandler.logError(error, "fillFirstName", "Failed to fill first name input");
            throw error;
        }
    }

    public void fillMiddleName(String middleName) {
        try{
            sendKeys(driver.findElement(middleNameInput), middleName, "Middle Name");
        } catch (Exception error){
            ErrorHandler.logError(error, "fillMiddleName", "Failed to fill middle name input");
            throw error;
        }
    }

    public void fillLastName(String lastName) {
        try{
            sendKeys(driver.findElement(lastNameInput), lastName, "Last Name");
        } catch (Exception error){
            ErrorHandler.logError(error, "fillLastName", "Failed to fill last name input");
            throw error;
        }
    }

    public String getGeneratedEmployeeId() {
        try {
            return getInputText(driver.findElement(generatedEmployeeIdInput));
        } catch (Exception error) {
            ErrorHandler.logError(error, "getGeneratedEmployeeId", "Failed to get generated employee ID");
            throw error;
        }
    }

    public void clickSaveButton() {
        try{
            clickElement(driver.findElement(saveButton), "Save Button");
            Thread.sleep(10000);
        } catch (Exception error){
            ErrorHandler.logError(error, "clickSaveButton", "Failed to click save button");
            throw new RuntimeException("Failed to click save button", error);
        }
    }

    public void clickCancelButton() {
        try{
            clickElement(driver.findElement(cancelButton), "Cancel Button");
        } catch (Exception error){
            ErrorHandler.logError(error, "clickCancelButton", "Failed to click cancel button");
            throw error;
        }
    }

    public void fillEmployeeDetails(String firstName, String middleName, String lastName) {
        fillFirstName(firstName);
        fillMiddleName(middleName);
        fillLastName(lastName);
    }


    public void clickToActivateCreateLoginDetailsCheckbox() {
        try{
            clickElement(driver.findElement(activateCreateLoginDetailsCheckbox), "Activate Create Login Details Checkbox");
        } catch (Exception error){
            ErrorHandler.logError(error, "clickToActivateCreateLoginDetailsCheckbox", "Failed to click to activate create login details checkbox");
            throw error;
        }
    }

    public void fillUsername(String username) {
        try{
            sendKeys(driver.findElement(UsernameInput), username, "Username");
        } catch (Exception error){
            ErrorHandler.logError(error, "fillUsername", "Failed to fill username input");
            throw error;
        }
    }

    public void fillPassword(String password) {
        try{
            sendKeys(driver.findElement(passwordInput), password, "Password");
        } catch (Exception error){
            ErrorHandler.logError(error, "fillPassword", "Failed to fill password input");
            throw error;
        }
    }

    public void fillConfirmPassword(String confirmPassword) {
        try{
            sendKeys(driver.findElement(confirmPasswordInput), confirmPassword, "Confirm Password");
        } catch (Exception error){
            ErrorHandler.logError(error, "fillConfirmPassword", "Failed to fill confirm password input");
            throw error;
        }
    }

    public void clickStatusEnabled() {
        try{
            clickElement(driver.findElement(statusEnabled), "Status Enabled");
        } catch (Exception error){
            ErrorHandler.logError(error, "clickStatusEnabled", "Failed to click status enabled");
            throw error;
        }
    }

    public void clickStatusDisabled() {
        try{
            clickElement(driver.findElement(statusDisabled), "Status Disabled");
        } catch (Exception error){
            ErrorHandler.logError(error, "clickStatusDisabled", "Failed to click status disabled");
            throw error;
        }
    }

    public void createLoginDetails(String username, String password) {
        try{
            clickToActivateCreateLoginDetailsCheckbox();
            fillUsername(username);
            fillPassword(password);
            fillConfirmPassword(password);
            clickStatusEnabled();
        } catch (Exception error){
            ErrorHandler.logError(error, "createLoginDetails", "Failed to create login details");
            throw error;
        }
    }

    public void addEmployee(String firstName, String middleName, String lastName, String username, String password) {
        clickPimMenu();
        clickAddButton();
        fillEmployeeDetails(firstName, middleName, lastName);
        createLoginDetails(username, password);
        clickSaveButton();

    }
}
