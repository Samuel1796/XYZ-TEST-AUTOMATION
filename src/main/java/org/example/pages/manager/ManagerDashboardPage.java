package org.example.pages.manager;

import io.qameta.allure.Step;
import org.example.config.AppUrls;
import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

/**
 * Page object for the Bank Manager area. Covers three views: Add Customer (#/manager/addCust) – first name, last name,
 * postal code and submit; Open Account (#/manager/openAccount) – customer dropdown, currency dropdown, Process;
 * Customers list (#/manager/list) – table of customers with Delete buttons. Navigation buttons (Add Customer, Open Account,
 * Customers, Home) are visible on all manager views.
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
    private static final By CUSTOMER_SELECT_OPEN_ACCOUNT_BY = By.xpath("//select[@id='currency']/preceding::select[1]");

    @FindBy(id = "currency")
    private WebElement currencySelect;

    @FindBy(xpath = "//button[contains(text(),'Process')]")
    private WebElement processButton;

    // --- Messages (context-dependent) ---
    @FindBy(css = "span.error, .error-message")
    private WebElement errorMessage;

    /**
     * Creates the page object and initializes PageFactory locators.
     *
     * @param driver the WebDriver instance (expected on a manager view or base URL)
     */
    public ManagerDashboardPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
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

    /** Clicks Add Customer submit, accepts alert. Use when success message is not needed. */
    @Step("Submit Add Customer form")
    public ManagerDashboardPage submitCustomerForm() {
        submitAddCustomerFormAndAcceptAlert();
        return this;
    }

    /**
     * Submits the Add Customer form, captures the JS alert text, accepts the alert, and returns the text.
     * Use for assertions when a customer is added successfully.
     */
    @Step("Submit Add Customer form and get alert message")
    public String submitCustomerFormAndGetAlertMessage() {
        return submitAddCustomerFormAndAcceptAlert();
    }

    private String submitAddCustomerFormAndAcceptAlert() {
        SeleniumUtils.waitAndClick(driver, addCustomerSubmit);
        String alertText = SeleniumUtils.getAlertText(driver);
        SeleniumUtils.acceptAlert(driver);
        return alertText;
    }

    /**
     * Adds a customer and returns the JS alert message shown on success.
     */
    @Step("Add customer and get alert message: {customerName}")
    public String addCustomerAndGetAlertMessage(String customerName, String postalCode) {
        clickAddCustomerButton()
                .enterCustomerName(customerName)
                .enterPostalCode(postalCode);
        return submitCustomerFormAndGetAlertMessage();
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
        WebElement selectEl = SeleniumUtils.waitForElementToBeVisible(driver, CUSTOMER_SELECT_OPEN_ACCOUNT_BY);
        new Select(selectEl).selectByVisibleText(customerName);
        return this;
    }

    @Step("Select currency: {currency}")
    public ManagerDashboardPage selectCurrency(String currency) {
        SeleniumUtils.waitUntilClickable(driver, currencySelect);
        new Select(currencySelect).selectByVisibleText(currency);
        return this;
    }

    @Step("Click Process button")
    public void clickProcessButton() {
        SeleniumUtils.waitAndClick(driver, processButton);
    }

    @Step("Create account for customer: {customerName} with currency: {currency}")
    public ManagerDashboardPage createAccount(String customerName, String currency) {
        createAccountAndGetAlertMessage(customerName, currency);
        return this;
    }

    /**
     * Creates an account and returns the JS alert message shown on success.
     */
    @Step("Create account and get alert message: {customerName} / {currency}")
    public String createAccountAndGetAlertMessage(String customerName, String currency) {
        clickOpenAccountButton()
                .selectCustomerForAccount(customerName)
                .selectCurrency(currency)
                .clickProcessButton();
        String alertText = SeleniumUtils.getAlertText(driver);
        SeleniumUtils.acceptAlert(driver);
        return alertText;
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
    public void clickCustomersButton() {
        SeleniumUtils.waitAndClick(driver, customersButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.MANAGER_CUSTOMERS_LIST);
    }

    /**
     * Finds the customer row in the Customers table by matching display name (first cell + " " + second cell),
     * scrolls the row into view (so Delete is visible in viewport), then clicks the row's Delete button.
     *
     * @param customerName display name as shown in table (e.g. "FirstName LastName")
     * @throws NoSuchElementException if no row matches the name
     */
    @Step("Scroll to customer and delete: {customerName}")
    public void scrollToCustomerAndDelete(String customerName) {
        WebElement customersTable = SeleniumUtils.waitForElementToBeVisible(driver, By.cssSelector("table.table"));
        List<WebElement> rows = customersTable.findElements(By.tagName("tr"));

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 2) {
                String rowName = cells.get(0).getText().trim() + " " + cells.get(1).getText().trim();
                if (rowName.equals(customerName)) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", row);
                    WebElement deleteBtn = row.findElement(By.xpath(".//button[contains(text(),'Delete')]"));
                    SeleniumUtils.waitUntilClickable(driver, deleteBtn).click();
                    return;
                }
            }
        }
        throw new NoSuchElementException("Customer row not found for: " + customerName);
    }

    /** Returns true if any table cell contains the given customer name (used after delete to confirm removal). */
    @Step("Check if customer exists: {customerName}")
    public boolean customerExists(String customerName) {
        By row = By.xpath("//td[contains(text(),'" + customerName + "')]");
        return SeleniumUtils.isElementDisplayed(driver, row);
    }

    @Step("Click Home button (back to #/login)")
    public void clickHomeButton() {
        SeleniumUtils.waitAndClick(driver, homeButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.LOGIN);
    }
}
