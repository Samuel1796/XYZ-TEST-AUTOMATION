package org.example.pages.customer;

import io.qameta.allure.Step;
import org.example.config.AppUrls;
import org.example.utils.SeleniumUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer account page (#/account). Deposit and Withdraw use the same URL; form and button change when Deposit or Withdraw is clicked.
 * Transactions (#/listTx) – separate view.
 */
public class CustomerDashboardPage {

    private final WebDriver driver;

    // --- #/account – main buttons ---
    private static final By DEPOSIT_BUTTON = By.xpath("//button[contains(text(),'Deposit')]");
    private static final By WITHDRAW_BUTTON = By.xpath("//button[contains(text(),'Withdraw')]");
    private static final By TRANSACTIONS_BUTTON = By.xpath("//button[contains(text(),'Transactions')]");
    private static final By LOGOUT_BUTTON = By.xpath("//button[contains(text(),'Logout')]");

    // --- #/account – form shown after clicking Deposit or Withdraw (same input, same Submit) ---
    private static final By AMOUNT_INPUT = By.cssSelector("input[ng-model='amount']");
    /** Single Submit button for both Deposit and Withdraw form */
    private static final By FORM_SUBMIT_BUTTON = By.xpath("//button[contains(text(),'Submit')]");

    // --- #/account – account info ---
    private static final By ACCOUNT_NUMBER = By.xpath("//div[contains(.,'Account Number')]//span[@class='ng-binding']");
    private static final By ACCOUNT_BALANCE = By.xpath("//div[contains(.,'Balance')]//span[@class='ng-binding']");
    private static final By DASHBOARD_TITLE = By.xpath("//strong[contains(text(),'Account')]");

    // --- Messages on #/account ---
    private static final By SUCCESS_MESSAGE = By.cssSelector(".alert-success");
    private static final By ERROR_MESSAGE = By.cssSelector(".alert-danger, .error");

    // --- #/listTx (Transactions) ---
    private static final By TRANSACTION_ROWS = By.cssSelector("table tbody tr");

    public CustomerDashboardPage(WebDriver driver) {
        this.driver = driver;
    }

    /** Ensure we are on customer account page. */
    public CustomerDashboardPage ensureOnAccountPage() {
        SeleniumUtils.waitForUrlContains(driver, AppUrls.CUSTOMER_ACCOUNT);
        return this;
    }

    @Step("Verify customer account page is displayed (#/account)")
    public boolean isDashboardDisplayed() {
        return SeleniumUtils.isElementDisplayed(driver, DASHBOARD_TITLE);
    }

    @Step("Click Deposit (form appears on same #/account)")
    public CustomerDashboardPage clickDepositButton() {
        SeleniumUtils.click(driver, DEPOSIT_BUTTON);
        WaitUtils.smallWait(); // form updates
        SeleniumUtils.waitForElementToBeVisible(driver, AMOUNT_INPUT);
        return this;
    }

    @Step("Enter deposit amount: {amount}")
    public CustomerDashboardPage enterDepositAmount(String amount) {
        SeleniumUtils.sendKeys(driver, AMOUNT_INPUT, amount);
        return this;
    }

    @Step("Submit deposit")
    public CustomerDashboardPage submitDeposit() {
        SeleniumUtils.click(driver, FORM_SUBMIT_BUTTON);
        return this;
    }

    @Step("Deposit amount: {amount}")
    public CustomerDashboardPage deposit(String amount) {
        ensureOnAccountPage();
        clickDepositButton().enterDepositAmount(amount).submitDeposit();
        return this;
    }

    @Step("Click Withdraw (form appears on same #/account)")
    public CustomerDashboardPage clickWithdrawButton() {
        SeleniumUtils.click(driver, WITHDRAW_BUTTON);
        WaitUtils.smallWait(); // form updates
        SeleniumUtils.waitForElementToBeVisible(driver, AMOUNT_INPUT);
        return this;
    }

    @Step("Enter withdrawal amount: {amount}")
    public CustomerDashboardPage enterWithdrawalAmount(String amount) {
        SeleniumUtils.sendKeys(driver, AMOUNT_INPUT, amount);
        return this;
    }

    @Step("Submit withdrawal")
    public CustomerDashboardPage submitWithdrawal() {
        SeleniumUtils.click(driver, FORM_SUBMIT_BUTTON);
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
        SeleniumUtils.click(driver, TRANSACTIONS_BUTTON);
        SeleniumUtils.waitForUrlContains(driver, AppUrls.CUSTOMER_TRANSACTIONS);
        return this;
    }

    @Step("Get account number")
    public String getAccountNumber() {
        try {
            return SeleniumUtils.getText(driver, ACCOUNT_NUMBER);
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Get account balance")
    public String getAccountBalance() {
        try {
            return SeleniumUtils.getText(driver, ACCOUNT_BALANCE);
        } catch (Exception e) {
            return "";
        }
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
        SeleniumUtils.click(driver, LOGOUT_BUTTON);
        return this;
    }
}
