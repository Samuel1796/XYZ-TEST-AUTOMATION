package org.example.pages.manager;

import io.qameta.allure.Step;
import org.example.config.AppUrls;
import org.example.utils.SeleniumUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Manager area: Add Customer (#/manager/addCust), Open Account (#/manager/openAccount), Customers list (#/manager/list).
 */
public class ManagerDashboardPage {

    private final WebDriver driver;

    // --- Navigation (visible on any manager view) ---
    private static final By ADD_CUSTOMER_BUTTON = By.xpath("//button[contains(text(),'Add Customer')]");
    private static final By OPEN_ACCOUNT_BUTTON = By.xpath("//button[contains(text(),'Open Account')]");
    private static final By CUSTOMERS_BUTTON = By.xpath("//button[contains(text(),'Customers')]");
    private static final By HOME_BUTTON = By.xpath("//button[contains(text(),'Home')]");
    private static final By DASHBOARD_TITLE = By.cssSelector("div.center strong");

    // --- #/manager/addCust (Add Customer form) ---
    private static final By FIRST_NAME_INPUT = By.xpath("//input[@ng-model='fName']");
    private static final By LAST_NAME_INPUT = By.xpath("//input[@ng-model='lName']");
    private static final By POSTAL_CODE_INPUT = By.xpath("//input[@ng-model='postCd']");
    /** Submit button inside the Add Customer form (form contains postCd input) */
    private static final By ADD_CUSTOMER_SUBMIT = By.xpath("//form[.//input[@ng-model='postCd']]//button");

    // --- #/manager/openAccount (Open Account form) ---
    // Customer dropdown: use select that precedes currency on this page (avoids matching #/customer userSelect)
    private static final By CUSTOMER_SELECT_OPEN_ACCOUNT = By.xpath("//select[@id='currency']/preceding::select[1]");
    private static final By CURRENCY_SELECT = By.id("currency");
    private static final By PROCESS_BUTTON = By.xpath("//button[contains(text(),'Process')]");

    // --- Messages (context-dependent) ---
    private static final By SUCCESS_MESSAGE = By.cssSelector("span.ng-binding.ng-scope");
    private static final By ERROR_MESSAGE = By.cssSelector("span.error, .error-message");

    public ManagerDashboardPage(WebDriver driver) {
        this.driver = driver;
    }

    @Step("Verify manager dashboard is displayed")
    public boolean isDashboardDisplayed() {
        return SeleniumUtils.isElementDisplayed(driver, DASHBOARD_TITLE);
    }

    @Step("Click Add Customer (navigate to #/manager/addCust)")
    public ManagerDashboardPage clickAddCustomerButton() {
        SeleniumUtils.click(driver, ADD_CUSTOMER_BUTTON);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.MANAGER_ADD_CUSTOMER);
        return this;
    }

    @Step("Enter customer name: {customerName}")
    public ManagerDashboardPage enterCustomerName(String customerName) {
        SeleniumUtils.sendKeys(driver, FIRST_NAME_INPUT, customerName);
        SeleniumUtils.sendKeys(driver, LAST_NAME_INPUT, customerName);
        return this;
    }

    @Step("Enter postal code: {postalCode}")
    public ManagerDashboardPage enterPostalCode(String postalCode) {
        SeleniumUtils.sendKeys(driver, POSTAL_CODE_INPUT, postalCode);
        return this;
    }

    @Step("Submit Add Customer form")
    public ManagerDashboardPage submitCustomerForm() {
        WaitUtils.smallWait();
        SeleniumUtils.click(driver, ADD_CUSTOMER_SUBMIT);
        WaitUtils.smallWait();
        SeleniumUtils.acceptAlert(driver);
        return this;
    }

    @Step("Add customer: {customerName} with postal code: {postalCode}")
    public ManagerDashboardPage addCustomer(String customerName, String postalCode) {
        clickAddCustomerButton()
                .enterCustomerName(customerName)
                .enterPostalCode(postalCode)
                .submitCustomerForm();
        return this;
    }

    @Step("Click Open Account (navigate to #/manager/openAccount)")
    public ManagerDashboardPage clickOpenAccountButton() {
        SeleniumUtils.click(driver, OPEN_ACCOUNT_BUTTON);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.MANAGER_OPEN_ACCOUNT);
        return this;
    }

    @Step("Select customer for account: {customerName}")
    public ManagerDashboardPage selectCustomerForAccount(String customerName) {
        SeleniumUtils.waitForDropdownToContainOption(driver, CUSTOMER_SELECT_OPEN_ACCOUNT, customerName);
        WebElement dropdown = SeleniumUtils.waitForElementToBeClickable(driver, CUSTOMER_SELECT_OPEN_ACCOUNT);
        new org.openqa.selenium.support.ui.Select(dropdown).selectByVisibleText(customerName);
        return this;
    }

    @Step("Select currency: {currency}")
    public ManagerDashboardPage selectCurrency(String currency) {
        WebElement dropdown = SeleniumUtils.waitForElementToBeClickable(driver, CURRENCY_SELECT);
        new org.openqa.selenium.support.ui.Select(dropdown).selectByVisibleText(currency);
        return this;
    }

    @Step("Click Process button")
    public ManagerDashboardPage clickProcessButton() {
        SeleniumUtils.click(driver, PROCESS_BUTTON);
        return this;
    }

    @Step("Create account for customer: {customerName} with currency: {currency}")
    public ManagerDashboardPage createAccount(String customerName, String currency) {
        clickOpenAccountButton()
                .selectCustomerForAccount(customerName)
                .selectCurrency(currency)
                .clickProcessButton();
        WaitUtils.smallWait();
        SeleniumUtils.acceptAlert(driver);
        return this;
    }

    @Step("Get success message")
    public String getSuccessMessage() {
        try {
            return SeleniumUtils.getText(driver, SUCCESS_MESSAGE).trim();
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Get error message")
    public String getErrorMessage() {
        try {
            return SeleniumUtils.getText(driver, ERROR_MESSAGE).trim();
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Click Customers (navigate to #/manager/list)")
    public ManagerDashboardPage clickCustomersButton() {
        SeleniumUtils.click(driver, CUSTOMERS_BUTTON);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.MANAGER_CUSTOMERS_LIST);
        return this;
    }

    @Step("Delete customer: {customerName}")
    public ManagerDashboardPage deleteCustomer(String customerName) {
        By deleteBtn = By.xpath("//td[contains(text(),'" + customerName + "')]/..//button[contains(text(),'Delete')]");
        SeleniumUtils.click(driver, deleteBtn);
        return this;
    }

    @Step("Check if customer exists: {customerName}")
    public boolean customerExists(String customerName) {
        By row = By.xpath("//td[contains(text(),'" + customerName + "')]");
        return SeleniumUtils.isElementDisplayed(driver, row);
    }

    @Step("Click Home button (back to #/login)")
    public ManagerDashboardPage clickHomeButton() {
        SeleniumUtils.waitForElementToBeClickable(driver, HOME_BUTTON).click();
        SeleniumUtils.waitForUrlContains(driver, AppUrls.LOGIN);
        return this;
    }
}
