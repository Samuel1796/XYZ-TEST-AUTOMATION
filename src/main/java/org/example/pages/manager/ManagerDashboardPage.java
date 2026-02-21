package org.example.pages.manager;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pages.BasePage;
import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object for Manager Dashboard Page.
 * Contains action-based methods for manager operations:
 * - Adding customers
 * - Creating accounts
 * - Deleting accounts
 *
 * @author QA Team
 * @version 1.0
 */
public class ManagerDashboardPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(ManagerDashboardPage.class);

    // Navigation Buttons
    private static final By ADD_CUSTOMER_BUTTON = By.xpath("//button[@ng-class='btnClass1']");
    private static final By OPEN_ACCOUNT_BUTTON = By.xpath("//button[@ng-class='btnClass2']");
    private static final By CUSTOMERS_BUTTON = By.xpath("//button[@ng-class='btnClass3']");

    // Customer Form Fields
    private static final By FIRST_NAME_INPUT = By.xpath("//input[@ng-model='fName']");
    private static final By LAST_NAME_INPUT = By.xpath("//input[@ng-model='lName']");
    private static final By POSTAL_CODE_INPUT = By.xpath("//input[@ng-model='postCd']");
    private static final By ADD_CUSTOMER_SUBMIT = By.xpath("//form[@ng-submit='addCust()']//button[@type='submit']");

    // Account Form Fields
    private static final By CUSTOMER_SELECT = By.id("userSelect");
    private static final By CURRENCY_SELECT = By.id("currency");
    private static final By PROCESS_BUTTON = By.xpath("//button[contains(text(),'Process')]");

    // Customer Search
    private static final By SEARCH_CUSTOMER_INPUT = By.xpath("//input[@ng-model='searchCust']");

    // Dashboard Elements
    private static final By DASHBOARD_TITLE = By.xpath("//div[contains(@class,'center')]/strong[contains(text(),'XYZ Bank')]");
    private static final By SUCCESS_MESSAGE = By.xpath("//span[contains(@ng-show,'message')]");
    private static final By ERROR_MESSAGE = By.xpath("//span[contains(@class,'error')]");

    /**
     * Constructor
     *
     * @param driver the WebDriver instance
     */
    public ManagerDashboardPage(WebDriver driver) {
        super(driver);
        logger.info("Manager Dashboard page initialized");
    }

    /**
     * Checks if manager dashboard is displayed
     *
     * @return true if dashboard is displayed
     */
    @Step("Verify manager dashboard is displayed")
    public boolean isDashboardDisplayed() {
        logger.info("Checking if manager dashboard is displayed");
        return isElementDisplayed(DASHBOARD_TITLE);
    }

    /**
     * Clicks on Add Customer button
     *
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Click Add Customer button")
    public ManagerDashboardPage clickAddCustomerButton() {
        logger.info("Clicking Add Customer button");
        click(ADD_CUSTOMER_BUTTON);
        return this;
    }

    /**
     * Enters customer name
     *
     * @param customerName the customer name to enter
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Enter customer name: {customerName}")
    public ManagerDashboardPage enterCustomerName(String customerName) {
        logger.info("Entering customer name: " + customerName);
        sendText(FIRST_NAME_INPUT, customerName);
        sendText(LAST_NAME_INPUT, customerName); // Use same name as last name for simplicity
        return this;
    }

    /**
     * Enters postal code
     *
     * @param postalCode the postal code to enter
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Enter postal code: {postalCode}")
    public ManagerDashboardPage enterPostalCode(String postalCode) {
        logger.info("Entering postal code: " + postalCode);
        sendText(POSTAL_CODE_INPUT, postalCode);
        return this;
    }

    /**
     * Submits the customer form
     *
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Submit customer form")
    public ManagerDashboardPage submitCustomerForm() {
        logger.info("Submitting customer form");
        click(ADD_CUSTOMER_SUBMIT);
        // Handle the alert that appears after adding a customer
        handleAlert();
        return this;
    }

    /**
     * Adds a new customer with complete flow
     *
     * @param customerName the customer name
     * @param postalCode the postal code
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Add customer: {customerName} with postal code: {postalCode}")
    public ManagerDashboardPage addCustomer(String customerName, String postalCode) {
        logger.info("Adding customer: " + customerName);
        clickAddCustomerButton()
                .enterCustomerName(customerName)
                .enterPostalCode(postalCode)
                .submitCustomerForm();
        return this;
    }

    /**
     * Clicks on Open Account button
     *
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Click Open Account button")
    public ManagerDashboardPage clickOpenAccountButton() {
        logger.info("Clicking Open Account button");
        click(OPEN_ACCOUNT_BUTTON);
        return this;
    }

    /**
     * Selects customer from dropdown for account creation
     *
     * @param customerName the customer name to select
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Select customer for account: {customerName}")
    public ManagerDashboardPage selectCustomerForAccount(String customerName) {
        logger.info("Selecting customer for account: " + customerName);
        selectFromDropdown(CUSTOMER_SELECT, customerName);
        return this;
    }

    /**
     * Selects currency for account
     *
     * @param currency the currency to select
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Select currency: {currency}")
    public ManagerDashboardPage selectCurrency(String currency) {
        logger.info("Selecting currency: " + currency);
        selectFromDropdown(CURRENCY_SELECT, currency);
        return this;
    }

    /**
     * Clicks Process button for account creation
     *
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Click Process button")
    public ManagerDashboardPage clickProcessButton() {
        logger.info("Clicking Process button");
        click(PROCESS_BUTTON);
        return this;
    }

    /**
     * Creates account for a customer with complete flow
     *
     * @param customerName the customer name
     * @param currency the currency
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Create account for customer: {customerName} with currency: {currency}")
    public ManagerDashboardPage createAccount(String customerName, String currency) {
        logger.info("Creating account for customer: " + customerName);
        clickOpenAccountButton()
                .selectCustomerForAccount(customerName)
                .selectCurrency(currency)
                .clickProcessButton();
        return this;
    }

    /**
     * Gets success message
     *
     * @return success message text
     */
    @Step("Get success message")
    public String getSuccessMessage() {
        logger.info("Getting success message");
        try {
            String message = getText(SUCCESS_MESSAGE);
            logger.debug("Success message: " + message);
            return message;
        } catch (Exception e) {
            logger.warn("Success message not found");
            return "";
        }
    }

    /**
     * Gets error message
     *
     * @return error message text
     */
    @Step("Get error message")
    public String getErrorMessage() {
        logger.info("Getting error message");
        try {
            String message = getText(ERROR_MESSAGE);
            logger.debug("Error message: " + message);
            return message;
        } catch (Exception e) {
            logger.warn("Error message not found");
            return "";
        }
    }

    /**
     * Clicks on Customers button to view customer list
     *
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Click Customers button")
    public ManagerDashboardPage clickCustomersButton() {
        logger.info("Clicking Customers button");
        click(CUSTOMERS_BUTTON);
        return this;
    }

    /**
     * Deletes a customer from the customers list
     *
     * @param customerName the customer name to delete
     * @return ManagerDashboardPage instance for fluent API
     */
    @Step("Delete customer: {customerName}")
    public ManagerDashboardPage deleteCustomer(String customerName) {
        logger.info("Deleting customer: " + customerName);
        By deleteButton = By.xpath("//td[contains(text(), '" + customerName + "')]/parent::tr//button[contains(text(), 'Delete')]");
        click(deleteButton);
        return this;
    }

    /**
     * Checks if customer exists in the list
     *
     * @param customerName the customer name to search for
     * @return true if customer exists
     */
    @Step("Check if customer exists: {customerName}")
    public boolean customerExists(String customerName) {
        logger.info("Checking if customer exists: " + customerName);
        try {
            By customerRow = By.xpath("//td[contains(text(), '" + customerName + "')]");
            return isElementDisplayed(customerRow);
        } catch (Exception e) {
            logger.debug("Customer not found");
            return false;
        }
    }

    /**
     * Helper method to select from dropdown
     *
     * @param locator the dropdown locator
     * @param value the value to select
     */
    private void selectFromDropdown(By locator, String value) {
        logger.debug("Selecting from dropdown: " + value);
        WebElement dropdown = SeleniumUtils.waitForElementToBeClickable(driver, locator);
        new org.openqa.selenium.support.ui.Select(dropdown).selectByVisibleText(value);
    }

    /**
     * Handles JavaScript alert dialogs
     */
    private void handleAlert() {
        try {
            Thread.sleep(500); // Brief wait for alert to appear
            driver.switchTo().alert().accept();
            logger.debug("Alert accepted");
        } catch (Exception e) {
            logger.debug("No alert present or already handled");
        }
    }
}

