package org.example.pages.manager;

import io.qameta.allure.Step;
import org.example.config.AppUrls;
import org.example.utils.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

/**
 * Login page for XYZ Bank.
 * #/login – Home, Customer Login, Bank Manager Login.
 * #/customer – Customer dropdown (userSelect) and Login button (after clicking Customer Login).
 */
public class LoginPage {

    private final WebDriver driver;

    // --- #/login (Home) ---
    @FindBy(xpath = "//button[contains(text(),'Customer Login')]")
    private WebElement customerLoginButton;

    @FindBy(xpath = "//button[contains(text(),'Bank Manager Login')]")
    private WebElement managerLoginButton;

    @FindBy(xpath = "//button[contains(text(),'Home')]")
    private WebElement homeButton;

    @FindBy(css = "strong")
    private WebElement pageTitle;

    // --- #/customer (after Customer Login clicked) ---
    @FindBy(id = "userSelect")
    private WebElement userSelect;

    @FindBy(xpath = "//button[normalize-space(text())='Login']")
    private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @Step("Select Customer user type (navigates to #/customer)")
    public LoginPage selectCustomerUserType() {
        SeleniumUtils.waitAndClick(driver, customerLoginButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.CUSTOMER_LOGIN);
        return this;
    }

    @Step("Select Manager user type (navigates to manager)")
    public LoginPage selectManagerUserType() {
        SeleniumUtils.waitAndClick(driver, managerLoginButton);
        SeleniumUtils.waitForUrlContains(driver, "#/manager");
        return this;
    }

    @Step("Select customer: {customerName}")
    public LoginPage selectCustomer(String customerName) {
        SeleniumUtils.waitUntilVisible(driver, userSelect);
        new Select(userSelect).selectByVisibleText(customerName);
        return this;
    }

    @Step("Click Login button (on #/customer)")
    public LoginPage clickLoginButton() {
        SeleniumUtils.waitAndClick(driver, loginButton);
        return this;
    }

    @Step("Login as customer: {customerName}")
    public LoginPage loginAsCustomer(String customerName) {
        selectCustomerUserType();
        selectCustomer(customerName);
        clickLoginButton();
        return this;
    }

    @Step("Login as manager")
    public LoginPage loginAsManager(String managerName) {
        selectManagerUserType();
        return this;
    }

    @Step("Verify login page is displayed (#/login)")
    public boolean isLoginPageDisplayed() {
        return SeleniumUtils.isElementDisplayed(pageTitle);
    }

    @Step("Verify customer option is available")
    public boolean isCustomerOptionAvailable() {
        return SeleniumUtils.isElementDisplayed(customerLoginButton);
    }

    @Step("Verify manager option is available")
    public boolean isManagerOptionAvailable() {
        return SeleniumUtils.isElementDisplayed(managerLoginButton);
    }

    @Step("Click Home button (back to #/login)")
    public LoginPage clickHomeButton() {
        SeleniumUtils.waitAndClick(driver, homeButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.LOGIN);
        return this;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
