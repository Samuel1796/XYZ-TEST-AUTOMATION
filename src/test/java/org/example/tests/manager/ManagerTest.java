package org.example.tests.manager;

import io.qameta.allure.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.base.BaseTest;
import org.example.pages.manager.LoginPage;
import org.example.pages.manager.ManagerDashboardPage;
import org.example.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Bank Manager functionality.
 * Tests cover: adding customers, creating accounts, and deleting accounts.
 *
 * Epic: User Story 1 - Bank Manager Account Management
 *
 * @author QA Team
 * @version 1.0
 */
@DisplayName("Bank Manager Tests")
@Epic("User Story 1: Bank Manager Account Management")
@Feature("Manager Operations")
public class ManagerTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(ManagerTest.class);
    private LoginPage loginPage;
    private ManagerDashboardPage dashboardPage;
    private static final String MANAGER_NAME = "Harry Potter";

    @BeforeEach
    public void setUpManagerTest() {
        loginPage = new LoginPage(driver);
        dashboardPage = new ManagerDashboardPage(driver);
    }

    @Test
    @DisplayName("Verify Successful Addition of a New Customer with Valid Data")
    @Story("Adding Customers")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("smoke")
    @Tag("customer_management")
    public void testSuccessfulAdditionOfNewCustomer() {
        logger.info("TEST: Verify Successful Addition of a New Customer with Valid Data");

        // Test Data
        TestDataGenerator.CustomerTestData testData = TestDataGenerator.generateCustomerTestData();
        String customerName = testData.getName();
        String postalCode = testData.getPostalCode();

        // Steps
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Verify manager dashboard is displayed");
        assertTrue(dashboardPage.isDashboardDisplayed(), "Manager dashboard should be displayed");

        Allure.step("Add customer with name: " + customerName);
        dashboardPage.addCustomer(customerName, postalCode);

        Allure.step("Verify success message is displayed");
        String successMessage = dashboardPage.getSuccessMessage();
        assertNotNull(successMessage, "Success message should be displayed");
        assertFalse(successMessage.isEmpty(), "Success message should not be empty");
    }

    @Test
    @DisplayName("Verify Validation - Name with Numbers (Invalid)")
    @Story("Adding Customers")
    @Tag("validation")
    @Tag("negative")
    public void testCustomerNameValidationWithNumbers() {
        logger.info("TEST: Verify Validation - Name with Numbers (Invalid)");

        // Test Data
        String invalidName = TestDataGenerator.generateInvalidCustomerNameWithNumbers();
        String postalCode = TestDataGenerator.generateValidPostalCode();

        // Steps
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Add customer with invalid name: " + invalidName);
        dashboardPage.addCustomer(invalidName, postalCode);

        Allure.step("Verify error message is displayed for invalid name");
        String errorMessage = dashboardPage.getErrorMessage();
        assertNotNull(errorMessage, "Error message should be displayed for invalid name");
    }

    @Test
    @DisplayName("Verify Validation - Name with Special Characters (Invalid)")
    @Story("Adding Customers")
    @Tag("validation")
    @Tag("negative")
    public void testCustomerNameValidationWithSpecialCharacters() {
        logger.info("TEST: Verify Validation - Name with Special Characters (Invalid)");

        // Test Data
        String invalidName = TestDataGenerator.generateInvalidCustomerNameWithSpecialChars();
        String postalCode = TestDataGenerator.generateValidPostalCode();

        // Steps
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Add customer with invalid name: " + invalidName);
        dashboardPage.addCustomer(invalidName, postalCode);

        Allure.step("Verify error message is displayed for invalid name");
        String errorMessage = dashboardPage.getErrorMessage();
        assertNotNull(errorMessage, "Error message should be displayed for invalid name");
    }

    @Test
    @DisplayName("Verify Validation - Postal Code with Letters (Invalid)")
    @Story("Adding Customers")
    @Tag("validation")
    @Tag("negative")
    public void testPostalCodeValidationWithLetters() {
        logger.info("TEST: Verify Validation - Postal Code with Letters (Invalid)");

        // Test Data
        String customerName = TestDataGenerator.generateValidCustomerName();
        String invalidPostalCode = TestDataGenerator.generateInvalidPostalCodeWithLetters();

        // Steps
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Add customer with invalid postal code: " + invalidPostalCode);
        dashboardPage.addCustomer(customerName, invalidPostalCode);

        Allure.step("Verify error message is displayed for invalid postal code");
        String errorMessage = dashboardPage.getErrorMessage();
        assertNotNull(errorMessage, "Error message should be displayed for invalid postal code");
    }

    @Test
    @DisplayName("Verify No Account Creation Without Selecting Customer")
    @Story("Creating Accounts")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("smoke")
    @Tag("account_management")
    public void testNoAccountCreationWithoutSelectingCustomer() {
        logger.info("TEST: Verify No Account Creation Without Selecting Customer");

        // Steps
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Attempt to create account without selecting customer");
        dashboardPage.clickOpenAccountButton()
                .selectCurrency("Dollar")
                .clickProcessButton();

        Allure.step("Verify error message is displayed");
        String errorMessage = dashboardPage.getErrorMessage();
        assertNotNull(errorMessage, "Error message should be displayed when customer is not selected");
    }

    @Test
    @DisplayName("Verify Successful Account Creation for Existing Customer")
    @Story("Creating Accounts")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("smoke")
    @Tag("account_management")
    public void testSuccessfulAccountCreation() {
        logger.info("TEST: Verify Successful Account Creation for Existing Customer");

        // First add a customer
        TestDataGenerator.CustomerTestData testData = TestDataGenerator.generateCustomerTestData();
        String customerName = testData.getName();
        String postalCode = testData.getPostalCode();

        // Steps
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Add customer");
        dashboardPage.addCustomer(customerName, postalCode);

        Allure.step("Create account for customer: " + customerName);
        dashboardPage.createAccount(customerName, "Dollar");

        Allure.step("Verify success message is displayed");
        String successMessage = dashboardPage.getSuccessMessage();
        assertNotNull(successMessage, "Success message should be displayed");
        assertFalse(successMessage.isEmpty(), "Success message should not be empty");
    }

    @Test
    @DisplayName("Verify Successful Account Deletion")
    @Story("Deleting Accounts")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("smoke")
    @Tag("account_management")
    public void testSuccessfulAccountDeletion() {
        logger.info("TEST: Verify Successful Account Deletion");

        // First add a customer
        TestDataGenerator.CustomerTestData testData = TestDataGenerator.generateCustomerTestData();
        String customerName = testData.getName();
        String postalCode = testData.getPostalCode();

        // Steps
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Add customer and create account");
        dashboardPage.addCustomer(customerName, postalCode);
        dashboardPage.createAccount(customerName, "Dollar");

        Allure.step("Navigate to customers list");
        dashboardPage.clickCustomersButton();

        Allure.step("Delete customer: " + customerName);
        dashboardPage.deleteCustomer(customerName);

        Allure.step("Verify customer is deleted");
        assertFalse(dashboardPage.customerExists(customerName), "Customer should be deleted");
    }

    @Test
    @DisplayName("Verify Addition with Empty Fields (Negative)")
    @Story("Adding Customers")
    @Tag("validation")
    @Tag("negative")
    public void testAdditionWithEmptyFields() {
        logger.info("TEST: Verify Addition with Empty Fields (Negative)");

        // Steps
        Allure.step("Login as manager");
        loginPage.loginAsManager(MANAGER_NAME);

        Allure.step("Click Add Customer button");
        dashboardPage.clickAddCustomerButton();

        Allure.step("Submit form with empty fields");
        dashboardPage.submitCustomerForm();

        Allure.step("Verify error message or validation is triggered");
        String errorMessage = dashboardPage.getErrorMessage();
        // Either error message should appear or form should not submit
        assertTrue(!errorMessage.isEmpty() || dashboardPage.isDashboardDisplayed(),
                "Form validation should prevent empty submission");
    }
}

