# XYZ Bank Test Automation Framework

## Overview
Professional test automation framework for XYZ Bank application using Selenium WebDriver, JUnit 5, and Allure Reports. The framework follows Page Object Model, SOLID, and DRY principles.

## Features
- ✅ Selenium WebDriver 4.15
- ✅ JUnit 5 with parameterized tests
- ✅ Allure Report integration with epics, stories, and tags
- ✅ Log4j 2 for comprehensive logging
- ✅ Page Object Model with Page Factory pattern
- ✅ TestDataGenerator utility for test data creation
- ✅ Docker & Docker Compose support
- ✅ GitHub Actions CI/CD pipeline
- ✅ Headless mode for CI/CD, headed mode for local development
- ✅ Automatic screenshot capture on test failure
- ✅ Professional code documentation with JavaDoc

## Project Structure

```
QAM06_XYZ_TEST_AUTOMATION/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── config/          # Configuration management
│   │   │   ├── driver/          # WebDriver factory
│   │   │   ├── pages/           # Page objects
│   │   │   │   ├── manager/     # Manager pages
│   │   │   │   └── customer/    # Customer pages
│   │   │   └── utils/           # Utilities (Selenium, TestData)
│   │   └── resources/
│   │       ├── config.properties
│   │       └── log4j2.xml
│   └── test/
│       └── java/org/example/
│           ├── base/            # BaseTest class
│           └── tests/
│               ├── manager/     # Manager tests
│               └── customer/    # Customer tests
├── .github/
│   └── workflows/
│       └── test-automation.yml  # GitHub Actions pipeline
├── pom.xml                      # Maven configuration
├── Dockerfile                   # Docker image definition
├── docker-compose.yml           # Docker Compose configuration
├── allure.properties            # Allure configuration
└── README.md                    # This file
```

## Test Coverage

### Manager Tests (User Story 1)
- ✅ Add new customer with valid data
- ✅ Customer name validation (no numbers, no special characters)
- ✅ Postal code validation (numeric only)
- ✅ Create accounts for customers
- ✅ Prevent account creation without selecting customer
- ✅ Delete customer accounts
- ✅ Form validation with empty fields

### Customer Tests (User Story 2)
- ✅ Access control - cannot access account before creation
- ✅ Deposit funds (positive amounts)
- ✅ Deposit validation (zero and negative amounts)
- ✅ Withdraw funds
- ✅ Withdraw validation (zero, negative, insufficient balance)
- ✅ View transaction history
- ✅ Transaction history persistence after logout/login
- ✅ Transaction history sorting and chronological order
- ✅ Transaction history read-only (cannot be modified)
- ✅ Account access revoked after deletion

## Prerequisites

### Local Development
- Java 17 or higher
- Maven 3.8.1 or higher
- Chrome/Chromium browser
- ChromeDriver (automatically managed via WebDriverManager)

### Docker
- Docker 20.10+
- Docker Compose 2.0+

## Installation

### Local Setup
```bash
# Clone the repository
git clone <repository-url>
cd QAM06_XYZ_TEST_AUTOMATION

# Install dependencies
mvn clean install

# Run all tests
mvn clean test

# Run specific test class
mvn clean test -Dtest=ManagerTest

# Run tests with specific tag
mvn clean test -Dgroups=smoke

# Generate Allure report
mvn allure:report

# View Allure report
mvn allure:serve
```

### Docker Setup
```bash
# Build Docker image
docker build -t xyz-bank-test-automation .

# Run tests in Docker
docker run --rm xyz-bank-test-automation

# Using Docker Compose
docker-compose up --build --remove-orphans

# View Allure Report Server
# Navigate to http://localhost:4040
```

## Configuration

### config.properties
Edit `src/main/resources/config.properties` to configure:
- Browser type (chrome, firefox, etc.)
- Headless mode (true for CI/CD, false for local)
- Window maximization
- Wait timeouts
- Base URL
- Log level
- Screenshot settings

### Example Configuration
```properties
browser=chrome
headless.mode=false              # Set to true for CI/CD
window.maximize=true
implicit.wait=10
explicit.wait=15
page.load.timeout=30
base.url=https://www.demo.globalqa.com/banking/index.php
screenshot.on.failure=true
```

## Running Tests

### Local Execution (Headed Mode)
```bash
mvn clean test
```

### CI/CD Execution (Headless Mode)
```bash
mvn clean test -Dheadless.mode=true
```

### Filter Tests by Tag
```bash
# Run only smoke tests
mvn clean test -Dgroups=smoke

# Run validation tests
mvn clean test -Dgroups=validation

# Run security tests
mvn clean test -Dgroups=security
```

### Run Specific Test Class
```bash
mvn clean test -Dtest=ManagerTest
mvn clean test -Dtest=CustomerTest
```

