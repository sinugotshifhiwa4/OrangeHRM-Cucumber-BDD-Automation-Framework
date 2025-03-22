package com.orangehrm.pages.portalPages;


import com.orangehrm.hooks.TestBase;
import com.orangehrm.utils.ErrorHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends TestBase {

    private final  WebDriver driver;

    By usernameInput = By.cssSelector("input[name='username']");
    By passwordInput = By.cssSelector("input[name='password']");
    By loginButton = By.xpath("//button[@type='submit' and contains(normalize-space(), 'Login')]");
    By errorMessage = By.xpath("//div[contains(@role, 'alert')]//p[contains(normalize-space(), 'Invalid credentials')]");
    By companyLogo = By.cssSelector("img[alt='company-branding']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void fillUsername(String username) {
        try{
            sendKeys(driver.findElement(usernameInput), username, "Username");
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

    public void clickLoginButton() {
        try{
            clickElement(driver.findElement(loginButton), "Login Button");
        } catch (Exception error){
            ErrorHandler.logError(error, "clickLoginButton", "Failed to click login button");
            throw error;
        }
    }

    public void isCompanyLogoPresent(){
        try{
            isElementVisible(driver.findElement(companyLogo), "Company Logo");
        } catch (Exception error){
            ErrorHandler.logError(error, "isCompanyLogoPresent", "Failed to validate presence of company logo");
            throw error;
        }
    }

    public void clickCompanyLogo(){
        try{
            clickElement(driver.findElement(companyLogo), "Company Logo");
        } catch (Exception error){
            ErrorHandler.logError(error, "clickCompanyLogo", "Failed to click company logo");
            throw error;
        }
    }

    public void isLoginErrorMessageVisible(){
        try{
            isElementVisible(driver.findElement(errorMessage), "Error Message");
        } catch (Exception error){
            ErrorHandler.logError(error, "isLoginErrorMessageVisible", "Failed to validate presence of error message");
            throw error;
        }
    }

    public void verifyLoginErrorMessageNotVisible(){
        try{
            waitForElementNotVisible(errorMessage, "Error Message");
        } catch (Exception error){
            ErrorHandler.logError(error, "verifyLoginErrorMessageNotVisible", "Failed to validate absence of error message");
            throw error;
        }
    }

    public void loginToPortal(String username, String password) {
        try{
            fillUsername(username);
            fillPassword(password);
            clickLoginButton();
        } catch (Exception error){
            ErrorHandler.logError(error, "loginToPortal", "Failed to login to portal");
            throw error;
        }
    }
}
