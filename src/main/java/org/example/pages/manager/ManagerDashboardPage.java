package org.example.pages.manager;

import io.qameta.allure.Step;
import org.example.config.AppUrls;
import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

/**
 * Manager area: Add Customer (#/manager/addCust), Open Account (#/manager/openAccount), Customers list (#/manager/list).
 */
public class ManagerDashboardPage {

    private final WebDriver driver;

    // --- Navigation (visible on any manager view) ---
    @FindBy(xpath = "//button[contains(text(),'Add Customer')]")
    private WebElement addCustomerButton;

    @FindBy(xpath = "//button[contains(text(),'Open Account')]")
    private WebElement openAccountButton;

    @FindBy(xpath = "//button[contains(text(),'Customers')]")
    private WebElement customersButton;

    @FindBy(xpath = "//button[contains(text(),'Home')]")
    private WebElement homeButton;

    @FindBy(css = "div.center strong")
    private WebElement dashboardTitle;

    // --- #/manager/addCust (Add Customer form) ---
    @FindBy(xpath = "//input[@ng-model='fName']")
    private WebElement firstNameInput;

    @FindBy(xpath = "//input[@ng-model='lName']")
    private WebElement lastNameInput;

    @FindBy(xpath = "//input[@ng-model='postCd']")
    private WebElement postalCodeInput;

    @FindBy(xpath = "//form[.//input[@ng-model='postCd']]//button")
    private WebElement addCustomerSubmit;

    // --- #/manager/openAccount (Open Account form) ---
    @FindBy(xpath = "//select[@id='currency']/preceding::select[1]")
    private WebElement customerSelectOpenAccount;

    @FindBy(id = "currency")
    private WebElement currencySelect;

    @FindBy(xpath = "//button[contains(text(),'Process')]")
    private WebElement processButton;

    // --- Messages (context-dependent) ---
    @FindBy(css = "span.ng-binding.ng-scope")
    private WebElement successMessage;

    @FindBy(css = "span.error, .error-message")
    private WebElement errorMessage;

    /** By locator kept for waitForDropdownToContainOption (needs By) */
    private static final By CUSTOMER_SELECT_OPEN_ACCOUNT_BY = By.xpath("//select[@id='currency']/preceding::select[1]");

    public ManagerDashboardPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @Step("Verify manager dashboard is displayed")
    public boolean isDashboardDisplayed() {
        return SeleniumUtils.isElementDisplayed(dashboardTitle);
    }

    @Step("Click Add Customer (navigate to #/manager/addCust)")
    public ManagerDashboardPage clickAddCustomerButton() {
        SeleniumUtils.waitAndClick(driver, addCustomerButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.MANAGER_ADD_CUSTOMER);
        return this;
    }

    @Step("Enter customer name: {customerName}")
    public ManagerDashboardPage enterCustomerName(String customerName) {
        SeleniumUtils.clearAndType(driver, firstNameInput, customerName);
        SeleniumUtils.clearAndType(driver, lastNameInput, customerName);
        return this;
    }

    @Step("Enter postal code: {postalCode}")
    public ManagerDashboardPage enterPostalCode(String postalCode) {
        SeleniumUtils.clearAndType(driver, postalCodeInput, postalCode);
        return this;
    }

    @Step("Submit Add Customer form")
    public ManagerDashboardPage submitCustomerForm() {
        SeleniumUtils.waitAndClick(driver, addCustomerSubmit);
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
        SeleniumUtils.waitAndClick(driver, openAccountButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.MANAGER_OPEN_ACCOUNT);
        return this;
    }

    @Step("Select customer for account: {customerName}")
    public ManagerDashboardPage selectCustomerForAccount(String customerName) {
        SeleniumUtils.waitForDropdownToContainOption(driver, CUSTOMER_SELECT_OPEN_ACCOUNT_BY, customerName);
        SeleniumUtils.waitUntilClickable(driver, customerSelectOpenAccount);
        new Select(customerSelectOpenAccount).selectByVisibleText(customerName);
        return this;
    }

    @Step("Select currency: {currency}")
    public ManagerDashboardPage selectCurrency(String currency) {
        SeleniumUtils.waitUntilClickable(driver, currencySelect);
        new Select(currencySelect).selectByVisibleText(currency);
        return this;
    }

    @Step("Click Process button")
    public ManagerDashboardPage clickProcessButton() {
        SeleniumUtils.waitAndClick(driver, processButton);
        return this;
    }

    @Step("Create account for customer: {customerName} with currency: {currency}")
    public ManagerDashboardPage createAccount(String customerName, String currency) {
        clickOpenAccountButton()
                .selectCustomerForAccount(customerName)
                .selectCurrency(currency)
                .clickProcessButton();
        SeleniumUtils.acceptAlert(driver);
        return this;
    }

    @Step("Get success message")
    public String getSuccessMessage() {
        try {
            SeleniumUtils.waitUntilVisible(driver, successMessage);
            return successMessage.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Get error message")
    public String getErrorMessage() {
        try {
            SeleniumUtils.waitUntilVisible(driver, errorMessage);
            return errorMessage.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Click Customers (navigate to #/manager/list)")
    public ManagerDashboardPage clickCustomersButton() {
        SeleniumUtils.waitAndClick(driver, customersButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.MANAGER_CUSTOMERS_LIST);
        return this;
    }

    @Step("Delete customer: {customerName}")
    public ManagerDashboardPage deleteCustomer(String customerName) {
        By deleteBtn = By.xpath("//td[contains(text(),'" + customerName + "')]/..//button[contains(text(),'Delete')]");
        WebElement deleteBtnElement = SeleniumUtils.waitForElementToBePresent(driver, deleteBtn);
        SeleniumUtils.scrollAndClick(driver, deleteBtnElement);
        return this;
    }

    @Step("Check if customer exists: {customerName}")
    public boolean customerExists(String customerName) {
        By row = By.xpath("//td[contains(text(),'" + customerName + "')]");
        return SeleniumUtils.isElementDisplayed(driver, row);
    }

    @Step("Click Home button (back to #/login)")
    public ManagerDashboardPage clickHomeButton() {
        SeleniumUtils.waitAndClick(driver, homeButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.LOGIN);
        return this;
    }
}
