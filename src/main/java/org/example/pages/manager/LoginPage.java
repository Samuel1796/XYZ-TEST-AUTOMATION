package org.example.pages.manager;

import io.qameta.allure.Step;
import org.example.config.AppUrls;
import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Login page for XYZ Bank.
 * #/login – Home, Customer Login, Bank Manager Login.
 * #/customer – Customer dropdown (userSelect) and Login button (after clicking Customer Login).
 */
public class LoginPage {

    private final WebDriver driver;

    // --- #/login (Home) ---
    private static final By CUSTOMER_LOGIN_BUTTON = By.xpath("//button[contains(text(),'Customer Login')]");
    private static final By MANAGER_LOGIN_BUTTON = By.xpath("//button[contains(text(),'Bank Manager Login')]");
    private static final By HOME_BUTTON = By.xpath("//button[contains(text(),'Home')]");
    private static final By PAGE_TITLE = By.cssSelector("strong");

    // --- #/customer (after Customer Login clicked) ---
    private static final By USER_SELECT = By.id("userSelect");
    /** Login button on customer login view – exact text to avoid matching "Customer Login" */
    private static final By LOGIN_BUTTON = By.xpath("//button[normalize-space(text())='Login']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    @Step("Select Customer user type (navigates to #/customer)")
    public LoginPage selectCustomerUserType() {
        SeleniumUtils.waitForElementToBeClickable(driver, CUSTOMER_LOGIN_BUTTON).click();
        SeleniumUtils.waitForUrlContains(driver, AppUrls.CUSTOMER_LOGIN);
        return this;
    }

    @Step("Select Manager user type (navigates to manager)")
    public LoginPage selectManagerUserType() {
        SeleniumUtils.waitForElementToBeClickable(driver, MANAGER_LOGIN_BUTTON).click();
        SeleniumUtils.waitForUrlContains(driver, "#/manager");
        return this;
    }

    @Step("Select customer: {customerName}")
    public LoginPage selectCustomer(String customerName) {
        WebElement dropdown = SeleniumUtils.waitForElementToBeVisible(driver, USER_SELECT);
        new org.openqa.selenium.support.ui.Select(dropdown).selectByVisibleText(customerName);
        return this;
    }

    @Step("Click Login button (on #/customer)")
    public LoginPage clickLoginButton() {
        SeleniumUtils.waitForElementToBeClickable(driver, LOGIN_BUTTON).click();
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
        return SeleniumUtils.isElementDisplayed(driver, PAGE_TITLE);
    }

    @Step("Verify customer option is available")
    public boolean isCustomerOptionAvailable() {
        return SeleniumUtils.isElementDisplayed(driver, CUSTOMER_LOGIN_BUTTON);
    }

    @Step("Verify manager option is available")
    public boolean isManagerOptionAvailable() {
        return SeleniumUtils.isElementDisplayed(driver, MANAGER_LOGIN_BUTTON);
    }

    @Step("Click Home button (back to #/login)")
    public LoginPage clickHomeButton() {
        SeleniumUtils.waitForElementToBeClickable(driver, HOME_BUTTON).click();
        SeleniumUtils.waitForUrlContains(driver, AppUrls.LOGIN);
        return this;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