## Allure Report

### Generate and View Report Locally
```bash
# Generate report
mvn allure:report

# Serve report (opens in browser)
mvn allure:serve
```

### Report Features
- Epic grouping (User Story 1 & 2)
- Story categorization
- Test type tags (smoke, validation, security, etc.)
- Severity levels
- Automatic screenshot attachments on failure
- Timeline and history
- Trend analysis

### Accessing Report in CI/CD
The report is automatically published to GitHub Pages after each test run:
- URL: `https://<username>.github.io/<repo>/allure-report`

## Logging

Logs are configured via Log4j 2 in `src/main/resources/log4j2.xml`:
- Console output for immediate feedback
- Rolling file appenders (logs directory)
- Separate logs per test execution
- Debug level for all framework classes

## Page Object Model

All pages follow the POM pattern with action-based methods:

### LoginPage
```java
LoginPage loginPage = new LoginPage(driver);
loginPage.loginAsCustomer("John");
loginPage.loginAsManager("Harry");
```

### ManagerDashboardPage
```java
ManagerDashboardPage manager = new ManagerDashboardPage(driver);
manager.addCustomer("Jane", "12345");
manager.createAccount("Jane", "Dollar");
manager.deleteCustomer("Jane");
```

### CustomerDashboardPage
```java
CustomerDashboardPage customer = new CustomerDashboardPage(driver);
customer.deposit("100.00");
customer.withdraw("50.00");
customer.clickTransactionsButton();
customer.logout();
```

## Test Data Generation

Using TestDataGenerator utility for realistic test data:

```java
// Generate valid customer data
TestDataGenerator.CustomerTestData data = 
    TestDataGenerator.generateCustomerTestData();

// Generate specific invalid data for validation testing
String invalidName = TestDataGenerator.generateInvalidCustomerNameWithNumbers();
String invalidPostal = TestDataGenerator.generateInvalidPostalCodeWithLetters();

// Generate transaction amounts
String depositAmount = TestDataGenerator.generateValidDepositAmount();
String withdrawAmount = TestDataGenerator.generateValidWithdrawalAmount();
```

## CI/CD Pipeline (GitHub Actions)

The pipeline (`test-automation.yml`) performs:
1. ✅ Code checkout
2. ✅ JDK setup (Java 17)
3. ✅ Maven build and test execution in headless mode
4. ✅ Allure report generation
5. ✅ Report artifact upload
6. ✅ GitHub Pages publication
7. ✅ PR comment with results
8. ✅ Screenshot upload on failure

### Triggering Pipeline
- Push to main/develop branches
- Pull requests to main/develop
- Scheduled daily runs (2 AM)

## Best Practices Implemented

### SOLID Principles
- **Single Responsibility**: Each class has one purpose
- **Open/Closed**: Easily extendable page objects
- **Liskov Substitution**: All pages extend BasePage
- **Interface Segregation**: Focused interfaces
- **Dependency Inversion**: Dependency injection via constructor

### DRY (Don't Repeat Yourself)
- Common methods in BasePage and BasTest
- Utility classes for reusable functions
- Configuration management for environment-specific settings
- Test data generation to avoid hardcoding

### Code Quality
- Comprehensive JavaDoc comments
- Professional logging with Log4j 2
- Fluent API for readable test code
- Proper exception handling
- Clean code formatting

## Troubleshooting

### Chrome Driver Issues
```bash
# Ensure WebDriverManager dependency is included
# It automatically downloads the correct ChromeDriver version
```

### Allure Report Not Generated
```bash
# Check Allure is installed
mvn allure:report

# Verify allure-junit5 dependency in pom.xml
```

### Tests Fail in Docker
```bash
# Check Docker resources
docker stats

# View container logs
docker logs <container-id>
```

### Screenshots Not Attached
- Verify `screenshot.on.failure=true` in config.properties
- Check screenshots directory has write permissions
- Ensure test framework is catching exceptions properly

## Extending the Framework

### Adding New Tests
1. Create test class extending `BaseTest`
2. Use appropriate `@Tag` for categorization
3. Add `@DisplayName`, `@Story`, `@Epic` annotations
4. Use page objects for element interactions
5. Leverage `TestDataGenerator` for test data

### Adding New Page Objects
1. Create class extending `BasePage`
2. Define locators as private static final By constants
3. Implement action-based methods
4. Add @Step annotations for Allure steps
5. Use logging for traceability

## Contributing

1. Follow existing code style and patterns
2. Add tests for new features
3. Update documentation
4. Run tests locally before pushing
5. Ensure CI/CD pipeline passes

## License
[Add your license information]

## Contact
QA Team - [Contact information]

## Version History
- v1.0 - Initial framework setup with manager and customer tests

