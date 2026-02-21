package org.example.pages.customer;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for Customer Dashboard Page.
 * Contains action-based methods for customer operations:
 * - Depositing funds
 * - Withdrawing money
 * - Viewing transaction history
 *
 * @author QA Team
 * @version 1.0
 */
public class CustomerDashboardPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(CustomerDashboardPage.class);

    // Navigation Buttons
    private static final By DEPOSIT_BUTTON = By.xpath("//button[contains(text(), 'Deposit')]");
    private static final By WITHDRAW_BUTTON = By.xpath("//button[contains(text(), 'Withdraw')]");
    private static final By TRANSACTIONS_BUTTON = By.xpath("//button[contains(text(), 'Transactions')]");
    private static final By LOGOUT_BUTTON = By.xpath("//button[contains(text(), 'Logout')]");

    // Transaction Form Fields
    private static final By AMOUNT_INPUT = By.cssSelector("input[ng-model='amount']");
    private static final By SUBMIT_BUTTON = By.xpath("//button[contains(text(), 'Submit')]");

    // Account Information
    private static final By ACCOUNT_NUMBER = By.xpath("//strong[contains(text(), 'Account Number')]/parent::div/span");
    private static final By ACCOUNT_BALANCE = By.xpath("//strong[contains(text(), 'Balance')]/parent::div/span");

    // Success/Error Messages
    private static final By SUCCESS_MESSAGE = By.cssSelector(".alert-success");
    private static final By ERROR_MESSAGE = By.cssSelector(".alert-danger, .error");

    // Dashboard Elements
    private static final By DASHBOARD_TITLE = By.xpath("//h2[contains(text(), 'Customer')]");
    private static final By WELCOME_MESSAGE = By.cssSelector(".welcome");

    // Transaction History Table
    private static final By TRANSACTIONS_TABLE = By.cssSelector("table");
    private static final By TRANSACTION_ROWS = By.cssSelector("table tbody tr");

    /**
     * Constructor
     *
     * @param driver the WebDriver instance
     */
    public CustomerDashboardPage(WebDriver driver) {
        super(driver);
        logger.info("Customer Dashboard page initialized");
    }

    /**
     * Checks if customer dashboard is displayed
     *
     * @return true if dashboard is displayed
     */
    @Step("Verify customer dashboard is displayed")
    public boolean isDashboardDisplayed() {
        logger.info("Checking if customer dashboard is displayed");
        return isElementDisplayed(DASHBOARD_TITLE);
    }

    /**
     * Clicks on Deposit button
     *
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Click Deposit button")
    public CustomerDashboardPage clickDepositButton() {
        logger.info("Clicking Deposit button");
        click(DEPOSIT_BUTTON);
        return this;
    }

    /**
     * Enters deposit amount
     *
     * @param amount the amount to deposit
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Enter deposit amount: {amount}")
    public CustomerDashboardPage enterDepositAmount(String amount) {
        logger.info("Entering deposit amount: " + amount);
        sendText(AMOUNT_INPUT, amount);
        return this;
    }

    /**
     * Submits the deposit form
     *
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Submit deposit")
    public CustomerDashboardPage submitDeposit() {
        logger.info("Submitting deposit");
        click(SUBMIT_BUTTON);
        return this;
    }

    /**
     * Deposits amount with complete flow
     *
     * @param amount the amount to deposit
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Deposit amount: {amount}")
    public CustomerDashboardPage deposit(String amount) {
        logger.info("Depositing amount: " + amount);
        clickDepositButton()
                .enterDepositAmount(amount)
                .submitDeposit();
        return this;
    }

    /**
     * Clicks on Withdraw button
     *
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Click Withdraw button")
    public CustomerDashboardPage clickWithdrawButton() {
        logger.info("Clicking Withdraw button");
        click(WITHDRAW_BUTTON);
        return this;
    }

    /**
     * Enters withdrawal amount
     *
     * @param amount the amount to withdraw
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Enter withdrawal amount: {amount}")
    public CustomerDashboardPage enterWithdrawalAmount(String amount) {
        logger.info("Entering withdrawal amount: " + amount);
        sendText(AMOUNT_INPUT, amount);
        return this;
    }

    /**
     * Submits the withdrawal form
     *
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Submit withdrawal")
    public CustomerDashboardPage submitWithdrawal() {
        logger.info("Submitting withdrawal");
        click(SUBMIT_BUTTON);
        return this;
    }

    /**
     * Withdraws amount with complete flow
     *
     * @param amount the amount to withdraw
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Withdraw amount: {amount}")
    public CustomerDashboardPage withdraw(String amount) {
        logger.info("Withdrawing amount: " + amount);
        clickWithdrawButton()
                .enterWithdrawalAmount(amount)
                .submitWithdrawal();
        return this;
    }

    /**
     * Clicks on Transactions button to view transaction history
     *
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Click Transactions button")
    public CustomerDashboardPage clickTransactionsButton() {
        logger.info("Clicking Transactions button");
        click(TRANSACTIONS_BUTTON);
        return this;
    }

    /**
     * Gets account number
     *
     * @return account number
     */
    @Step("Get account number")
    public String getAccountNumber() {
        logger.info("Getting account number");
        try {
            return getText(ACCOUNT_NUMBER);
        } catch (Exception e) {
            logger.warn("Account number not found");
            return "";
        }
    }

    /**
     * Gets account balance
     *
     * @return account balance
     */
    @Step("Get account balance")
    public String getAccountBalance() {
        logger.info("Getting account balance");
        try {
            String balance = getText(ACCOUNT_BALANCE);
            logger.debug("Account balance: " + balance);
            return balance;
        } catch (Exception e) {
            logger.warn("Account balance not found");
            return "";
        }
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
     * Gets transaction history as list of strings
     *
     * @return list of transaction records
     */
    @Step("Get transaction history")
    public List<String> getTransactionHistory() {
        logger.info("Getting transaction history");
        try {
            List<WebElement> rows = driver.findElements(TRANSACTION_ROWS);
            return rows.stream()
                    .map(WebElement::getText)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Transaction history not found");
            return List.of();
        }
    }

    /**
     * Checks if transaction history is displayed
     *
     * @return true if transaction history is visible
     */
    @Step("Verify transaction history is displayed")
    public boolean isTransactionHistoryDisplayed() {
        logger.info("Checking if transaction history is displayed");
        try {
            return isElementDisplayed(TRANSACTIONS_TABLE);
        } catch (Exception e) {
            logger.debug("Transaction history not displayed");
            return false;
        }
    }

    /**
     * Gets number of transactions
     *
     * @return count of transaction records
     */
    @Step("Get transaction count")
    public int getTransactionCount() {
        logger.info("Getting transaction count");
        try {
            List<WebElement> rows = driver.findElements(TRANSACTION_ROWS);
            int count = rows.size();
            logger.debug("Transaction count: " + count);
            return count;
        } catch (Exception e) {
            logger.warn("Failed to get transaction count");
            return 0;
        }
    }

    /**
     * Clicks logout button
     *
     * @return CustomerDashboardPage instance for fluent API
     */
    @Step("Click logout button")
    public CustomerDashboardPage logout() {
        logger.info("Logging out");
        click(LOGOUT_BUTTON);
        return this;
    }

    /**
     * Checks if account is accessible
     *
     * @return true if account information is displayed
     */
    @Step("Verify account is accessible")
    public boolean isAccountAccessible() {
        logger.info("Checking if account is accessible");
        return isDashboardDisplayed();
    }
}

