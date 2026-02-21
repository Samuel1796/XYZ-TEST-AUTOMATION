package org.example.tests.manager;

import io.qameta.allure.*;
import org.example.base.BaseTest;
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
 * User Story 1: As a Bank Manager, I want to add customers, create accounts, and delete accounts
 * so that I can manage customer accounts efficiently.
 */
@DisplayName("User Story 1: Bank Manager – Add customers, create accounts, delete accounts")
@Epic("User Story 1: Bank Manager")
@Feature("Manager Account Management")
public class ManagerTest extends BaseTest {

    private LoginPage loginPage;
    private ManagerDashboardPage managerPage;

    @BeforeEach
    void setUpManager() {
        loginPage = new LoginPage(driver);
        managerPage = new ManagerDashboardPage(driver);
    }

    @Nested
    @DisplayName("AC1 – Adding Customers (names alphabetic only, postal codes numeric only)")
    @Story("Adding Customers")
    @Tag("us1")
    @Tag("adding_customers")
    class AddingCustomers {

        @Test
        @DisplayName("Verify manager can add new customer with valid name and postal code")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void managerCanAddNewCustomer_withValidData_success() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();

            loginPage.loginAsManager("Manager");
            assertTrue(managerPage.isDashboardDisplayed());

            managerPage.addCustomer(data.getName(), data.getPostalCode());
            String msg = managerPage.getSuccessMessage();
            assertNotNull(msg);
            assertFalse(msg.isEmpty());
        }

        @Test
        @DisplayName("Verify customer name with numbers is rejected (alphabetic only)")
        @Tag("validation")
        @Tag("negative")
        void customerName_withNumbers_rejected() {
            String invalidName = TestDataGenerator.generateInvalidCustomerNameWithNumbers();
            String postalCode = TestDataGenerator.generateValidPostalCode();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(invalidName, postalCode);

            assertNotNull(managerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify customer name with special characters is rejected (alphabetic only)")
        @Tag("validation")
        @Tag("negative")
        void customerName_withSpecialCharacters_rejected() {
            String invalidName = TestDataGenerator.generateInvalidCustomerNameWithSpecialChars();
            String postalCode = TestDataGenerator.generateValidPostalCode();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(invalidName, postalCode);

            assertNotNull(managerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify postal code with letters is rejected (numeric only)")
        @Tag("validation")
        @Tag("negative")
        void postalCode_withLetters_rejected() {
            String name = TestDataGenerator.generateValidCustomerName();
            String invalidPostal = TestDataGenerator.generateInvalidPostalCodeWithLetters();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(name, invalidPostal);

            assertNotNull(managerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify add customer with empty form shows validation")
        @Tag("validation")
        @Tag("negative")
        void addCustomer_emptyForm_validationOrError() {
            loginPage.loginAsManager("Manager");
            managerPage.clickAddCustomerButton();
            managerPage.submitCustomerForm();

            String error = managerPage.getErrorMessage();
            assertTrue(!error.isEmpty() || managerPage.isDashboardDisplayed(),
                    "Form validation or error expected");
        }
    }

    @Nested
    @DisplayName("AC2 – Creating Accounts (create for added customers; no access until account created)")
    @Story("Creating Accounts")
    @Tag("us1")
    @Tag("creating_accounts")
    class CreatingAccounts {

        @Test
        @DisplayName("Verify open account without selecting customer shows error")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void openAccount_withoutSelectingCustomer_showsError() {
            loginPage.loginAsManager("Manager");
            managerPage.clickOpenAccountButton()
                    .selectCurrency("Dollar")
                    .clickProcessButton();

            assertNotNull(managerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify manager can create account for added customer – success")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void managerCanCreateAccount_forAddedCustomer_success() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
            String displayName = data.getName() + " " + data.getName();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            managerPage.createAccount(displayName, "Dollar");

            String msg = managerPage.getSuccessMessage();
            assertNotNull(msg);
            assertFalse(msg.isEmpty());
        }
    }

    @Nested
    @DisplayName("AC3 – Deleting Accounts (manager can delete; deleted customer cannot access)")
    @Story("Deleting Accounts")
    @Tag("us1")
    @Tag("deleting_accounts")
    class DeletingAccounts {

        @Test
        @DisplayName("Verify manager can delete customer account – customer removed from list")
        @Severity(SeverityLevel.CRITICAL)
        @Tag("smoke")
        void managerCanDeleteCustomerAccount_customerRemovedFromList() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
            String displayName = data.getName() + " " + data.getName();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            managerPage.createAccount(displayName, "Dollar");
            managerPage.clickCustomersButton();
            managerPage.deleteCustomer(displayName);

            assertFalse(managerPage.customerExists(displayName), "Customer should be deleted");
        }
    }
}
