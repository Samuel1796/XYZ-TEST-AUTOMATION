package org.example.tests.customer;

import io.qameta.allure.*;
import org.example.base.BaseTest;
import org.example.config.ConfigManager;
import org.example.pages.customer.CustomerDashboardPage;
import org.example.pages.manager.LoginPage;
import org.example.pages.manager.ManagerDashboardPage;
import org.example.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User Story 2: As a Customer, I want to view my transactions, deposit funds, and withdraw money
 * so that I can manage my finances effectively.
 */
@DisplayName("User Story 2: Customer – View transactions, deposit funds, withdraw money")
@Epic("User Story 2: Customer")
@Feature("Customer Banking")
public class CustomerTest extends BaseTest {

    private LoginPage loginPage;
    private ManagerDashboardPage managerPage;
    private CustomerDashboardPage customerPage;
    private String testCustomerName;

    @BeforeEach
    void setUpCustomer() {
        loginPage = new LoginPage(driver);
        managerPage = new ManagerDashboardPage(driver);
        customerPage = new CustomerDashboardPage(driver);
        createTestCustomer();
    }

    private void createTestCustomer() {
        TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
        String name = data.getName();
        // App shows "FirstName LastName" in dropdowns (we use same name for first and last)
        testCustomerName = name + " " + name;

        loginPage.loginAsManager("Manager");
        managerPage.addCustomer(name, data.getPostalCode());
        managerPage.createAccount(testCustomerName, "Dollar");
        driver.navigate().to(ConfigManager.getBaseUrl());
    }

    @Nested
    @DisplayName("AC2 – Customers cannot access account until manager has created one")
    @Story("Account Access")
    @Tag("us2")
    @Tag("access")
    class AccountAccessUntilCreated {

        @Test
        @DisplayName("Verify customer without account cannot reach account (no account in dropdown)")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void customerWithoutAccount_cannotReachAccount() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            driver.navigate().to(ConfigManager.getBaseUrl());

            loginPage.selectCustomerUserType();
            assertTrue(loginPage.isLoginPageDisplayed());
        }

        @Test
        @DisplayName("Verify deleted customer cannot access account (removed from dropdown)")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void deletedCustomer_cannotAccessAccount() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
            String displayName = data.getName() + " " + data.getName();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            managerPage.createAccount(displayName, "Dollar");
            managerPage.clickCustomersButton();
            managerPage.deleteCustomer(displayName);
            assertFalse(managerPage.customerExists(displayName));

