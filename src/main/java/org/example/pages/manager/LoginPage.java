package org.example.pages.manager;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object for Login Page.
 * Contains action-based methods for login functionality.
 * Used by both bank managers and customers.
 *
 * @author QA Team
 * @version 1.0
 */
public class LoginPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(LoginPage.class);

    // Locators
    private static final By CUSTOMER_LOGIN_BUTTON = By.xpath("//button[contains(text(),'Customer Login')]");
    private static final By MANAGER_LOGIN_BUTTON = By.xpath("//button[contains(text(),'Bank Manager Login')]");
    private static final By USER_SELECT_DROPDOWN = By.id("userSelect");
    private static final By LOGIN_BUTTON = By.xpath("//button[contains(text(),'Login')]");
    private static final By PAGE_TITLE = By.xpath("//strong[contains(text(),'XYZ Bank')]");
    private static final By ERROR_MESSAGE = By.cssSelector(".error-message");
    private static final By HOME_BUTTON = By.xpath("//button[contains(text(),'Home')]");

    /**
     * Constructor
     *
     * @param driver the WebDriver instance
     */
    public LoginPage(WebDriver driver) {
        super(driver);
        logger.info("Login page initialized");
    }

    /**
     * Selects Customer user type
     *
     * @return LoginPage instance for fluent API
     */
    @Step("Select Customer user type")
    public LoginPage selectCustomerUserType() {
        logger.info("Selecting Customer user type");
        click(CUSTOMER_LOGIN_BUTTON);
        return this;
    }

    /**
     * Selects Manager user type
     *
     * @return LoginPage instance for fluent API
     */
    @Step("Select Manager user type")
    public LoginPage selectManagerUserType() {
        logger.info("Selecting Manager user type");
        click(MANAGER_LOGIN_BUTTON);
        return this;
    }

    /**
     * Selects a customer from the dropdown
     *
     * @param customerName the customer name to select
     * @return LoginPage instance for fluent API
     */
    @Step("Select customer: {customerName}")
    public LoginPage selectCustomer(String customerName) {
        logger.info("Selecting customer: " + customerName);
        WebElement dropdown = findElement(USER_SELECT_DROPDOWN);
        new org.openqa.selenium.support.ui.Select(dropdown).selectByVisibleText(customerName);
        return this;
    }

    /**
     * Selects a manager from the dropdown (not used in XYZ Bank - manager login goes directly to dashboard)
     *
     * @param managerName the manager name to select
     * @return LoginPage instance for fluent API
     */
    @Step("Select manager: {managerName}")
    public LoginPage selectManager(String managerName) {
        logger.info("Selecting manager: " + managerName);
        // In XYZ Bank, clicking Bank Manager Login goes directly to manager dashboard
        return this;
    }

    /**
     * Clicks the login button
     *
     * @return LoginPage instance for fluent API
     */
    @Step("Click login button")
    public LoginPage clickLoginButton() {
        logger.info("Clicking login button");
        click(LOGIN_BUTTON);
        return this;
    }

    /**
     * Performs customer login with fluent API
     *
     * @param customerName the customer name
     * @return LoginPage instance
     */
    @Step("Login as customer: {customerName}")
    public LoginPage loginAsCustomer(String customerName) {
        logger.info("Logging in as customer: " + customerName);
        selectCustomerUserType()
                .selectCustomer(customerName)
                .clickLoginButton();
        return this;
    }

    /**
     * Performs manager login with fluent API
     *
     * @param managerName the manager name
     * @return LoginPage instance
     */
    @Step("Login as manager: {managerName}")
    public LoginPage loginAsManager(String managerName) {
        logger.info("Logging in as manager: " + managerName);
        selectManagerUserType();
        // In XYZ Bank, clicking Bank Manager Login goes directly to manager dashboard
        return this;
    }

    /**
     * Checks if login page is displayed
     *
     * @return true if login page is displayed
     */
    @Step("Verify login page is displayed")
    public boolean isLoginPageDisplayed() {
        logger.info("Checking if login page is displayed");
        return isElementDisplayed(PAGE_TITLE);
    }

    /**
     * Gets error message if displayed
     *
     * @return error message text
     */
    @Step("Get error message")
    public String getErrorMessage() {
        logger.info("Getting error message");
        try {
            return getText(ERROR_MESSAGE);
        } catch (Exception e) {
            logger.warn("Error message not found");
            return "";
        }
    }

    /**
     * Checks if customer login button is visible
     *
     * @return true if customer login button is visible
     */
    @Step("Verify customer option is available")
    public boolean isCustomerOptionAvailable() {
        return isElementDisplayed(CUSTOMER_LOGIN_BUTTON);
    }

    /**
     * Checks if manager login button is visible
     *
     * @return true if manager login button is visible
     */
    @Step("Verify manager option is available")
    public boolean isManagerOptionAvailable() {
        return isElementDisplayed(MANAGER_LOGIN_BUTTON);
    }

    /**
     * Clicks Home button to return to login page
     *
     * @return LoginPage instance for fluent API
     */
    @Step("Click Home button")
    public LoginPage clickHomeButton() {
        logger.info("Clicking Home button");
        click(HOME_BUTTON);
        return this;
    }
}

