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
//@Description("Bank Manager flows: add customer (valid/invalid), open account, delete customer. " +
//        "Validates form validation and success/error messages.")
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
            assertTrue(managerPage.isDashboardDisplayed(), "Manager dashboard should be displayed before adding customer");

            String alertMessage = managerPage.addCustomerAndGetAlertMessage(data.getName(), data.getPostalCode());
            assertNotNull(alertMessage, "JS alert should be shown when customer is added successfully");
            assertFalse(alertMessage.isEmpty(), "Alert message should not be empty");
        }

        @Test
        @DisplayName("Verify customer name with numbers is rejected")
        @Severity(SeverityLevel.NORMAL)
        void customerName_withNumbers_rejected() {
            String invalidName = TestDataGenerator.generateInvalidCustomerNameWithNumbers();
            String postalCode = TestDataGenerator.generateValidPostalCode();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(invalidName, postalCode);

            String error = managerPage.getErrorMessage();
            assertNotNull(error, "Error message should be shown for invalid name with numbers");
            assertFalse(error.isEmpty(), "Error message should not be empty");
        }

        @Test
        @DisplayName("Verify customer name with special characters is rejected")
        @Severity(SeverityLevel.NORMAL)
        void customerName_withSpecialCharacters_rejected() {
            String invalidName = TestDataGenerator.generateInvalidCustomerNameWithSpecialChars();
            String postalCode = TestDataGenerator.generateValidPostalCode();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(invalidName, postalCode);

            String error = managerPage.getErrorMessage();
            assertNotNull(error, "Error message should be shown for invalid name with special characters");
            assertFalse(error.isEmpty(), "Error message should not be empty");
        }

        @Test
        @DisplayName("Verify postal code with letters is rejected")
        @Severity(SeverityLevel.NORMAL)
        void postalCode_withLetters_rejected() {
            String name = TestDataGenerator.generateValidCustomerName();
            String invalidPostal = TestDataGenerator.generateInvalidPostalCodeWithLetters();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(name, invalidPostal);

            String error = managerPage.getErrorMessage();
            assertNotNull(error, "Error message should be shown for invalid postal code with letters");
            assertFalse(error.isEmpty(), "Error message should not be empty");
        }


    }

    // ─── AC2: Creating Accounts ──────────────────────────────────────────

    @Nested
    @DisplayName("Creating Accounts")
    @Story("Creating Accounts")
    class CreatingAccounts {

        @Test
        @DisplayName("Verify manager can create account for added customer")
        @Severity(SeverityLevel.CRITICAL)
        void createAccount_forAddedCustomer() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
            String displayName = data.getName() + " " + data.getName();

            loginPage.loginAsManager("Manager");
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            String alertMessage = managerPage.createAccountAndGetAlertMessage(displayName, "Dollar");

            assertNotNull(alertMessage, "JS alert should be shown when account is created successfully");
            assertFalse(alertMessage.isEmpty(), "Alert message should not be empty");
        }
    }


}
