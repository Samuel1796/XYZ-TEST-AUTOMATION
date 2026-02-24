package org.example.pages.customer;

import io.qameta.allure.Step;
import org.example.config.ConfigManager;
import org.example.config.AppUrls;
import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Page object for the customer account flow. Account view (#/account) shows balance and tab buttons for Deposit,
 * Withdraw, and Transactions. Deposit and Withdraw share the same URL; only the visible form changes when the tab
 * is clicked (both have an amount input; we wait for the correct form before typing). Transactions view (#/listTx)
 * shows the transaction history table. This class also provides waits for balance and transaction count to avoid
 * flaky assertions after async updates.
 */
public class CustomerDashboardPage {

    private final WebDriver driver;

    // --- #/account – tab buttons ---
    @FindBy(xpath = "//button[contains(@ng-click,'deposit')]")
    private WebElement depositTabButton;

    @FindBy(xpath = "//button[normalize-space(text())=\"Withdrawl\"]")
    private WebElement withdrawTabButton;

    @FindBy(xpath = "//button[contains(text(),'Transactions')]")
    private WebElement transactionsButton;

    @FindBy(xpath = "//button[contains(text(),'Logout')]")
    private WebElement logoutButton;

    // --- #/account – form shown after clicking Deposit or Withdraw tab ---
    @FindBy(css = "input[ng-model='amount']")
    private WebElement amountInput;

    @FindBy(xpath = "//button[text()='Deposit']")
    private WebElement depositSubmitButton;

    @FindBy(xpath = "//button[text()='Withdraw']")
    private WebElement withdrawSubmitButton;

    // --- #/account – account info ---
    @FindBy(xpath = "//div/strong[2]")
    private WebElement accountBalance;

    /** By locator for transaction table (wait for page ready before counting rows) */
    private static final By TRANSACTION_TABLE = By.cssSelector("table.table");
    /** By locator for transaction rows */
    private static final By TRANSACTION_ROWS = By.cssSelector("table tbody tr");

    /**
     * Creates the page object and initializes PageFactory elements.
     *
     * @param driver the WebDriver instance (expected on #/account or #/listTx)
     */
    public CustomerDashboardPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    /** Waits until the current URL contains #/account. Use before deposit/withdraw/balance actions. */
    public CustomerDashboardPage ensureOnAccountPage() {
        SeleniumUtils.waitForUrlContains(driver, AppUrls.CUSTOMER_ACCOUNT);
        return this;
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

    /** By for amount input. Deposit and Withdraw forms each have one; only the active form’s input is visible. */
    private static final By AMOUNT_INPUT = By.cssSelector("input[ng-model='amount']");
    private static final By WITHDRAW_SUBMIT_BUTTON = By.xpath("//button[text()='Withdraw']");

    @Step("Click Withdraw tab (form appears on same #/account)")
    public CustomerDashboardPage clickWithdrawButton() {
        SeleniumUtils.waitAndClick(driver, withdrawTabButton);
        // Wait for Withdraw form to be shown so the visible amount input is the withdraw one
        SeleniumUtils.waitForElementToBeVisible(driver, WITHDRAW_SUBMIT_BUTTON);
        SeleniumUtils.waitForFirstVisible(driver, AMOUNT_INPUT);
        return this;
    }

    @Step("Enter withdrawal amount: {amount}")
    public CustomerDashboardPage enterWithdrawalAmount(String amount) {
        // Find and type in one flow to avoid stale element after Angular re-render on Withdraw tab
        SeleniumUtils.waitFirstVisibleThenClearAndType(driver, AMOUNT_INPUT, amount);
        // Let the UI update balance before we read it
        SeleniumUtils.waitForElementToBeVisible(driver, By.xpath("//div/strong[2]"));
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



    /** Returns the displayed balance text (e.g. "500"); empty string if element not found. */
    @Step("Get account balance")
    public String getAccountBalance() {
        try {
            SeleniumUtils.waitUntilVisible(driver, accountBalance);
            return accountBalance.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** Parses displayed balance to int; returns 0 on parse error or missing element. */
    @Step("Get account balance as integer")
    public int getBalanceAsInt() {
        try {
            String balance = getAccountBalance();
            return Integer.parseInt(balance);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Waits until the displayed balance equals the expected value (e.g. after deposit/withdraw).
     * Use before navigating to Transactions so the app has applied the last transaction.
     */
    @Step("Wait for balance to equal {expected}")
    public CustomerDashboardPage waitForBalanceEqualTo(int expected) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        wait.until(d -> getBalanceAsInt() == expected);
        return this;
    }



    /** Returns text of each transaction row on #/listTx; empty list if table missing or error. */
    @Step("Get transaction history (#/listTx)")
    public List<String> getTransactionHistory() {
        try {
            waitForTransactionsPageReady();
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

    /** Number of rows in the transaction table after waiting for the table to be present. */
    @Step("Get transaction count")
    public int getTransactionCount() {
        waitForTransactionsPageReady();
        return driver.findElements(TRANSACTION_ROWS).size();
    }

    /** Waits for transactions view to be ready (table present). Avoids long wait when list is empty. */
    private void waitForTransactionsPageReady() {
        SeleniumUtils.waitForElementToBePresent(driver, TRANSACTION_TABLE);
    }

    /**
     * Waits until the transaction list has at least the given number of rows (Angular may render after the table is in DOM).
     * Use this after navigating to Transactions to avoid flaky "empty table" assertions.
     */
    @Step("Wait for transaction list to have at least {minCount} row(s)")
    public CustomerDashboardPage waitForTransactionCountAtLeast(int minCount) {
        waitForTransactionsPageReady();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        wait.until(d -> d.findElements(TRANSACTION_ROWS).size() >= minCount);
        return this;
    }

    @Step("Click Logout")
    public CustomerDashboardPage logout() {
        SeleniumUtils.waitAndClick(driver, logoutButton);
        return this;
    }


    /** Returns true if the Transactions button is displayed (customer must have an account). */
    public boolean isTransactionsButtonVisible() {
        try {
            return transactionsButton.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    /** Returns true if the Deposit tab button is displayed. */
    public boolean isDepositButtonVisible() {
        try {
            return depositTabButton.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    /** Returns true if the Withdraw tab button is displayed. */
    public boolean isWithdrawButtonVisible() {
        try {
            return withdrawTabButton.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }
}
