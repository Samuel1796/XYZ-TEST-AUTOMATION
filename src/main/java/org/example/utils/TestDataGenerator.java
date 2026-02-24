package org.example.utils;

import com.github.javafaker.Faker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test data generator utility for creating valid test data.
 * Uses JavaFaker library and custom logic to generate customer, account, and transaction data.
 *
 * @author QA Team
 * @version 1.0
 */
public class TestDataGenerator {

    private static final Logger logger = LogManager.getLogger(TestDataGenerator.class);
    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    /**
     * Generates a valid customer name (alphabetic characters only)
     *
     * @return a valid customer name
     */
    public static String generateValidCustomerName() {
        String name = faker.name().firstName();
        logger.debug("Generated customer name: " + name);
        return name;
    }

    /**
     * Generates an invalid customer name with numbers
     *
     * @return customer name with numbers
     */
    public static String generateInvalidCustomerNameWithNumbers() {
        String name = faker.name().firstName() + "123";
        logger.debug("Generated invalid customer name with numbers: " + name);
        return name;
    }

    /**
     * Generates an invalid customer name with special characters
     *
     * @return customer name with special characters
     */
    public static String generateInvalidCustomerNameWithSpecialChars() {
        String name = faker.name().firstName() + "@#$";
        logger.debug("Generated invalid customer name with special characters: " + name);
        return name;
    }

    /**
     * Generates a valid postal code (numeric characters only)
     *
     * @return a valid postal code
     */
    public static String generateValidPostalCode() {
        String postalCode = String.format("%05d", random.nextInt(100000));
        logger.debug("Generated postal code: " + postalCode);
        return postalCode;
    }

    /**
     * Generates an invalid postal code with letters
     *
     * @return postal code with letters
     */
    public static String generateInvalidPostalCodeWithLetters() {
        String postalCode = "12345ABC";
        logger.debug("Generated invalid postal code with letters: " + postalCode);
        return postalCode;
    }

    /**
     * Generates a valid deposit amount (positive)
     *
     * @return a valid deposit amount
     */
    public static String generateValidDepositAmount() {
        int amount = random.nextInt(1000) + 1; // 1 to 1000
        String amountStr = String.valueOf(amount);
        logger.debug("Generated deposit amount: " + amountStr);
        return amountStr;
    }

    /**
     * Generates a valid withdrawal amount (positive whole number)
     *
     * @return a valid withdrawal amount
     */
    public static String generateValidWithdrawalAmount() {
        int amount = random.nextInt(500) + 1; // 1 to 500
        String amountStr = String.valueOf(amount);
        logger.debug("Generated withdrawal amount: " + amountStr);
        return amountStr;
    }

    /**
     * Generates a zero amount (invalid for deposits/withdrawals)
     *
     * @return "0"
     */
    public static String generateZeroAmount() {
        logger.debug("Generated zero amount");
        return "0";
    }

    /**
     * Generates a negative amount (invalid for deposits/withdrawals)
     *
     * @return a negative amount as string
     */
    public static String generateNegativeAmount() {
        int amount = -(random.nextInt(100) + 1);
        String amountStr = String.valueOf(amount);
        logger.debug("Generated negative amount: " + amountStr);
        return amountStr;
    }

    /**
     * Generates a large deposit amount (whole number)
     *
     * @return a large amount
     */
    public static String generateLargeAmount() {
        int amount = random.nextInt(10000) + 5000; // 5000 to 15000
        String amountStr = String.valueOf(amount);
        logger.debug("Generated large amount: " + amountStr);
        return amountStr;
    }

    /**
     * Data for one invalid "Add Customer" case (for parameterized tests).
     * Holds a description and the invalid name/postalCode to submit.
     */
    public static class InvalidAddCustomerCase {
        private final String description;
        private final String name;
        private final String postalCode;

        public InvalidAddCustomerCase(String description, String name, String postalCode) {
            this.description = description;
            this.name = name;
            this.postalCode = postalCode;
        }

        public String getDescription() { return description; }
        public String getName() { return name; }
        public String getPostalCode() { return postalCode; }
    }

    /**
     * Provides invalid add-customer data for parameterized tests.
     * Each case has a description and (name, postalCode) that should be rejected by the Add Customer form.
     *
     * @return list of invalid add-customer cases
     */
    public static List<InvalidAddCustomerCase> invalidAddCustomerCases() {
        List<InvalidAddCustomerCase> cases = new ArrayList<>();
        cases.add(new InvalidAddCustomerCase(
                "name with numbers",
                generateInvalidCustomerNameWithNumbers(),
                generateValidPostalCode()));
        cases.add(new InvalidAddCustomerCase(
                "name with special characters",
                generateInvalidCustomerNameWithSpecialChars(),
                generateValidPostalCode()));
        cases.add(new InvalidAddCustomerCase(
                "postal code with letters",
                generateValidCustomerName(),
                generateInvalidPostalCodeWithLetters()));
        return cases;
    }

    /**
     * Generates test customer data
     *
     * @return CustomerTestData object with generated values
     */
    public static CustomerTestData generateCustomerTestData() {
        CustomerTestData data = new CustomerTestData();
        data.setName(generateValidCustomerName());
        data.setPostalCode(generateValidPostalCode());
        logger.debug("Generated customer test data: " + data);
        return data;
    }

    /**
     * Inner class to hold customer test data
     */
    public static class CustomerTestData {
        private String name;
        private String postalCode;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        @Override
        public String toString() {
            return "CustomerTestData{" +
                    "name='" + name + '\'' +
                    ", postalCode='" + postalCode + '\'' +
                    '}';
        }
    }
}

