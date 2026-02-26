# XYZ Bank Test Automation – Project Structure & Implementation

This document walks through the full project structure and how the implementation is organized.

---

## 1. Project overview

- **Application under test:** [XYZ Bank (GlobalSQA)](https://www.globalsqa.com/angularJs-protractor/BankingProject/) – AngularJS banking app (Customer Login, Bank Manager Login, Add Customer, Open Account, Deposit, Withdraw, Transactions).
- **Stack:** Java 17, Maven, Selenium WebDriver 4, JUnit 5, Allure 2, Log4j 2, JavaFaker.
- **Scope:** Two user stories – **US1** (Bank Manager: add customers, create accounts, delete accounts) and **US2** (Customer: view transactions, deposit, withdraw).

---

## 2. Folder structure

```
XYZ-TEST-AUTOMATION/
├── .github/
│   ├── workflows/
│   │   └── test-automation.yml    # CI pipeline (test, report, publish, notify)
│   └── REPO_SECRETS.md            # CI secrets/variables documentation
├── docs/
│   ├── PROJECT_IMPLEMENTATION.md  # This file
│   └── RUBRIC_ASSESSMENT.md       # Critique and rubric scoring
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── config/            # Configuration
│   │   │   ├── driver/            # WebDriver lifecycle
│   │   │   ├── pages/             # Page Object Model
│   │   │   └── utils/             # Shared helpers & test data
│   │   └── resources/
│   │       ├── config.properties  # Base URL, timeouts, headless
│   │       └── log4j2.xml         # Logging configuration
│   └── test/
│       ├── java/org/example/
│       │   ├── setup/             # Test setup (base class)
│       │   ├── tests/             # Test classes (manager, customer)
│       │   └── utils/             # Test-only utils (e.g. Allure writer)
│       └── resources/
│           └── allure/            # Allure static assets (categories, env, executor)
├── allure.properties              # Allure results/report paths
├── pom.xml                        # Maven build & dependencies
├── .gitignore
└── README.md
```

- **Main code** lives under `src/main/java` (config, driver, pages, utils).
- **Tests** live under `src/test/java` (setup, tests, test utils).
- **CI** is under `.github/workflows`; Allure assets and docs under `src/test/resources/allure` and `docs/`.

---

## 3. Implementation walkthrough

### 3.1 POM (Maven) – `pom.xml`

- **Artifact:** `QAM06_XYZ_TEST_AUTOMATION`, version `1.0-SNAPSHOT`.
- **Properties:** Java 17, Selenium 4.15, JUnit 5.10, Allure 2.17, Log4j 2.21, UTF-8 encoding.
- **Dependencies:**
  - **Selenium** – `selenium-java` (WebDriver).
  - **JUnit 5** – `junit-jupiter-api`, `junit-jupiter-engine`, `junit-jupiter-params`.
  - **Allure** – `allure-junit5`, `allure-java-commons`.
  - **Log4j 2** – `log4j-core`, `log4j-api`.
  - **JavaFaker** – `javafaker` for test data.
- **Plugins:**
  - **maven-clean-plugin** – cleans `target`.
  - **maven-antrun-plugin:**
    - **initialize:** deletes `target/allure-results` and `target/allure-report` (clean run).
    - **test:** creates `allure-results`, copies `categories.json`, `environment.properties`, `executor.json` into it.
  - **maven-surefire-plugin:** runs `*Test`/`*Tests`, single thread, `allure.results.directory` set to `target/allure-results`.
  - **allure-maven:** `resultsDirectory` = `target/allure-results`, `reportDirectory` = `target/allure-report`; binds `report` goal to `test` phase.
  - **maven-compiler-plugin:** source/target 17.

So: one Maven run does clean, test, Allure result generation, and report generation; Allure paths are aligned for local and CI.

---

## 3.2 Detailed File-by-File Explanation

This section describes every Java file, configuration file, and asset in the project, explaining its purpose, contents, and responsibilities.

### **Configuration & Properties Files**

#### `config.properties` (src/main/resources/)

**Purpose:** Central configuration for test environment settings.

**Contents & Keys:**
```properties
# Browser Configuration
browser=chrome
headless.mode=false
window.maximize=true
implicit.wait=5
explicit.wait=10
page.load.timeout=20

# Application under test (override in CI with -Dbase.url=...)
base.url=https://www.globalsqa.com/angularJs-protractor/BankingProject/

# Log Configuration
log.level=DEBUG
log.path=logs

# Allure Report Configuration
allure.results.directory=target/allure-results
allure.report.directory=target/allure-report
```

**Key Points:**
- **headless.mode=false** – Run with browser visible locally; set to `true` in CI
- **implicit.wait=5** – Default wait time when finding elements (seconds)
- **explicit.wait=10** – Explicit WebDriverWait timeout for conditional waits (seconds)
- **page.load.timeout=20** – Maximum time for page to fully load (seconds)
- **base.url** – Application URL; can be overridden via `-Dbase.url=...` in CI
- **log.level=DEBUG** – Verbose logging for debugging; can be set to INFO in production

**Used by:** BaseTest.Config static inner class reads this file during static initialization.

---

#### `log4j2.xml` (src/main/resources/)

**Purpose:** Configures Apache Log4j2 logging framework.

**Contents:**
- **Console Appender** – Logs to stdout with pattern: `[TIMESTAMP] [LEVEL] [CLASS:METHOD] MESSAGE`
- **File Appender** – Logs to `logs/app.log` with rolling file policy (daily rollover, 30-day retention)
- **Root Logger** – Level set via config.properties `log.level`
- **Package-specific loggers** – Can override log level for specific classes (e.g. Selenium, org.example)

**Example Output:**
```
[2026-02-21 14:32:10.123] [INFO ] [BaseTest:setUp] Test started: addNewCustomer_withValidData
[2026-02-21 14:32:12.456] [DEBUG] [SeleniumUtils:waitAndClick] Clicking element: By.id("addBtn")
```

**Used by:** Every Java class via `LogManager.getLogger(ClassName.class)`.

---

#### `allure.properties` (project root)

**Purpose:** Tells Allure Maven plugin where to find results and where to generate reports.

**Contents:**
```properties
allure.results.directory=target/allure-results
allure.report.directory=target/allure-report
```

**Why separate from config.properties:**
- config.properties is for test behavior (URLs, timeouts)
- allure.properties is for Maven build (Allure plugin configuration)

**Used by:** maven-allure-plugin during `mvn allure:report`.

---

### **Java Source Files – Main (src/main/java/org/example/)**

#### **Package: config/**

##### `AppConfig.java`
(If exists; otherwise BaseTest.Config handles this)

**Purpose:** Central configuration loader.

**Responsibilities:**
- Load `config.properties` into static Properties object
- Provide getter methods for each configuration key
- Support system property overrides

**Key Methods:**
```java
public static String getBaseUrl()           // Returns app URL
public static boolean isHeadlessMode()      // Returns headless flag
public static long getImplicitWait()        // Returns implicit wait (seconds)
public static long getExplicitWait()        // Returns explicit wait (seconds)
public static long getPageLoadTimeout()     // Returns page load timeout (seconds)
public static boolean shouldMaximizeWindow() // Returns maximize flag
```

**Override Example:**
```bash
mvn test -Dbase.url=https://staging.example.com -Dheadless.mode=true
```

System properties always override config.properties values.

---

##### `AppUrls.java`

**Purpose:** Constants for all application URL fragments.

**Contents:**
```java
public static final String BANK_HOME_URL = "#/";
public static final String BANK_LOGIN_URL = "#/login";
public static final String MANAGER_LOGIN_URL = "#/manager/login";
public static final String MANAGER_DASHBOARD_URL = "#/manager/dashboard";
public static final String MANAGER_ADD_CUSTOMER_URL = "#/manager/addCust";
public static final String MANAGER_OPEN_ACCOUNT_URL = "#/manager/openAcc";
public static final String CUSTOMER_LOGIN_URL = "#/customer/login";
public static final String CUSTOMER_ACCOUNT_URL = "#/customer/account";
public static final String CUSTOMER_TRANSACTIONS_URL = "#/customer/transactions";
```

**Usage Example in Page Objects:**
```java
driver.navigate().to(Config.getBaseUrl() + AppUrls.MANAGER_DASHBOARD_URL);
```

**Why constants:**
- ✅ Single source of truth for URLs
- ✅ Easy to update if app structure changes
- ✅ Reduces hardcoded strings in test code
- ✅ Improves maintainability

---

#### **Package: pages/**

##### **Subdirectory: pages/manager/**

###### `LoginPage.java`

**Purpose:** Page Object for the login page (home page with login options).

**UI Elements (Locators):**
```java
// Buttons
@FindBy(xpath = "//button[contains(text(), 'Bank Manager Login')]")
private WebElement bankManagerLoginBtn;

@FindBy(xpath = "//button[contains(text(), 'Customer Login')]")
private WebElement customerLoginBtn;

// Customer dropdown (after selecting Customer Login)
@FindBy(name = "userSelect")
private WebElement customerDropdown;

// Login button
@FindBy(xpath = "//button[contains(text(), 'Login')]")
private WebElement loginBtn;
```

**Key Methods:**

```java
public void loginAsManager()
  // Clicks "Bank Manager Login" button
  // Returns ManagerDashboardPage for fluent chaining

public void loginAsCustomer(String customerName)
  // Clicks "Customer Login" button
  // Selects customer from dropdown by name
  // Clicks "Login" button
  // Navigates to customer account page

public boolean isCustomerInDropdown(String name)
  // Checks if customer name exists in dropdown
  // Used to verify customer was successfully added
```

**Usage in Tests:**
```java
loginPage.loginAsManager();
loginPage.loginAsCustomer("John Doe");
```

---

###### `ManagerDashboardPage.java`

**Purpose:** Page Object for the manager dashboard (add customer, open account, delete customer).

**UI Elements (Locators):**
```java
// Navigation
@FindBy(xpath = "//button[@ng-click='addCust()']")
private WebElement addCustomerBtn;

@FindBy(xpath = "//button[@ng-click='openAccount()']")
private WebElement openAccountBtn;

// Add Customer Form
@FindBy(name = "fstname")
private WebElement firstNameInput;

@FindBy(name = "lstname")
private WebElement lastNameInput;

@FindBy(name = "postCd")
private WebElement postalCodeInput;

@FindBy(xpath = "//button[@type='submit']")
private WebElement addCustomerSubmitBtn;

// Customers Table
@FindBy(xpath = "//table//tbody//tr")
private List<WebElement> customerRows;

@FindBy(xpath = "//button[@ng-click='deleteCust(cust)']")
private List<WebElement> deleteButtons;
```

**Key Methods:**

```java
public ManagerDashboardPage addCustomer(String firstName, String lastName, String postalCode)
  // Clicks "Add Customer" button
  // Fills first name, last name, postal code
  // Clicks submit
  // Returns this for fluent chaining

public String addCustomerAndGetAlertMessage(String firstName, String lastName, String postalCode)
  // Same as above, but captures and returns browser alert text
  // Used to verify validation messages

public boolean customerExists(String name)
  // Searches customer list for given name
  // Returns true if found, false otherwise

public ManagerDashboardPage createAccount(String customerName, String currency)
  // Clicks "Open Account" button
  // Selects customer and currency
  // Clicks "Process" to create account
  // Returns this for fluent chaining

public void scrollToCustomerAndDelete(String customerName)
  // Finds customer in table
  // Scrolls to view if needed
  // Clicks delete button
```

**Fluent Pattern Example:**
```java
managerPage
  .addCustomer("John", "Doe", "12345")
  .createAccount("John Doe", "Dollar");
```

---

##### **Subdirectory: pages/customer/**

###### `CustomerDashboardPage.java`

**Purpose:** Page Object for customer banking dashboard (deposit, withdraw, transactions).

**UI Elements (Locators):**
```java
// Tabs
@FindBy(xpath = "//button[contains(text(), 'Deposit')]")
private WebElement depositTab;

@FindBy(xpath = "//button[contains(text(), 'Withdraw')]")
private WebElement withdrawTab;

@FindBy(xpath = "//button[contains(text(), 'Transactions')]")
private WebElement transactionsTab;

// Deposit/Withdraw Form (shared container)
@FindBy(name = "amount")
private WebElement amountInput;

@FindBy(xpath = "//button[@type='submit']")
private WebElement depositSubmitBtn;

@FindBy(xpath = "//button[@type='submit']")
private WebElement withdrawSubmitBtn;

// Account Info
@FindBy(xpath = "//strong[@id='balance']")
private WebElement balanceDisplay;

// Transactions Table
@FindBy(xpath = "//table[@class='table table-striped']//tbody//tr")
private List<WebElement> transactionRows;
```

**Key Methods:**

```java
public void deposit(int amount)
  // Clicks deposit tab
  // Enters amount
  // Clicks deposit button
  // Waits for balance to update

public void withdraw(int amount)
  // Clicks withdraw tab
  // Enters amount
  // Clicks withdraw button
  // Waits for balance to update

public int getBalanceAsInt()
  // Returns current account balance as integer
  // Extracts from balance display element

public void clickTransactionsButton()
  // Clicks transactions tab
  // Navigates to transactions view

public int getTransactionCount()
  // Returns number of transaction rows in table

public void waitForBalanceEqualTo(int expectedBalance)
  // Explicit wait for balance to match expected value
  // Polls balance display until match or timeout

public void waitForTransactionCountAtLeast(int minimumCount)
  // Explicit wait for at least N transactions
  // Polls table row count until >= minimum or timeout

public boolean transactionTypeExists(String type)
  // Checks if transaction of type (Deposit/Withdraw) exists
  // Returns true if found, false otherwise
```

**Stale Element Handling:**
```java
public void waitForWithdrawFormBeforeTyping(int amount)
  // Waits for withdraw form to be fully rendered
  // Prevents StaleElementReferenceException when switching tabs
  // Then enters amount
```

**Usage Examples:**
```java
customerPage.deposit(1000);
customerPage.withdraw(500);
int balance = customerPage.getBalanceAsInt();
customerPage.clickTransactionsButton();
customerPage.waitForTransactionCountAtLeast(2);
```

---

#### **Package: utils/**

##### `SeleniumUtils.java`

**Purpose:** Utility methods for common Selenium operations (waits, clicks, types, alerts).

**Key Methods:**

```java
// Waits
public static void waitForElementToBeVisible(WebElement element, long seconds)
  // Explicit wait for element visibility
  // Throws TimeoutException if element not visible after timeout

public static void waitForElementToBeClickable(WebElement element, long seconds)
  // Explicit wait for element to be clickable
  // Element must be visible AND enabled

public static void waitForElementToBePresent(By locator, long seconds)
  // Explicit wait for element to be present in DOM
  // Used before interacting with dynamic elements

public static void waitForUrlContains(String urlFragment, long seconds)
  // Explicit wait for URL to contain fragment
  // Useful for verifying navigation

// Actions (with built-in waits)
public static void waitAndClick(WebElement element, long seconds)
  // Waits for element to be clickable
  // Then clicks it
  // Logs action for debugging

public static void clearAndType(WebElement element, String text, long seconds)
  // Waits for element to be present
  // Clears existing text
  // Types new text
  // Logs action for debugging

public static void waitFirstVisibleThenClearAndType(
  List<WebElement> elements, String text, long seconds)
  // Handles stale element exceptions
  // Waits for first visible element from list
  // Clears and types text
  // Retries if stale element detected

// Alerts
public static void acceptAlert(long seconds)
  // Waits for alert to appear
  // Accepts (clicks OK)
  // Logs alert text for debugging

public static String getAlertText(long seconds)
  // Waits for alert
  // Returns text
  // Does NOT dismiss alert

// Dropdowns
public static void selectFromDropdownByVisibleText(WebElement dropdown, String text)
  // Selects dropdown option by visible text
  // Handles Select element or custom dropdowns

public static void selectFromDropdownByValue(WebElement dropdown, String value)
  // Selects dropdown option by value attribute

// Configuration
public static void setExplicitWait(long seconds)
  // Sets global explicit wait timeout
  // Called by BaseTest to use config.properties value
  // Used by all page objects
```

**Why a utility class:**
- ✅ DRY principle – waits/clicks/types used in many tests
- ✅ Consistent behavior – all tests use same wait logic
- ✅ Easy to modify – change wait logic in one place
- ✅ Centralized logging – all interactions logged

---

##### `TestDataGenerator.java`

**Purpose:** Generates random test data (customer names, postal codes, amounts).

**Key Methods:**

```java
// Customer Data
public static String generateValidCustomerName()
  // Returns random name like "John Doe", "Jane Smith"
  // Uses JavaFaker library
  // Only letters and spaces

public static String generateInvalidCustomerNameWithNumbers()
  // Returns name with numbers like "John123 Doe"
  // Used for negative testing (should be rejected)

public static String generateInvalidCustomerNameWithSpecialChars()
  // Returns name with special chars like "John@Doe#"
  // Used for negative testing

public static String generateValidPostalCode()
  // Returns random 5-digit postal code
  // Example: "12345", "67890"

public static String generateInvalidPostalCodeWithLetters()
  // Returns postal code with letters like "123AB"
  // Used for negative testing

public static CustomerTestData generateCustomerTestData()
  // Returns DTO with:
  //   - name: valid customer name
  //   - postalCode: valid postal code
  // Used in setup steps

// Amount Data
public static int generateValidDepositAmount()
  // Returns random amount 100-10000
  // Example: 1500, 5000

public static int generateZeroAmount()
  // Returns 0
  // Used to test zero deposits should not change balance

public static int generateNegativeAmount()
  // Returns negative amount like -500
  // Used to test negative amounts should not change balance

public static int generateLargeAmount()
  // Returns very large amount 999999+
  // Used to test large withdrawals should fail

public static int generateValidWithdrawalAmount()
  // Returns amount that can be withdrawn (50-500)
  // Less than typical deposit amounts

// Parameterized Test Data
public static List<InvalidAddCustomerCase> invalidAddCustomerCases()
  // Returns list of invalid customer cases for parameterized tests:
  //   1. Valid name + invalid postal code
  //   2. Invalid name (with numbers) + valid postal code
  //   3. Invalid name (special chars) + valid postal code
  // Each case has description for test report
```

**DTO Classes:**
```java
public static class CustomerTestData {
    public String name;
    public String postalCode;
}

public static class InvalidAddCustomerCase {
    public String description;      // "Name with numbers"
    public String name;             // "John123"
    public String postalCode;       // "12345"
}
```

**Usage Examples:**
```java
// Random data
String name = TestDataGenerator.generateValidCustomerName();
int amount = TestDataGenerator.generateValidDepositAmount();

// For parameterized tests
@ParameterizedTest
@MethodSource("org.example.utils.TestDataGenerator#invalidAddCustomerCases")
void testInvalidData(InvalidAddCustomerCase testCase) {
    // testCase.description, testCase.name, testCase.postalCode
}
```

**Why runtime generation:**
- ✅ Fresh data per test run
- ✅ No static test data files to maintain
- ✅ Realistic data via JavaFaker
- ✅ Easy to create test variations

---

### **Java Source Files – Test (src/test/java/org/example/)**

#### **Package: setup/**

##### `BaseTest.java`

**Purpose:** Base class for all test classes; manages driver lifecycle, configuration, and Allure integration.

**(See BASETEST_EXPLANATION.md for detailed breakdown)**

**Quick Summary:**

| Component | Purpose |
|-----------|---------|
| `logger` | Logs all test events |
| `driver` | WebDriver instance |
| `loginPage`, `managerPage`, `customerPage` | Page object instances |
| `testFailed`, `lastFailure` | Failure tracking for Allure |
| `Config` (inner class) | Loads and provides configuration |
| `initConfigAndAllure()` (@BeforeAll) | One-time setup per test class |
| `setUp()` (@BeforeEach) | Fresh setup per test method |
| `tearDown()` (@AfterEach) | Cleanup and failure reporting |
| `TestFailureCapture` (interface) | Implemented by BaseTest for TestWatcher integration |

**Key Points:**
- All tests extend BaseTest
- One driver per test method (isolation)
- Automatic screenshot and error attachment on failure
- Configuration from config.properties

---

#### **Package: tests/**

##### **Subdirectory: tests/manager/**

###### `ManagerTest.java`

**Purpose:** Tests for Bank Manager functionality (US1 – Add Customer, Create Account, Delete Customer).

**Test Methods (9 tests + 3 parameterized invocations = 12 total):**

```java
// AC1: Adding Customers
@Test
void addNewCustomer_withValidData()
  // Test: Manager can add customer with valid data
  // Steps:
  //   1. Login as manager
  //   2. Add customer with valid name and postal code
  //   3. Verify customer appears in dropdown and table
  // Assertions: Customer exists in list

@ParameterizedTest
@MethodSource("invalidAddCustomerCases")
void invalidCustomerData_rejected(InvalidAddCustomerCase testCase)
  // Test: Invalid customer data is rejected
  // Runs 3 times:
  //   1. Name with numbers → Error
  //   2. Name with special chars → Error
  //   3. Postal code with letters → Error
  // Assertions: Alert message shown, customer not added

// AC2: Creating Accounts
@Test
void createAccount_forAddedCustomer()
  // Test: Manager can open account for added customer
  // Steps:
  //   1. Add customer
  //   2. Open account with Dollar currency
  //   3. Verify success alert
  // Assertions: Account created successfully

@Test
void openAccount_withoutCustomerSelection_showsError()
  // Test: Cannot create account without selecting customer
  // Steps:
  //   1. Click "Open Account" without selecting customer
  //   2. Click "Process"
  // Assertions: Error message shown

@Test
void openAccount_withoutCurrencySelection_showsError()
  // Test: Cannot create account without selecting currency
  // Steps:
  //   1. Click "Open Account"
  //   2. Select customer but not currency
  //   3. Click "Process"
  // Assertions: Error message shown

// AC3: Deleting Accounts
@Test
void managerCanDeleteCustomer_removedFromList()
  // Test: Manager can delete customer from list
  // Steps:
  //   1. Add customer
  //   2. Click delete button next to customer
  //   3. Verify alert shown
  // Assertions: Customer no longer in list

@Test
void managerCanDeleteCustomer_notInCustomerDropdown()
  // Test: Deleted customer not in dropdown
  // Steps:
  //   1. Add customer
  //   2. Delete customer
  //   3. Check dropdown
  // Assertions: Customer not in dropdown

@Test
void managerCanDeleteMultipleCustomers()
  // Test: Manager can delete multiple customers sequentially
  // Steps:
  //   1. Add customer 1, add customer 2
  //   2. Delete both
  // Assertions: Both removed from list

@Test
void managerCanViewAllAddedCustomers()
  // Test: All added customers visible in list
  // Steps:
  //   1. Add 3 customers
  //   2. Scroll customers list
  // Assertions: All 3 visible
```

**Annotations:**
- `@Tag("manager")` – Filter to run only manager tests
- `@Tag("us1")` – Filter to run only US1 tests
- `@DisplayName("...")` – Human-readable test name in report
- `@Story("Add Customers")` – Story grouping in Allure

---

##### **Subdirectory: tests/customer/**

###### `CustomerTest.java`

**Purpose:** Tests for Customer functionality (US2 – Transactions, Deposits, Withdrawals).

**Test Methods (12 tests):**

```java
// Account Access
@Test
void customerWithoutAccount_cannotAccessBankingActions()
  // Test: Customer with no account cannot perform transactions
  // Steps:
  //   1. Login as customer without account
  //   2. Try to click deposit/withdraw
  // Assertions: Buttons disabled or not visible

@Test
void deletedCustomer_cannotAccessAccount()
  // Test: Deleted customer cannot login
  // Steps:
  //   1. Create customer
  //   2. Delete customer
  //   3. Try to login as deleted customer
  // Assertions: Cannot login

// Viewing Transactions
@Test
void newAccount_emptyTransactionList()
  // Test: New account has no transactions
  // Steps:
  //   1. Create account
  //   2. Click transactions
  // Assertions: Transaction list empty

@Test
void transactionList_showsCorrectType()
  // Test: Transactions show correct type (Deposit/Withdraw)
  // Steps:
  //   1. Deposit 100
  //   2. Withdraw 50
  //   3. View transactions
  // Assertions: First is "Deposit", second is "Withdraw"

@Test
void multipleTransactions_allAppearInList()
  // Test: All transactions appear in list
  // Steps:
  //   1. Perform 3 deposits
  //   2. View transactions
  // Assertions: All 3 in list

@Test
void transactionList_persistsAfterReLogin()
  // Test: Transactions persist after logout/login
  // Steps:
  //   1. Deposit, verify in list
  //   2. Logout, login again
  //   3. Check transactions
  // Assertions: Transactions still there

// Depositing Funds
@Test
void validDeposit_updatesBalance()
  // Test: Valid deposit increases balance
  // Steps:
  //   1. Check initial balance (0)
  //   2. Deposit 500
  //   3. Check balance
  // Assertions: Balance == 500

@Test
void zeroDeposit_balanceUnchanged()
  // Test: Zero deposit doesn't change balance
  // Steps:
  //   1. Initial balance 0
  //   2. Deposit 0
  // Assertions: Balance still 0

@Test
void negativeDeposit_balanceUnchanged()
  // Test: Negative deposit rejected
  // Steps:
  //   1. Initial balance 0
  //   2. Try deposit -100
  // Assertions: Balance still 0, error shown

// Withdrawing Money
@Test
void validWithdrawal_updatesBalance()
  // Test: Valid withdrawal decreases balance
  // Steps:
  //   1. Deposit 1000
  //   2. Withdraw 500
  //   3. Check balance
  // Assertions: Balance == 500

@Test
void withdrawalExceedingBalance_balanceUnchanged()
  // Test: Cannot withdraw more than balance
  // Steps:
  //   1. Deposit 100
  //   2. Try withdraw 200
  // Assertions: Balance still 100, error shown

@Test
void withdrawExactDeposit_balanceBecomesZero()
  // Test: Can withdraw entire balance
  // Steps:
  //   1. Deposit 500
  //   2. Withdraw 500
  // Assertions: Balance == 0
```

**Annotations:**
- `@Tag("customer")` – Filter to customer tests
- `@Tag("us2")` – Filter to US2 tests
- `@DisplayName("...")` – Human-readable names
- `@Story("...")` – Story grouping

**Setup Pattern:**
```java
@BeforeEach
void setUp() {
    super.setUp();  // BaseTest setup (driver, pages)

    // Create shared customer + account for customer tests
    loginPage.loginAsManager();
    managerPage.addCustomer("John", "Doe", "12345");
    managerPage.createAccount("John Doe", "Dollar");

    // Logout from manager, login as customer
    loginPage.loginAsCustomer("John Doe");
}
```

---

#### **Package: utils/** (Test utils)

##### `AllureReportWriter.java`

**Purpose:** Writes Allure environment and executor metadata files.

**Methods:**

```java
public static void writeAllureEnvironmentAndExecutor(String baseUrl, boolean headless)
  // Called from BaseTest @BeforeAll
  // Writes two files to target/allure-results:
  //   1. environment.properties
  //   2. executor.json
```

**environment.properties Generated Content:**
```properties
Base URL=https://www.globalsqa.com/angularJs-protractor/BankingProject/
Headless Mode=false
Java Version=17.0.8
OS Name=Windows 10
OS Version=10.0
Browser=Chrome
```

**executor.json Generated Content:**
```json
{
  "name": "XYZ Bank UI Test Automation",
  "type": "github",
  "url": "https://github.com/user/XYZ-TEST-AUTOMATION",
  "buildUrl": "https://github.com/user/XYZ-TEST-AUTOMATION/actions/runs/...",
  "buildName": "GitHub Actions #123",
  "buildOrder": 123
}
```

**Why needed:**
- Allure report displays environment info
- Helps readers know test environment, Java version, OS
- CI sets build URL and build name automatically
- Local runs show local environment

---

##### `TestUtils.java`

**Purpose:** Utility methods for test operations (test name extraction, error attachment, test watcher).

**(See BASETEST_EXPLANATION.md for integration details)**

**Methods:**

```java
public static String getTestMethodName()
  // Extracts current test method name from stack trace
  // Looks for methods starting with "test" or "should"

public static String getStackTraceString(Throwable t)
  // Converts exception to readable stack trace string

public static void attachErrorOverviewToAllure(Throwable cause)
  // Attaches error summary to Allure report
  // Includes: test name, error type, message, stack trace

public static void attachScreenshot(WebDriver driver)
  // Attaches screenshot to Allure report

public static class ConfigurationException extends RuntimeException
  // Thrown when required config is missing or invalid

public static class BaseTestWatcher implements TestWatcher
  // JUnit 5 extension that captures test failures
  // Sets testFailed and lastFailure on BaseTest instance

public interface TestFailureCapture
  // Contract for test classes to support failure capture
```

---

### **Asset Files**

#### `src/test/resources/allure/categories.json`

**Purpose:** Defines failure categories for Allure report clustering.

**Categories:**
1. **Passed** – Test passed
2. **Failed – Assertion** – Test assertion failed (expected != actual)
3. **Failed – Element not found** – Selenium ElementNotFoundException
4. **Failed – Timeout** – WebDriverWait timeout
5. **Failed – Other** – Generic test failure
6. **Broken** – Test setup failed, database error, etc.

**Usage:** Allure report groups failures by category; helps identify common issues.

---

#### `src/test/resources/allure/environment.properties`

**Purpose:** Static fallback for Allure environment metadata.

**Overridden at runtime by AllureReportWriter during @BeforeAll.

---

#### `src/test/resources/allure/executor.json`

**Purpose:** Static fallback for Allure executor metadata.

**Overridden at runtime by AllureReportWriter during @BeforeAll.

---

#### `.github/workflows/test-automation.yml`

**Purpose:** CI/CD pipeline for GitHub Actions.

**Key Steps:**
1. Checkout code
2. Set up Java 17 + Maven
3. Install Chrome & ChromeDriver
4. Run tests: `mvn test`
5. Generate Allure report: `mvn allure:report`
6. Publish to GitHub Pages
7. Notify Slack & Email

**Triggers:**
- Push to master
- Pull request to master
- Manual trigger (workflow_dispatch)

---

#### `.github/REPO_SECRETS.md`

**Purpose:** Documents required GitHub Secrets and Variables for CI.

**Required Secrets:**
- `APP_BASE_URL` – Application URL
- `SLACK_WEBHOOK_URL` – Slack notification webhook
- `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` – Email SMTP
- `EMAIL_TO` – Email recipient list

**Required Variables:**
- `SEND_EMAIL` – true/false to enable email notifications

---

### Configuration Files

#### `pom.xml`

**Detailed breakdown:**

**Coordinates:**
```xml
<groupId>org.example</groupId>
<artifactId>QAM06_XYZ_TEST_AUTOMATION</artifactId>
<version>1.0-SNAPSHOT</version>
```

**Properties:**
```xml
<maven.compiler.source>17</maven.compiler.source>
<maven.compiler.target>17</maven.compiler.target>
<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
<selenium.version>4.15.0</selenium.version>
<junit.version>5.10.0</junit.version>
<allure.version>2.17.0</allure.version>
<log4j.version>2.21.0</log4j.version>
```

**Key Dependencies:**
| Artifact | Version | Purpose |
|----------|---------|---------|
| selenium-java | 4.15.0 | WebDriver for browser automation |
| junit-jupiter-api | 5.10.0 | JUnit 5 test framework |
| junit-jupiter-engine | 5.10.0 | JUnit 5 test runner |
| junit-jupiter-params | 5.10.0 | Parameterized tests |
| allure-junit5 | 2.17.0 | Allure report integration |
| log4j-core | 2.21.0 | Logging framework |
| javafaker | 1.0.2 | Test data generation |

**Key Plugins:**
| Plugin | Bindings | Purpose |
|--------|----------|---------|
| maven-clean-plugin | clean | Remove target directory |
| maven-antrun-plugin | initialize, test | Delete old results, copy assets |
| maven-surefire-plugin | test | Run `*Test`/`*Tests` classes |
| allure-maven | report | Generate Allure HTML report |
| maven-compiler-plugin | compile | Compile Java 17 source |

---

### Documentation Files

#### `README.md`

**Purpose:** Project overview and quick start guide for developers.

**Typically contains:**
- Project title and description
- Technologies/stack
- Prerequisites (Java 17, Maven, Chrome)
- Installation steps
- How to run tests locally
- How to view Allure report
- Project structure overview
- Contributing guidelines

---

#### `docs/PROJECT_IMPLEMENTATION.md` (this file)

**Purpose:** Comprehensive walkthrough of project structure and implementation details.

---

#### `docs/BASETEST_EXPLANATION.md`

**Purpose:** Deep dive into BaseTest class – variables, methods, lifecycle, design patterns.

**Covers:**
- Class structure and annotations
- Every variable (why it exists, how it's used)
- Every method (purpose, steps, usage)
- Complete lifecycle flow
- Integration with TestUtils
- Real-world examples

---

#### `.gitignore`

**Purpose:** Specifies files to exclude from Git repository.

**Typical contents:**
```
target/
.idea/
*.iml
.DS_Store
logs/
*.log
.env
```

Prevents build artifacts, IDE files, and logs from being committed.

---

## 3.3 Configuration and driver – `BaseTest` (setup)



All config and driver setup is kept in **BaseTest**:

- **BaseTest.Config** (static inner class)
  - Loads `config.properties` from classpath (same keys: `base.url`, `headless.mode`, `implicit.wait`, `explicit.wait`, `page.load.timeout`, `window.maximize`).
  - System properties override (e.g. `-Dbase.url`, `-Dheadless.mode` for CI).
  - Used only inside BaseTest for driver creation and navigation; BaseTest calls **SeleniumUtils.setExplicitWait()** so SeleniumUtils and page objects use the configured timeout for WebDriverWait.
- **Driver creation and quit**
  - `createDriver()` and `quitDriver(driver)` are private static methods in BaseTest (Chrome only, `CHROME_BIN` when set, headless and timeouts from Config).
- **AppUrls.java** (main)
  - Constants for URL fragments: `#/login`, `#/manager`, `#/manager/addCust`, etc. Base URL comes from BaseTest.Config.

---

### 3.4 Page objects – `pages/`

- **manager/LoginPage.java**
  - Home, Bank Manager Login, Customer Login, customer dropdown, login button.
  - Methods: `loginAsManager()`, `loginAsCustomer(name)`, `selectCustomerUserType()`, `isCustomerInDropdown(name)`, etc.
- **manager/ManagerDashboardPage.java**
  - Add Customer (name, postal code, submit, alert/error), Open Account (customer, currency, process), Customers list (delete, scroll, exists), Home/Customers buttons.
  - Methods: `addCustomer()`, `addCustomerAndGetAlertMessage()`, `createAccount()`, `createAccountAndGetAlertMessage()`, `scrollToCustomerAndDelete()`, `customerExists()`, etc.
- **customer/CustomerDashboardPage.java**
  - Account page: Deposit/Withdraw tabs, amount input, Deposit/Withdraw submit, balance, Transactions, Logout.
  - Transactions page: table rows, count.
  - Methods: `deposit(amount)`, `withdraw(amount)`, `clickTransactionsButton()`, `getBalanceAsInt()`, `getTransactionCount()`, `waitForTransactionCountAtLeast(n)`, `waitForBalanceEqualTo(n)`, etc.
  - Handles Deposit vs Withdraw form visibility and stale elements (e.g. wait for Withdraw form before typing amount).

Pages take `WebDriver` in the constructor and use `SeleniumUtils` for waits and interactions.

---

### 3.5 Utils – `utils/`

- **SeleniumUtils.java**
  - Waits: `waitForElementToBeVisible`, `waitForElementToBeClickable`, `waitForElementToBePresent`, `waitForUrlContains`, `waitUntilVisible`, `waitUntilClickable`, `waitForFirstVisible`, `waitFirstVisibleThenClearAndType` (with stale retry).
  - Actions: `waitAndClick`, `clearAndType`.
  - Alerts: `acceptAlert`, `getAlertText`.
  - Dropdown/option helpers.
- **TestDataGenerator.java**
  - Uses JavaFaker and Random.
  - Customer: `generateValidCustomerName()`, `generateInvalidCustomerNameWithNumbers()`, `generateInvalidCustomerNameWithSpecialChars()`, `generateValidPostalCode()`, `generateInvalidPostalCodeWithLetters()`, `generateCustomerTestData()` (DTO with name + postalCode). **Invalid Add Customer (parameterized):** `InvalidAddCustomerCase` (description, name, postalCode) and `invalidAddCustomerCases()` return a list of invalid cases (name with numbers, name with special chars, postal with letters) for `@ParameterizedTest` in ManagerTest.
  - Amounts: `generateValidDepositAmount()`, `generateZeroAmount()`, `generateNegativeAmount()`, `generateLargeAmount()`, `generateValidWithdrawalAmount()`.

Test data is generated per test; no hardcoded credentials in code.

---

### 3.6 Test setup – `src/test/java/org/example/setup/`

- **BaseTest.java**
  - All test classes extend this; lives in a dedicated `setup` package.
  - `@BeforeAll`: calls `AllureReportWriter.writeAllureEnvironmentAndExecutor()` (writes `environment.properties` and `executor.json` into `target/allure-results`).
  - `@BeforeEach`: creates driver, navigates to base URL.
  - `@AfterEach`: on failure, attaches error overview to Allure; then quits driver.
  - `TestWatcher`: sets `testFailed` and `lastFailure` for attachments.
  - `getTestMethodName()` supports both `test*` and `should*`-style names.

Centralizes lifecycle and Allure integration.

---

### 3.7 Test classes – `src/test/java/org/example/tests/`

- **manager/ManagerTest.java** (9 test methods; parameterized adds 3 invocations)
  - **US1 – Bank Manager**, `@Tag("manager")`, `@Tag("us1")`.
  - **AC1 Adding Customers:** addNewCustomer_withValidData; **invalidCustomerData_rejected** (one `@ParameterizedTest` with `@CsvSource` for name with numbers, name with special chars, postal with letters).
  - **AC2 Creating Accounts:** createAccount_forAddedCustomer; **openAccount_withoutCustomerSelection_showsError**; **openAccount_withoutCurrencySelection_showsError**.
  - **AC3 Deleting Accounts:** managerCanDeleteCustomer_removedFromList; managerCanDeleteCustomer_notInCustomerDropdown.
- **customer/CustomerTest.java** (12 tests)
  - **US2 – Customer Banking**, `@Tag("customer")`, `@Tag("us2")`.
  - **Account Access:** customerWithoutAccount_cannotAccessBankingActions, deletedCustomer_cannotAccessAccount.
  - **Viewing Transactions:** newAccount_emptyTransactionList, transactionList_showsCorrectType, multipleTransactions_allAppearInList, transactionList_persistsAfterReLogin.
  - **Depositing Funds:** validDeposit_updatesBalance, zeroDeposit_balanceUnchanged, negativeDeposit_balanceUnchanged.
  - **Withdrawing Money:** validWithdrawal_updatesBalance, withdrawalExceedingBalance_balanceUnchanged, withdrawExactDeposit_balanceBecomesZero.

Tests use `@DisplayName`, `@Story`, `@Severity`; BDD-style method names; shared customer created in `@BeforeEach` for customer tests. No duplicate driver creation; one driver per test method.

---

### 3.8 Test data strategy

- **Runtime generation:** `TestDataGenerator` (Faker + Random) for names, postal codes, amounts. For parameterized tests, `invalidAddCustomerCases()` supplies invalid add-customer data (description + name + postalCode) so ManagerTest uses one `@ParameterizedTest` with `@MethodSource` instead of multiple duplicate test methods.
- **No static test data files** (e.g. CSV/JSON) in the repo; all data is generated in code.
- **Config:** `config.properties` for environment (base URL, timeouts); no test data stored there.

---

### 3.9 Allure integration

- **allure.properties (project root):** `allure.results.directory=target/allure-results`, `allure.report.directory=target/allure-report`.
- **src/test/resources/allure/:**
  - **categories.json** – failure categories (Passed, Failed – Assertion, Element not found, Timeout, Other, Broken) with `matchedStatuses` and `messageRegex`; copied into `allure-results` by antrun.
  - **environment.properties** – static fallback; overwritten at runtime by `AllureReportWriter`.
  - **executor.json** – static fallback; overwritten at runtime with build name, order, URL, report URL (CI).
- **AllureReportWriter.java (test utils):** Writes current run’s environment (app URL, Java, OS, headless, etc.) and executor (name, build, URLs) into `target/allure-results` from `BaseTest` `@BeforeAll`.
- **BaseTest:** On failure, attaches error overview; Allure picks it up from results.
- **Local:** `mvn test` then `mvn allure:serve` or open `target/allure-report/index.html`.
- **CI:** Workflow runs `mvn allure:report`, prepares report for GitHub Pages (history, base path patch), publishes to `gh-pages`; report URL is set in executor.

---

### 3.10 CI pipeline – `.github/workflows/test-automation.yml`

See `docs/CI_PIPELINE.md` for a full, step-by-step explanation of the runner, jobs, and each workflow step.

- **Triggers:** push/PR to `master`, `workflow_dispatch`.
- **Job: ui-tests-xyz-bank** (ubuntu-latest, contents: write).
- **Steps (summary):**
  1. Checkout.
  2. Set up Java 17 (Temurin, Maven cache).
  3. Print target URL (from secret).
  4. Install Chrome and ChromeDriver (matching versions from Chrome for Testing).
  5. Run Selenium tests: `mvn -B -e test` with headless, base URL, Allure executor properties.
  6. Test summary: Python script reads Surefire XML, prints readable summary, exits non-zero on failures.
  7. Build Slack payload and email body (same script).
  8. Notify Slack (continue-on-error).
  9. Send Email (if `vars.SEND_EMAIL == 'true'`, using secrets for SMTP and `EMAIL_TO`).
  10. On failure: print Surefire reports.
  11. Upload Surefire artifacts.
  12. Checkout gh-pages into `gh-pages/`.
  13. Copy history into `target/allure-results/history`.
  14. Verify Allure results (at least one *-result.json or *-container.json).
  15. Build Allure report: `mvn allure:report`.
  16. Prepare report for GitHub Pages (copy from `target/allure-report` or `target/site/allure-maven-plugin`).
  17. Patch Allure report for repo subpath (base href and JS paths).
  18. Publish to gh-pages.

Secrets (e.g. APP_BASE_URL, SMTP_*, EMAIL_TO, SLACK_WEBHOOK_URL) and variable (SEND_EMAIL) are documented in `.github/REPO_SECRETS.md`.

---

## 4. Run instructions

- **Local tests:** `mvn clean test`
- **Local Allure:** `mvn allure:serve` or open `target/allure-report/index.html`
- **CI:** Push to `master` or run workflow manually; Allure report at `https://<owner>.github.io/XYZ-TEST-AUTOMATION/`

---

## 5. Summary

| Layer           | Responsibility                                      |
|----------------|-----------------------------------------------------|
| **POM**        | Dependencies, Surefire, Allure, antrun (clean + copy) |
| **Config**     | Base URL, timeouts, headless                        |
| **Driver**     | Chrome only, CI-friendly binary and options         |
| **Pages**      | POM for login, manager, customer/account/transactions |
| **Utils**      | Waits, click, type, alerts, test data               |
| **Setup**      | BaseTest: driver lifecycle, Allure env/executor, failure attachments |
| **Tests**      | 17 tests across Manager (5) and Customer (12), tagged and named for US1/US2 |
| **Allure**     | Categories, env/executor at runtime, report local + CI (gh-pages) |
| **CI**         | Java, Chrome, test, report, publish, Slack/email, Surefire upload |

This structure keeps configuration, driver, pages, and test data separate from test classes and uses a single CI workflow for test execution and Allure reporting.
