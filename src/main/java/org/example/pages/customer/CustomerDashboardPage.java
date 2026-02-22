package org.example.pages.customer;

import io.qameta.allure.Step;
import org.example.config.AppUrls;
import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer account page (#/account). Deposit and Withdraw use the same URL;
 * form and button change when Deposit or Withdraw is clicked.
 * Transactions (#/listTx) – separate view.
 */
public class CustomerDashboardPage {

    private final WebDriver driver;



    // --- #/account – tab buttons ---
    @FindBy(xpath = "//button[contains(@ng-click,'deposit')]")
    private WebElement depositTabButton;

    @FindBy(xpath = "//button[contains(@ng-click,'withdrawl')]")
    private WebElement withdrawTabButton;

    @FindBy(xpath = "//button[contains(text(),'Transactions')]")
    private WebElement transactionsButton;

    @FindBy(xpath = "//button[contains(text(),'Logout')]")
    private WebElement logoutButton;

    // --- #/account – form shown after clicking Deposit or Withdraw tab ---
    @FindBy(css = "input[ng-model='amount']")
    private WebElement amountInput;

    @FindBy(xpath = "//button[@type='submit' and contains(text(),'Deposit')]")
    private WebElement depositSubmitButton;

    @FindBy(xpath = "//button[@type='submit' and contains(text(),'Withdraw')]")
    private WebElement withdrawSubmitButton;

    // --- #/account – account info ---
    @FindBy(xpath = "//div[contains(.,'Account Number')]//span[@class='ng-binding']")
    private WebElement accountNumber;

    @FindBy(xpath = "//div[contains(.,'Balance')]//span[@class='ng-binding']")
    private WebElement accountBalance;

    @FindBy(xpath = "//strong[contains(text(),'Account')]")
    private WebElement dashboardTitle;

    // --- Messages on #/account ---
    @FindBy(css = ".alert-success")
    private WebElement successMessage;

    @FindBy(css = ".alert-danger, .error")
    private WebElement errorMessage;

    /** By locator kept for findElements (transaction rows list) */
    private static final By TRANSACTION_ROWS = By.cssSelector("table tbody tr");

    public CustomerDashboardPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    /** Ensure we are on customer account page. */
    public CustomerDashboardPage ensureOnAccountPage() {
        SeleniumUtils.waitForUrlContains(driver, AppUrls.CUSTOMER_ACCOUNT);
        return this;
    }

    @Step("Verify customer account page is displayed (#/account)")
    public boolean isDashboardDisplayed() {
        return SeleniumUtils.isElementDisplayed(dashboardTitle);
    }

    @Step("Click Deposit tab (form appears on same #/account)")
    public CustomerDashboardPage clickDepositButton() {
        SeleniumUtils.waitAndClick(driver, depositTabButton);
        SeleniumUtils.waitUntilVisible(driver, amountInput);
        return this;
    }

    @Step("Enter deposit amount: {amount}")
    public CustomerDashboardPage enterDepositAmount(String amount) {
        SeleniumUtils.clearAndType(driver, amountInput, amount);
        return this;
    }

    @Step("Submit deposit")
    public CustomerDashboardPage submitDeposit() {
        SeleniumUtils.waitAndClick(driver, depositSubmitButton);
        return this;
    }

    @Step("Deposit amount: {amount}")
    public CustomerDashboardPage deposit(String amount) {
        ensureOnAccountPage();
        clickDepositButton().enterDepositAmount(amount).submitDeposit();
        return this;
    }

    @Step("Click Withdraw tab (form appears on same #/account)")
    public CustomerDashboardPage clickWithdrawButton() {
        SeleniumUtils.waitAndClick(driver, withdrawTabButton);
        SeleniumUtils.waitUntilVisible(driver, amountInput);
        return this;
    }

    @Step("Enter withdrawal amount: {amount}")
    public CustomerDashboardPage enterWithdrawalAmount(String amount) {
        SeleniumUtils.clearAndType(driver, amountInput, amount);
        return this;
    }

    @Step("Submit withdrawal")
    public CustomerDashboardPage submitWithdrawal() {
        SeleniumUtils.waitAndClick(driver, withdrawSubmitButton);
        return this;
    }

    @Step("Withdraw amount: {amount}")
    public CustomerDashboardPage withdraw(String amount) {
        ensureOnAccountPage();
        clickWithdrawButton().enterWithdrawalAmount(amount).submitWithdrawal();
        return this;
    }

    @Step("Click Transactions (navigate to #/listTx)")
    public CustomerDashboardPage clickTransactionsButton() {
        SeleniumUtils.waitAndClick(driver, transactionsButton);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.CUSTOMER_TRANSACTIONS);
        return this;
    }



    @Step("Get account balance")
    public String getAccountBalance() {
        try {
            SeleniumUtils.waitUntilVisible(driver, accountBalance);
            return accountBalance.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Get account balance as integer")
    public int getBalanceAsInt() {
        try {
            String balance = getAccountBalance();
            return Integer.parseInt(balance);
        } catch (NumberFormatException e) {
            return 0;
        }
    }



    @Step("Get transaction history (#/listTx)")
    public List<String> getTransactionHistory() {
        try {
            List<WebElement> rows = driver.findElements(TRANSACTION_ROWS);
            return rows.stream().map(WebElement::getText).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Step("Verify transaction history is displayed")
    public boolean isTransactionHistoryDisplayed() {
        return SeleniumUtils.isElementDisplayed(driver, By.cssSelector("table"));
    }

    @Step("Get transaction count")
    public int getTransactionCount() {
        return driver.findElements(TRANSACTION_ROWS).size();
    }

    @Step("Click Logout")
    public CustomerDashboardPage logout() {
        SeleniumUtils.waitAndClick(driver, logoutButton);
        return this;
    }


    public boolean isTransactionsButtonVisible() {
        try {
            return transactionsButton.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public boolean isDepositButtonVisible() {
        try {
            return depositTabButton.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public boolean isWithdrawButtonVisible() {
        try {
            return withdrawTabButton.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }
}
