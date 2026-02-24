package org.example.tests.manager;

import io.qameta.allure.*;
import org.example.setup.BaseTest;
import org.example.pages.manager.LoginPage;
import org.example.pages.manager.ManagerDashboardPage;
import org.example.utils.SeleniumUtils;
import org.example.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User Story 1: As a Bank Manager, I want to add customers, create accounts,
 * and delete accounts so that I can manage customer accounts efficiently.
 */
@DisplayName("US1 – Bank Manager")
@Epic("XYZ Bank")
@Feature("Manager Account Management")
@Tag("manager")
@Tag("us1")
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

            loginPage.loginAsManager();

            String alertMessage = managerPage.addCustomerAndGetAlertMessage(data.getName(), data.getPostalCode());
            assertNotNull(alertMessage, "JS alert should be shown when customer is added successfully");
            assertTrue(alertMessage.contains("Customer added"),
                    "Alert should confirm customer was added (e.g. 'Customer added successfully with customer id : ...'). Got: " + alertMessage);
        }

        /**
         * Parameterized test fed by TestDataGenerator.invalidAddCustomerCases().
         * Covers: name with numbers, name with special characters, postal code with letters.
         */
        @ParameterizedTest(name = "Invalid add customer: {0}")
        @MethodSource("invalidAddCustomerData")
        @DisplayName("Verify invalid customer data is rejected")
        @Severity(SeverityLevel.NORMAL)
        void invalidCustomerData_rejected(String description, String name, String postalCode) {
            loginPage.loginAsManager();
            managerPage.addCustomer(name, postalCode);

            String error = managerPage.getErrorMessage();
            assertNotNull(error, "Error message should be shown for invalid data: " + description);
            assertFalse(error.isEmpty(), "Error message should not be empty");
        }

        static Stream<Arguments> invalidAddCustomerData() {
            return TestDataGenerator.invalidAddCustomerCases().stream()
                    .map(c -> Arguments.of(c.getDescription(), c.getName(), c.getPostalCode()));
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

            loginPage.loginAsManager();
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            String alertMessage = managerPage.createAccountAndGetAlertMessage(displayName, "Dollar");

            assertNotNull(alertMessage, "JS alert should be shown when account is created successfully");
            assertFalse(alertMessage.isEmpty(), "Alert message should not be empty");
        }

    }



    // ─── AC3: Deleting Accounts ──────────────────────────────────────────

    @Nested
    @DisplayName("Deleting Accounts")
    @Story("Deleting Accounts")
    class DeletingAccounts {

        @Test
        @DisplayName("Verify manager can delete customer and customer is removed from list")
        @Severity(SeverityLevel.CRITICAL)
        void managerCanDeleteCustomer_removedFromList() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
            String displayName = data.getName() + " " + data.getName();

            loginPage.loginAsManager();
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            managerPage.createAccount(displayName, "Dollar");
            managerPage.clickCustomersButton();
            managerPage.scrollToCustomerAndDelete(displayName);

            assertFalse(managerPage.customerExists(displayName),
                    "Deleted customer should not appear in the Customers table");
        }


        @Test
        @DisplayName("Verify deleted customer no longer appears in Customer login dropdown")
        @Severity(SeverityLevel.NORMAL)
        void managerCanDeleteCustomer_notInCustomerDropdown() {
            TestDataGenerator.CustomerTestData data = TestDataGenerator.generateCustomerTestData();
            String displayName = data.getName() + " " + data.getName();

            loginPage.loginAsManager();
            managerPage.addCustomer(data.getName(), data.getPostalCode());
            managerPage.createAccount(displayName, "Dollar");
            managerPage.clickCustomersButton();
            managerPage.scrollToCustomerAndDelete(displayName);
            assertFalse(managerPage.customerExists(displayName), "Customer should be removed from list");

            managerPage.clickHomeButton();
            loginPage.selectCustomerUserType();
            assertFalse(loginPage.isCustomerInDropdown(displayName),
                    "Deleted customer should not appear in Customer login dropdown");
        }
    }
}
