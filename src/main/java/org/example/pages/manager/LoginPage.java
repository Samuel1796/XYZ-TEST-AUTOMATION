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

import java.util.List;

/**
 * Page object for the XYZ Bank login flow. Covers two views: (1) Home (#/login) with Customer Login and
 * Bank Manager Login buttons; (2) Customer selection (#/customer) with dropdown {@code userSelect} and Login button.
 * Manager login goes directly to manager home after clicking Bank Manager Login.
 */
public class LoginPage {

    private final WebDriver driver;

    // --- #/login (Home) ---
    @FindBy(xpath = "//button[contains(text(),'Customer Login')]")
    private WebElement customerLoginButton;

    @FindBy(xpath = "//button[contains(text(),'Bank Manager Login')]")
    private WebElement managerLoginButton;

    // --- #/customer (after Customer Login clicked) ---
    @FindBy(id = "userSelect")
    private WebElement userSelect;

    @FindBy(xpath = "//button[normalize-space(text())='Login']")
    private WebElement loginButton;

    /**
     * Creates the page object and initializes PageFactory elements for the current driver.
     *
     * @param driver the WebDriver instance (must be on the app base URL)
     */
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    /** Clicks Customer Login and waits until URL contains #/customer (dropdown page). */
    @Step("Select Customer user type (navigates to #/customer)")
    public LoginPage selectCustomerUserType() {
        SeleniumUtils.waitAndClick(driver, customerLoginButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.CUSTOMER_LOGIN);
        return this;
    }

    @Step("Select Manager user type (navigates to manager)")
    public LoginPage selectManagerUserType() {
        SeleniumUtils.waitAndClick(driver, managerLoginButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.MANAGER_HOME);
        return this;
    }

    @Step("Select customer: {customerName}")
    public void selectCustomer(String customerName) {
        SeleniumUtils.waitUntilVisible(driver, userSelect);
        new Select(userSelect).selectByVisibleText(customerName);
    }

    @Step("Click Login button (on #/customer)")
    public void clickLoginButton() {
        SeleniumUtils.waitAndClick(driver, loginButton);
    }

    /** Full customer login: select Customer Login → choose customer from dropdown → click Login. Lands on #/account. */
    @Step("Login as customer: {customerName}")
    public LoginPage loginAsCustomer(String customerName) {
        selectCustomerUserType();
        selectCustomer(customerName);
        clickLoginButton();
        return this;
    }

    /** Clicks Bank Manager Login and waits for manager home (#/manager). No customer selection. */
    @Step("Login as manager")
    public LoginPage loginAsManager() {
        selectManagerUserType();
        return this;
    }

    /** Returns true if the given display name (e.g. "First Last") appears in the customer dropdown on #/customer. */
    @Step("Check if customer is in dropdown: {customerName}")
    public boolean isCustomerInDropdown(String customerName) {
        SeleniumUtils.waitUntilVisible(driver, userSelect);
        List<WebElement> options = userSelect.findElements(By.tagName("option"));
        return options.stream()
                .anyMatch(option -> option.getText().trim().equals(customerName));
    }
}