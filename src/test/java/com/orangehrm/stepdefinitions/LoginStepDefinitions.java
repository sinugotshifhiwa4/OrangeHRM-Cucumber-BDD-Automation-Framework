package com.orangehrm.stepdefinitions;

import com.orangehrm.hooks.TestBase;
import com.orangehrm.pages.base.BasePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class LoginStepDefinitions {

    private final TestBase testBase;

    public LoginStepDefinitions(TestBase testBase) {
        this.testBase = testBase;
    }

    @Given("the user is on the Orange HRM login page")
    public void theUserIsOnTheOrangeHRMLoginPage() {
        testBase.loginPage.isCompanyLogoPresent();
    }

    @When("the user enters valid username {string} and password {string}")
    public void theUserEntersValidUsernameAndPassword(String username, String password) {
        if (username.equals("<valid_username>") && password.equals("<valid_password>")) {
            testBase.loginPage.fillUsername(testBase.decryptCredentials().get(0));
            testBase.loginPage.fillPassword(testBase.decryptCredentials().get(1));
        } else {
            testBase.loginPage.fillUsername(username);
            testBase.loginPage.fillPassword(password);
        }
    }

    @And("the user clicks the login button")
    public void theUserClicksTheLoginButton() {
        testBase.loginPage.clickLoginButton();
    }

    @Then("the user should login successfully")
    public void theUserShouldLoginSuccessfully() {
        testBase.loginPage.verifyLoginErrorMessageNotVisible();
    }

    @And("the user should be redirected to the dashboard page")
    public void theUserShouldBeRedirectedToTheDashboardPage() {
        testBase.sideMenuPage.verifyDashboardMenuIsVisible();
    }

    @When("the user enters username {string} and password {string}")
    public void theUserEntersUsernameAndPassword(String username, String password) {
        if (username.equals("<invalid_username>") && password.equals("<invalid_password>")) {
            testBase.loginPage.fillUsername("GeneralUser");
            testBase.loginPage.fillPassword("Password@123");
        } else {
            testBase.loginPage.fillUsername(username);
            testBase.loginPage.fillPassword(password);
        }
    }

    @Then("the login should fail and an appropriate error message should be displayed")
    public void theLoginShouldFailAndAnAppropriateErrorMessageShouldBeDisplayed() {
        testBase.loginPage.isLoginErrorMessageVisible();
    }
}