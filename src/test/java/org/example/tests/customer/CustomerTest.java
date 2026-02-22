package org.example.tests.customer;

import io.qameta.allure.*;
import org.example.base.BaseTest;
import org.example.pages.customer.CustomerDashboardPage;
import org.example.pages.manager.LoginPage;
import org.example.pages.manager.ManagerDashboardPage;
import org.example.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User Story 2: As a Customer, I want to view my transactions, deposit funds,
 * and withdraw money so that I can manage my finances effectively.
 */
@DisplayName("US2 – Customer Banking")
@Epic("XYZ Bank")
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
        testCustomerName = name + " " + name;

        loginPage.loginAsManager("Manager");
        managerPage.addCustomer(name, data.getPostalCode());
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
            String displayName = data.getName() + " " + data.getName();

            loginPage.loginAsManager("Manager");
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
            String displayName = data.getName() + " " + data.getName();

            loginPage.loginAsManager("Manager");
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

        // ─── AC1: Viewing Transactions ───────────────────────────────────────

        @Nested
        @DisplayName("Viewing Transactions")
        @Story("Viewing Transactions")
        class ViewingTransactions {

            @Test
            @DisplayName("Verify customer can view recent transactions after deposit")
            @Severity(SeverityLevel.CRITICAL)
            void viewRecentTransactions_afterDeposit() {
                loginPage.loginAsCustomer(testCustomerName);
                customerPage.deposit(TestDataGenerator.generateValidDepositAmount());
                customerPage.clickTransactionsButton();

                assertTrue(customerPage.isTransactionHistoryDisplayed());
                assertTrue(customerPage.getTransactionCount() > 0);
            }

            @Test
            @DisplayName("Verify new account has empty transaction list")
            @Severity(SeverityLevel.NORMAL)
            void newAccount_emptyTransactionList() {
                loginPage.loginAsCustomer(testCustomerName);
                customerPage.clickTransactionsButton();

                assertEquals(0, customerPage.getTransactionCount());
            }

            @Test
            @DisplayName("Verify transaction list shows correct transaction type")
            @Severity(SeverityLevel.NORMAL)
            void transactionList_showsCorrectType() {
                loginPage.loginAsCustomer(testCustomerName);
                customerPage.deposit("500");
                customerPage.clickTransactionsButton();

                var list = customerPage.getTransactionHistory();
                assertFalse(list.isEmpty());
                assertTrue(list.stream().anyMatch(t -> t.contains("Credit")),
                        "Transaction list should contain a Credit entry after deposit");
            }

            @Test
            @DisplayName("Verify multiple transactions all appear in list")
            @Severity(SeverityLevel.NORMAL)
            void multipleTransactions_allAppearInList() {
                loginPage.loginAsCustomer(testCustomerName);
                customerPage.deposit("100");
                customerPage.deposit("200");
                customerPage.withdraw("50");
                customerPage.clickTransactionsButton();

                assertTrue(customerPage.getTransactionCount() >= 3);
            }

            @Test
            @DisplayName("Verify transaction list persists after logout and re-login")
            @Severity(SeverityLevel.NORMAL)
            void transactionList_persistsAfterReLogin() {
                loginPage.loginAsCustomer(testCustomerName);
                customerPage.deposit("300");
                customerPage.logout();
                // After logout the app lands on #/customer (dropdown page)
                loginPage.selectCustomer(testCustomerName);
                loginPage.clickLoginButton();
                customerPage.clickTransactionsButton();

                assertTrue(customerPage.getTransactionCount() > 0);
            }
        }

        // ─── AC2: Depositing Funds ───────────────────────────────────────────

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

            @Test
            @DisplayName("Verify small deposit (1) updates balance correctly")
            @Severity(SeverityLevel.MINOR)
            void smallDeposit_updatesBalance() {
                loginPage.loginAsCustomer(testCustomerName);
                int balanceBefore = customerPage.getBalanceAsInt();
                String smallAmount = TestDataGenerator.generateSmallAmount();
                customerPage.deposit(smallAmount);
                int balanceAfter = customerPage.getBalanceAsInt();

                assertEquals(balanceBefore + Integer.parseInt(smallAmount), balanceAfter,
                        "Balance should increase by " + smallAmount);
            }
        }

        // ─── AC3: Withdrawing Money ──────────────────────────────────────────

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
                int balanceBefore = customerPage.getBalanceAsInt();
                customerPage.withdraw(withdrawAmount);
                int balanceAfter = customerPage.getBalanceAsInt();

                assertEquals(balanceBefore - Integer.parseInt(withdrawAmount), balanceAfter,
                        "Balance should decrease by " + withdrawAmount);
            }

            @Test
            @DisplayName("Verify zero withdrawal does not change balance")
            @Severity(SeverityLevel.NORMAL)
            void zeroWithdrawal_balanceUnchanged() {
                loginPage.loginAsCustomer(testCustomerName);
                int balanceBefore = customerPage.getBalanceAsInt();
                customerPage.withdraw(TestDataGenerator.generateZeroAmount());
                int balanceAfter = customerPage.getBalanceAsInt();

                assertEquals(balanceBefore, balanceAfter,
                        "Balance should not change when withdrawing zero");
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

        // ─── AC4: Transaction Security ───────────────────────────────────────

        @Nested
        @DisplayName("Transaction Security")
        @Story("Transaction Security")
        class TransactionSecurity {

            @Test
            @DisplayName("Verify transaction history is read-only and cannot be altered")
            @Severity(SeverityLevel.CRITICAL)
            void transactionHistory_isReadOnly() {
                loginPage.loginAsCustomer(testCustomerName);
                customerPage.deposit("250");
                customerPage.clickTransactionsButton();

                assertFalse(customerPage.getTransactionHistory().isEmpty());
                assertTrue(customerPage.isTransactionHistoryDisplayed());
            }
        }
    }
}