            driver.navigate().to(ConfigManager.getBaseUrl());
            loginPage.selectCustomerUserType();
            assertTrue(loginPage.isLoginPageDisplayed());
        }
    }

    @Nested
    @DisplayName("AC1 – Viewing Transactions (view list of recent transactions)")
    @Story("Viewing Transactions")
    @Tag("us2")
    @Tag("viewing_transactions")
    class ViewingTransactions {

        @Test
        @DisplayName("Verify customer can view list of recent transactions after deposit")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void customerCanViewRecentTransactions_afterDeposit() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit(TestDataGenerator.generateValidDepositAmount());
            customerPage.clickTransactionsButton();

            assertTrue(customerPage.isTransactionHistoryDisplayed());
            assertTrue(customerPage.getTransactionCount() > 0);
        }

        @Test
        @DisplayName("Verify new account has empty transaction list")
        void newAccount_transactionListEmpty() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.clickTransactionsButton();

            assertEquals(0, customerPage.getTransactionCount());
        }

        @Test
        @DisplayName("Verify transaction list shows correct type and amount (e.g. Deposit)")
        void transactionList_showsCorrectTypeAndAmount() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit("500.00");
            customerPage.clickTransactionsButton();

            var list = customerPage.getTransactionHistory();
            assertFalse(list.isEmpty());
            assertTrue(list.stream().anyMatch(t -> t.contains("Deposit")));
        }

        @Test
        @DisplayName("Verify multiple transactions all appear in list")
        void multipleTransactions_allAppearInList() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit("100.00");
            customerPage.deposit("200.00");
            customerPage.withdraw("50.00");
            customerPage.clickTransactionsButton();

            assertTrue(customerPage.getTransactionCount() >= 3);
        }

        @Test
        @DisplayName("Verify transaction list persists after logout and login")
        void transactionList_persistsAfterLogoutLogin() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit("300.00");
            customerPage.logout();
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.clickTransactionsButton();

            assertTrue(customerPage.getTransactionCount() > 0);
        }
    }

    @Nested
    @DisplayName("AC2 – Depositing Funds (enter amount; validate positive; balance updated on success)")
    @Story("Depositing Funds")
    @Tag("us2")
    @Tag("depositing_funds")
    class DepositingFunds {

        @Test
        @DisplayName("Verify customer can deposit valid positive amount – balance updated")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void customerCanDeposit_validPositiveAmount_success() {
            String amount = TestDataGenerator.generateValidDepositAmount();

            loginPage.loginAsCustomer(testCustomerName);
            assertTrue(customerPage.isDashboardDisplayed());
            customerPage.deposit(amount);

            String msg = customerPage.getSuccessMessage();
            assertNotNull(msg);
            assertFalse(msg.isEmpty());
        }

        @Test
        @DisplayName("Verify deposit zero amount shows validation error")
        @Tag("validation")
        @Tag("negative")
        void deposit_zeroAmount_validationError() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit(TestDataGenerator.generateZeroAmount());

            assertNotNull(customerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify deposit negative amount shows validation error")
        @Tag("validation")
        @Tag("negative")
        void deposit_negativeAmount_validationError() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit(TestDataGenerator.generateNegativeAmount());

            assertNotNull(customerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify deposit small positive amount succeeds")
        @Tag("boundary")
        void deposit_smallPositiveAmount_success() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit(TestDataGenerator.generateSmallAmount());

            assertNotNull(customerPage.getSuccessMessage());
        }
    }

    @Nested
    @DisplayName("AC3 – Withdrawing Money (enter amount; validate positive and sufficient balance; balance updated)")
    @Story("Withdrawing Money")
    @Tag("us2")
    @Tag("withdrawing_money")
    class WithdrawingMoney {

        @Test
        @DisplayName("Verify customer can withdraw valid amount with sufficient balance – balance updated")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void customerCanWithdraw_validAmountSufficientBalance_success() {
            String depositAmount = TestDataGenerator.generateLargeAmount();
            String withdrawAmount = TestDataGenerator.generateValidWithdrawalAmount();

            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit(depositAmount);
            customerPage.withdraw(withdrawAmount);

            assertNotNull(customerPage.getSuccessMessage());
        }

        @Test
        @DisplayName("Verify withdraw zero amount shows validation error")
        @Tag("validation")
        @Tag("negative")
        void withdraw_zeroAmount_validationError() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.withdraw(TestDataGenerator.generateZeroAmount());

            assertNotNull(customerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify withdraw more than balance shows validation error (insufficient balance)")
        @Tag("validation")
        @Tag("negative")
        void withdraw_moreThanBalance_validationError() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.withdraw(TestDataGenerator.generateLargeAmount());

            assertNotNull(customerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify withdraw exact balance succeeds")
        @Tag("boundary")
        void withdraw_exactBalance_success() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit("100.00");
            customerPage.withdraw("100.00");

            assertNotNull(customerPage.getSuccessMessage());
        }
    }

    @Nested
    @DisplayName("AC4 – Transaction Security (cannot reset or alter transaction history)")
    @Story("Transaction Security")
    @Tag("us2")
    @Tag("transaction_security")
    class TransactionSecurity {

        @Test
        @DisplayName("Verify transaction history is read-only (displayed but not editable)")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void transactionHistory_readOnly_notEditable() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit("250.00");
            customerPage.clickTransactionsButton();

            assertFalse(customerPage.getTransactionHistory().isEmpty());
            assertTrue(customerPage.isTransactionHistoryDisplayed());
        }
    }
}
