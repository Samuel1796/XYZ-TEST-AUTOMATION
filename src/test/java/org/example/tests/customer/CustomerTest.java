package org.example.tests.customer;

import io.qameta.allure.*;
import org.example.setup.BaseTest;
import org.example.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User Story 2: As a Customer, I want to view my transactions, deposit funds,
 * and withdraw money so that I can manage my finances effectively.
 */
@DisplayName("US2 – Customer Banking")
@Epic("XYZ Bank")
@Feature("Customer Banking")
@Tag("customer")
@Tag("us2")
public class CustomerTest extends BaseTest {

    private String testCustomerName;

    /** Creates a test customer with one Dollar account via manager flow; runs after BaseTest.setUp. */
    @BeforeEach
    void setUpCustomer() {
        createTestCustomer();
    }

    /** Adds a customer, creates a Dollar account for "FirstName LastName", then navigates home so tests can login as customer. */
    private void createTestCustomer() {
        TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
        testCustomerName = data.getDisplayName();

        loginPage.loginAsManager();
        managerPage.addCustomer(data.getName(), data.getPostalCode());
        managerPage.createAccount(testCustomerName, "Dollar");
        managerPage.clickHomeButton();
    }

    // ─── Account Access ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Account Access")
    @Story("Account Access")
    class AccountAccess {

        @Test
        @DisplayName("Verify customer without account cannot access transactions, deposit, or withdrawal")
        @Severity(SeverityLevel.CRITICAL)
        void customerWithoutAccount_cannotAccessBankingActions() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
            String displayName = data.getDisplayName();

            loginPage.loginAsManager();
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            managerPage.clickHomeButton();

            loginPage.loginAsCustomer(displayName);

            assertFalse(customerPage.isTransactionsButtonVisible(),
                    "Transactions button should not be visible for customer without account");
            assertFalse(customerPage.isDepositButtonVisible(),
                    "Deposit button should not be visible for customer without account");
            assertFalse(customerPage.isWithdrawButtonVisible(),
                    "Withdrawal button should not be visible for customer without account");
        }


        @Test
        @DisplayName("Verify deleted customer cannot access account")
        @Severity(SeverityLevel.CRITICAL)
        void deletedCustomer_cannotAccessAccount() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
            String displayName = data.getDisplayName();

            loginPage.loginAsManager();
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            managerPage.createAccount(displayName, "Dollar");
            managerPage.clickCustomersButton();
            managerPage.scrollToCustomerAndDelete(displayName);
            assertFalse(managerPage.customerExists(displayName),
                    "Customer should no longer exist in the table after deletion");

            managerPage.clickHomeButton();
            loginPage.selectCustomerUserType();
            assertFalse(loginPage.isCustomerInDropdown(displayName),
                    "Deleted customer should not appear in the customer dropdown");
        }
    }

    @Nested
    @DisplayName("Viewing Transactions")
    @Story("Viewing Transactions")
    class ViewingTransactions {

        @Test
        @DisplayName("Verify new account has empty transaction list")
        @Severity(SeverityLevel.NORMAL)
        void newAccount_emptyTransactionList() {
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.clickTransactionsButton();
            assertEquals(0, customerPage.getTransactionCount());
        }
    }

    @Nested
    @DisplayName("Depositing Funds")
    @Story("Depositing Funds")
    class DepositingFunds {

        @Test
        @DisplayName("Verify valid deposit updates balance correctly")
        @Severity(SeverityLevel.CRITICAL)
        void validDeposit_updatesBalance() {
            String amount = TestDataGenerator.generateValidDepositAmount();
            loginPage.loginAsCustomer(testCustomerName);
            int balanceBefore = customerPage.getBalanceAsInt();
            customerPage.deposit(amount);
            int balanceAfter = customerPage.getBalanceAsInt();
            assertEquals(balanceBefore + Integer.parseInt(amount), balanceAfter,
                    "Balance should increase by " + amount);
        }

        @Test
        @DisplayName("Verify zero deposit does not change balance")
        @Severity(SeverityLevel.NORMAL)
        void zeroDeposit_balanceUnchanged() {
            loginPage.loginAsCustomer(testCustomerName);
            int balanceBefore = customerPage.getBalanceAsInt();
            customerPage.deposit(TestDataGenerator.generateZeroAmount());
            int balanceAfter = customerPage.getBalanceAsInt();
            assertEquals(balanceBefore, balanceAfter,
                    "Balance should not change when depositing zero");
        }

        @Test
        @DisplayName("Verify negative deposit does not change balance")
        @Severity(SeverityLevel.NORMAL)
        void negativeDeposit_balanceUnchanged() {
            loginPage.loginAsCustomer(testCustomerName);
            int balanceBefore = customerPage.getBalanceAsInt();
            customerPage.deposit(TestDataGenerator.generateNegativeAmount());
            int balanceAfter = customerPage.getBalanceAsInt();
            assertEquals(balanceBefore, balanceAfter,
                    "Balance should not change when depositing a negative amount");
        }
    }

    @Nested
    @DisplayName("Withdrawing Money")
    @Story("Withdrawing Money")
    class WithdrawingMoney {

        @Test
        @DisplayName("Verify valid withdrawal with sufficient balance updates balance correctly")
        @Severity(SeverityLevel.CRITICAL)
        void validWithdrawal_updatesBalance() {
            String depositAmount = TestDataGenerator.generateLargeAmount();
            String withdrawAmount = TestDataGenerator.generateValidWithdrawalAmount();
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit(depositAmount);
            int balanceAfterDeposit = customerPage.getBalanceAsInt();
            customerPage.withdraw(withdrawAmount);
            int balanceAfterWithdrawal = customerPage.getBalanceAsInt();
            assertEquals(balanceAfterDeposit - Integer.parseInt(withdrawAmount), balanceAfterWithdrawal,
                    "Balance should decrease by " + withdrawAmount);
        }

        @Test
        @DisplayName("Verify withdrawal exceeding balance does not change balance")
        @Severity(SeverityLevel.NORMAL)
        void withdrawalExceedingBalance_balanceUnchanged() {
            loginPage.loginAsCustomer(testCustomerName);
            int balanceBefore = customerPage.getBalanceAsInt();
            customerPage.withdraw(TestDataGenerator.generateLargeAmount());
            int balanceAfter = customerPage.getBalanceAsInt();
            assertEquals(balanceBefore, balanceAfter,
                    "Balance should not change when withdrawing more than available");
        }

        @Test
        @DisplayName("Verify withdrawing exact deposited amount brings balance to zero")
        @Severity(SeverityLevel.NORMAL)
        void withdrawExactDeposit_balanceBecomesZero() {
            String amount = "500";
            loginPage.loginAsCustomer(testCustomerName);
            customerPage.deposit(amount);
            customerPage.withdraw(amount);
            int balanceAfter = customerPage.getBalanceAsInt();
            assertEquals(0, balanceAfter,
                    "Balance should be zero after withdrawing the same amount deposited");
        }
    }
}
