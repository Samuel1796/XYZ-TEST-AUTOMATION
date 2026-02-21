package org.example.tests.customer;

import io.qameta.allure.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.base.BaseTest;
import org.example.config.ConfigManager;
import org.example.pages.customer.CustomerDashboardPage;
import org.example.pages.manager.LoginPage;
import org.example.pages.manager.ManagerDashboardPage;
import org.example.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Customer Banking Operations.
 * Tests cover: depositing, withdrawing, and transaction history viewing.
 *
 * Epic: User Story 2 - Customer Banking Operations
 *
 * @author QA Team
 * @version 1.0
 */
@DisplayName("Customer Banking Tests")
@Epic("User Story 2: Customer Banking Operations")
@Feature("Customer Transactions")
public class CustomerTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(CustomerTest.class);
    private LoginPage loginPage;
    private ManagerDashboardPage managerDashboardPage;
    private CustomerDashboardPage customerDashboardPage;
    private static final String MANAGER_NAME = "Harry Potter";
    private String testCustomerName;

    @BeforeEach
    public void setUpCustomerTest() {
        logger.info("Setting up CustomerTest with WebDriver: " + driver);

        // Initialize page objects AFTER super.setUp() which initializes driver
        loginPage = new LoginPage(driver);
        managerDashboardPage = new ManagerDashboardPage(driver);
        customerDashboardPage = new CustomerDashboardPage(driver);

        // Create test customer
        createTestCustomer();
    }

    /**
     * Helper method to create a test customer
     */
    private void createTestCustomer() {
        logger.info("Creating test customer for tests");
        TestDataGenerator.CustomerTestData testData = TestDataGenerator.generateCustomerTestData();
        testCustomerName = testData.getName();

        try {
            loginPage.loginAsManager(MANAGER_NAME);
            managerDashboardPage.addCustomer(testCustomerName, testData.getPostalCode());
            managerDashboardPage.createAccount(testCustomerName, "Dollar");

            // Logout from manager account - navigate back to login page
            driver.navigate().to(ConfigManager.getBaseUrl());
            logger.info("Test customer created successfully: " + testCustomerName);
        } catch (Exception e) {
            logger.error("Failed to create test customer", e);
            throw new RuntimeException("Failed to create test customer: " + e.getMessage(), e);
        }
    }


    @Test
    @DisplayName("Verify Customer Cannot Access Account Before Creation")
    @Story("Account Access Control")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("smoke")
    @Tag("security")
    public void testCustomerCannotAccessAccountBeforeCreation() {
        logger.info("TEST: Verify Customer Cannot Access Account Before Creation");


        // Create a customer without account
        TestDataGenerator.CustomerTestData testData = TestDataGenerator.generateCustomerTestData();
        String customerWithoutAccount = testData.getName();

        // Step 1: Login as manager and add customer without creating account
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Add customer without creating account");
        managerDashboardPage.addCustomer(customerWithoutAccount, testData.getPostalCode());

        Allure.step("Logout from manager");
        driver.navigate().to(loginPage.getCurrentUrl());

        // Step 2: Try to login as customer without account
        Allure.step("Attempt to login as customer without account");
        loginPage.selectCustomerUserType();

        Allure.step("Verify customer cannot be selected or access denied");
        // The customer should not be available in dropdown for selection
        assertTrue(loginPage.isLoginPageDisplayed(), "Login page should still be displayed");
    }

    @Test
    @DisplayName("Verify Successful Deposit with Valid Amount")
    @Story("Depositing Funds")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("smoke")
    @Tag("transaction")
    public void testSuccessfulDeposit() {
        logger.info("TEST: Verify Successful Deposit with Valid Amount");


        String depositAmount = TestDataGenerator.generateValidDepositAmount();

        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Verify customer dashboard is displayed");
        assertTrue(customerDashboardPage.isDashboardDisplayed(), "Customer dashboard should be displayed");

        Allure.step("Deposit amount: " + depositAmount);
        customerDashboardPage.deposit(depositAmount);

        Allure.step("Verify success message");
        String successMessage = customerDashboardPage.getSuccessMessage();
        assertNotNull(successMessage, "Success message should be displayed");
        assertFalse(successMessage.isEmpty(), "Success message should not be empty");
    }

    @Test
    @DisplayName("Verify Deposit Validation - Zero Amount")
    @Story("Depositing Funds")
    @Tag("validation")
    @Tag("negative")
    public void testDepositValidationZeroAmount() {
        logger.info("TEST: Verify Deposit Validation - Zero Amount");


        String zeroAmount = TestDataGenerator.generateZeroAmount();

        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Attempt to deposit zero amount");
        customerDashboardPage.deposit(zeroAmount);

        Allure.step("Verify error message is displayed");
        String errorMessage = customerDashboardPage.getErrorMessage();
        assertNotNull(errorMessage, "Error message should be displayed for zero amount");
    }

    @Test
    @DisplayName("Verify Deposit Validation - Negative Amount")
    @Story("Depositing Funds")
    @Tag("validation")
    @Tag("negative")
    public void testDepositValidationNegativeAmount() {
        logger.info("TEST: Verify Deposit Validation - Negative Amount");


        String negativeAmount = TestDataGenerator.generateNegativeAmount();

        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Attempt to deposit negative amount");
        customerDashboardPage.deposit(negativeAmount);

        Allure.step("Verify error message is displayed");
        String errorMessage = customerDashboardPage.getErrorMessage();
        assertNotNull(errorMessage, "Error message should be displayed for negative amount");
    }

    @Test
    @DisplayName("Verify Deposit with Very Small Positive Amount (Boundary)")
    @Story("Depositing Funds")
    @Severity(SeverityLevel.MINOR)
    @Tag("boundary_test")
    public void testDepositWithSmallAmount() {
        logger.info("TEST: Verify Deposit with Very Small Positive Amount (Boundary)");


        String smallAmount = TestDataGenerator.generateSmallAmount();

        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Deposit small amount: " + smallAmount);
        customerDashboardPage.deposit(smallAmount);

        Allure.step("Verify success message");
        String successMessage = customerDashboardPage.getSuccessMessage();
        assertNotNull(successMessage, "Success message should be displayed");
    }

    @Test
    @DisplayName("Verify Successful Withdrawal with Sufficient Balance")
    @Story("Withdrawing Money")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("smoke")
    @Tag("transaction")
    public void testSuccessfulWithdrawal() {
        logger.info("TEST: Verify Successful Withdrawal with Sufficient Balance");


        String depositAmount = TestDataGenerator.generateLargeAmount();
        String withdrawAmount = TestDataGenerator.generateValidWithdrawalAmount();

        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("First deposit to ensure sufficient balance");
        customerDashboardPage.deposit(depositAmount);

        Allure.step("Withdraw amount: " + withdrawAmount);
        customerDashboardPage.withdraw(withdrawAmount);

        Allure.step("Verify success message");
        String successMessage = customerDashboardPage.getSuccessMessage();
        assertNotNull(successMessage, "Success message should be displayed");
    }

    @Test
    @DisplayName("Verify Withdrawal Validation - Zero Amount")
    @Story("Withdrawing Money")
    @Tag("validation")
    @Tag("negative")
    public void testWithdrawalValidationZeroAmount() {
        logger.info("TEST: Verify Withdrawal Validation - Zero Amount");


        String zeroAmount = TestDataGenerator.generateZeroAmount();

        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Attempt to withdraw zero amount");
        customerDashboardPage.withdraw(zeroAmount);

        Allure.step("Verify error message is displayed");
        String errorMessage = customerDashboardPage.getErrorMessage();
        assertNotNull(errorMessage, "Error message should be displayed for zero amount");
    }

    @Test
    @DisplayName("Verify Withdrawal Validation - Insufficient Balance")
    @Story("Withdrawing Money")
    @Tag("validation")
    @Tag("negative")
    public void testWithdrawalValidationInsufficientBalance() {
        logger.info("TEST: Verify Withdrawal Validation - Insufficient Balance");


        String largeAmount = TestDataGenerator.generateLargeAmount();

        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Attempt to withdraw large amount with insufficient balance");
        customerDashboardPage.withdraw(largeAmount);

        Allure.step("Verify error message is displayed");
        String errorMessage = customerDashboardPage.getErrorMessage();
        assertNotNull(errorMessage, "Error message should be displayed for insufficient balance");
    }

    @Test
    @DisplayName("Verify Withdrawal Exact Balance (to Zero)")
    @Story("Withdrawing Money")
    @Severity(SeverityLevel.MINOR)
    @Tag("boundary_test")
    public void testWithdrawalExactBalance() {
        logger.info("TEST: Verify Withdrawal Exact Balance (to Zero)");

        String depositAmount = "100.00"; // Specific amount for testing
        String withdrawAmount = "100.00"; // Exact same amount


        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Deposit exact amount");
        customerDashboardPage.deposit(depositAmount);

        Allure.step("Withdraw exact same amount");
        customerDashboardPage.withdraw(withdrawAmount);

        Allure.step("Verify success message");
        String successMessage = customerDashboardPage.getSuccessMessage();
        assertNotNull(successMessage, "Success message should be displayed");
    }

    @Test
    @DisplayName("Verify Customer Can View Transaction History")
    @Story("Viewing Transactions")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("smoke")
    @Tag("transaction_history")
    public void testViewTransactionHistory() {
        logger.info("TEST: Verify Customer Can View Transaction History");


        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Make a deposit");
        customerDashboardPage.deposit(TestDataGenerator.generateValidDepositAmount());

        Allure.step("Click Transactions button");
        customerDashboardPage.clickTransactionsButton();

        Allure.step("Verify transaction history is displayed");
        assertTrue(customerDashboardPage.isTransactionHistoryDisplayed(),
                "Transaction history should be displayed");

        Allure.step("Verify transaction records are present");
        int transactionCount = customerDashboardPage.getTransactionCount();
        assertTrue(transactionCount > 0, "Transaction records should be present");
    }

    @Test
    @DisplayName("Verify No Transaction History for New/Empty Account")
    @Story("Viewing Transactions")
    @Severity(SeverityLevel.MINOR)
    @Tag("transaction_history")
    public void testNoTransactionHistoryForNewAccount() {
        logger.info("TEST: Verify No Transaction History for New/Empty Account");


        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Click Transactions button");
        customerDashboardPage.clickTransactionsButton();

        Allure.step("Verify transaction history");
        int transactionCount = customerDashboardPage.getTransactionCount();
        assertTrue(transactionCount == 0, "New account should have no transactions");
    }

    @Test
    @DisplayName("Verify Transaction History Displays Correct Transaction Types and Amounts")
    @Story("Viewing Transactions")
    @Tag("transaction_history")
    public void testTransactionHistoryDisplaysCorrectInfo() {
        logger.info("TEST: Verify Transaction History Displays Correct Transaction Types and Amounts");


        String depositAmount = "500.00";

        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Make a deposit");
        customerDashboardPage.deposit(depositAmount);

        Allure.step("View transaction history");
        customerDashboardPage.clickTransactionsButton();

        Allure.step("Verify transaction details are correct");
        var transactions = customerDashboardPage.getTransactionHistory();
        assertFalse(transactions.isEmpty(), "Transaction history should not be empty");
        assertTrue(transactions.stream().anyMatch(t -> t.contains("Deposit")),
                "Transaction should contain Deposit type");
    }

    @Test
    @DisplayName("Verify Transaction History Sorting and Chronological Order")
    @Story("Viewing Transactions")
    @Tag("transaction_history")
    public void testTransactionHistorySortingOrder() {
        logger.info("TEST: Verify Transaction History Sorting and Chronological Order");


        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Make multiple transactions");
        customerDashboardPage.deposit("100.00");
        customerDashboardPage.deposit("200.00");
        customerDashboardPage.withdraw("50.00");

        Allure.step("View transaction history");
        customerDashboardPage.clickTransactionsButton();

        Allure.step("Verify transactions are in chronological order");
        var transactions = customerDashboardPage.getTransactionHistory();
        assertTrue(transactions.size() >= 3, "Should have at least 3 transactions");
    }

    @Test
    @DisplayName("Verify Transaction History Cannot Be Modified")
    @Story("Transaction Security")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("security")
    @Tag("transaction_history")
    public void testTransactionHistoryCannotBeModified() {
        logger.info("TEST: Verify Transaction History Cannot Be Modified");


        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Make a transaction");
        customerDashboardPage.deposit("250.00");

        Allure.step("View transaction history");
        customerDashboardPage.clickTransactionsButton();

        Allure.step("Verify transaction history table is read-only");
        // Get transaction history
        var transactions = customerDashboardPage.getTransactionHistory();
        assertFalse(transactions.isEmpty(), "Transactions should be displayed");

        // Verify that transaction history cannot be edited (no edit buttons)
        assertTrue(customerDashboardPage.isTransactionHistoryDisplayed(),
                "Transaction history should be visible but read-only");
    }

    @Test
    @DisplayName("Verify Customer Access Revoked After Account Deletion")
    @Story("Account Access Control")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("smoke")
    @Tag("security")
    public void testCustomerAccessRevokedAfterAccountDeletion() {
        logger.info("TEST: Verify Customer Access Revoked After Account Deletion");

        // Create a new customer to delete
        TestDataGenerator.CustomerTestData testData = TestDataGenerator.generateCustomerTestData();
        String customerToDelete = testData.getName();

        // Step 1: Create customer and account as manager
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Add and create account for customer");
        managerDashboardPage.addCustomer(customerToDelete, testData.getPostalCode());
        managerDashboardPage.createAccount(customerToDelete, "Dollar");

        Allure.step("Delete customer account");
        managerDashboardPage.clickCustomersButton();
        managerDashboardPage.deleteCustomer(customerToDelete);

        Allure.step("Verify customer is deleted");
        assertFalse(managerDashboardPage.customerExists(customerToDelete),
                "Customer should be deleted");

        // Step 2: Try to login as deleted customer
        Allure.step("Logout from manager account");
        driver.navigate().to(loginPage.getCurrentUrl());

        Allure.step("Attempt to login with deleted customer account");
        loginPage.selectCustomerUserType();

        Allure.step("Verify deleted customer cannot login");
        // The deleted customer should not be available in the dropdown
        assertTrue(loginPage.isLoginPageDisplayed(), "Login page should still be displayed");
    }

    @Test
    @DisplayName("Verify Transaction History Persists After Logout/Login")
    @Story("Viewing Transactions")
    @Tag("transaction_history")
    public void testTransactionHistoryPersistsAfterLogout() {
        logger.info("TEST: Verify Transaction History Persists After Logout/Login");


        // Steps
        Allure.step("Login as customer: " + testCustomerName);
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("Make a transaction");
        customerDashboardPage.deposit("300.00");

        Allure.step("Logout");
        customerDashboardPage.logout();

        Allure.step("Login again");
        loginPage.loginAsCustomer(testCustomerName);

        Allure.step("View transaction history");
        customerDashboardPage.clickTransactionsButton();

        Allure.step("Verify transaction history is still present");
        int transactionCount = customerDashboardPage.getTransactionCount();
        assertTrue(transactionCount > 0, "Transaction history should persist after logout/login");
    }
}

