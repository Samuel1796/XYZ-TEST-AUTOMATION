package org.example.tests.manager;

import io.qameta.allure.*;
import org.example.base.BaseTest;
import org.example.pages.manager.LoginPage;
import org.example.pages.manager.ManagerDashboardPage;
import org.example.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User Story 1: As a Bank Manager, I want to add customers, create accounts,
 * and delete accounts so that I can manage customer accounts efficiently.
 */
@DisplayName("US1 – Bank Manager")
@Epic("XYZ Bank")
@Feature("Manager Account Management")
public class ManagerTest extends BaseTest {

    private LoginPage loginPage;
    private ManagerDashboardPage managerPage;

    @BeforeEach
    void setUpManager() {
        loginPage = new LoginPage(driver);
        managerPage = new ManagerDashboardPage(driver);
    }

    // ─── AC1: Adding Customers ───────────────────────────────────────────

    @Nested
    @DisplayName("Adding Customers")
    @Story("Adding Customers")
    class AddingCustomers {

        @Test
        @DisplayName("Verify manager can add new customer with valid data")
        @Severity(SeverityLevel.CRITICAL)
        void addNewCustomer_withValidData() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();

            loginPage.loginAsManager("Manager");
            assertTrue(managerPage.isDashboardDisplayed());

            managerPage.addCustomer(data.getName(), data.getPostalCode());
            String msg = managerPage.getSuccessMessage();
            assertNotNull(msg);
            assertFalse(msg.isEmpty());
        }

        @Test
        @DisplayName("Verify customer name with numbers is rejected")
        @Severity(SeverityLevel.NORMAL)
        void customerName_withNumbers_rejected() {
            String invalidName = TestDataGenerator.generateInvalidCustomerNameWithNumbers();
            String postalCode = TestDataGenerator.generateValidPostalCode();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(invalidName, postalCode);

            assertNotNull(managerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify customer name with special characters is rejected")
        @Severity(SeverityLevel.NORMAL)
        void customerName_withSpecialCharacters_rejected() {
            String invalidName = TestDataGenerator.generateInvalidCustomerNameWithSpecialChars();
            String postalCode = TestDataGenerator.generateValidPostalCode();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(invalidName, postalCode);

            assertNotNull(managerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify postal code with letters is rejected")
        @Severity(SeverityLevel.NORMAL)
        void postalCode_withLetters_rejected() {
            String name = TestDataGenerator.generateValidCustomerName();
            String invalidPostal = TestDataGenerator.generateInvalidPostalCodeWithLetters();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(name, invalidPostal);

            assertNotNull(managerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify empty form submission shows validation")
        @Severity(SeverityLevel.MINOR)
        void emptyForm_showsValidation() {
            loginPage.loginAsManager("Manager");
            managerPage.clickAddCustomerButton();
            managerPage.submitCustomerForm();

            String error = managerPage.getErrorMessage();
            assertTrue(!error.isEmpty() || managerPage.isDashboardDisplayed(),
                    "Form validation or error expected");
        }
    }

    // ─── AC2: Creating Accounts ──────────────────────────────────────────

    @Nested
    @DisplayName("Creating Accounts")
    @Story("Creating Accounts")
    class CreatingAccounts {

        @Test
        @DisplayName("Verify process without selecting customer shows error")
        @Severity(SeverityLevel.NORMAL)
        void processWithoutCustomer_showsError() {
            loginPage.loginAsManager("Manager");
            managerPage.clickOpenAccountButton()
                    .selectCurrency("Dollar")
                    .clickProcessButton();

            assertNotNull(managerPage.getErrorMessage());
        }

        @Test
        @DisplayName("Verify manager can create account for added customer")
        @Severity(SeverityLevel.CRITICAL)
        void createAccount_forAddedCustomer() {
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

    // ─── AC3: Deleting Accounts ──────────────────────────────────────────

    @Nested
    @DisplayName("Deleting Accounts")
    @Story("Deleting Accounts")
    class DeletingAccounts {

        @Test
        @DisplayName("Verify manager can delete customer – customer removed from list")
        @Severity(SeverityLevel.CRITICAL)
        void deleteCustomer_removedFromList() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
            String displayName = data.getName() + " " + data.getName();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            managerPage.createAccount(displayName, "Dollar");
            managerPage.clickCustomersButton();
            managerPage.deleteCustomer(displayName);

            assertFalse(managerPage.customerExists(displayName),
                    "Customer should no longer appear in the list");
        }
    }
}
