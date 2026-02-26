# BaseTest Class - Detailed Explanation

## Overview

`BaseTest` is the **foundation class for all UI test cases** in the XYZ-TEST-AUTOMATION framework. It encapsulates configuration management, WebDriver lifecycle, page object initialization, and Allure reporting integration. Every test class in the project extends `BaseTest` to inherit these capabilities.

---

## Table of Contents

1. [Class Structure](#class-structure)
2. [Variables Explained](#variables-explained)
3. [Inner Classes](#inner-classes)
4. [Methods Explained](#methods-explained)
5. [Lifecycle Flow](#lifecycle-flow)
6. [Integration with TestUtils](#integration-with-testutils)

---

## Class Structure

```java
@ExtendWith(TestUtils.BaseTestWatcher.class)
public class BaseTest implements TestUtils.TestFailureCapture
```

### `@ExtendWith(TestUtils.BaseTestWatcher.class)`

**Purpose:** Registers a JUnit 5 extension (TestWatcher) that monitors test execution.

**Why:**
- Automatically captures test failures during execution
- Sets `testFailed` and `lastFailure` flags so the tearDown method knows to attach error details
- Eliminates the need to manually check assertion results

**How it works:**
- Before each test runs, the watcher is ready to listen
- If a test fails (throws exception or assertion error), `testFailed()` is triggered
- The watcher extracts the test instance and calls `setTestFailed(true)` and `setLastFailure(cause)`

### `implements TestUtils.TestFailureCapture`

**Purpose:** Makes BaseTest compatible with the TestWatcher.

**Why:**
- The `TestWatcher` looks for objects implementing `TestFailureCapture` interface
- This interface defines methods to set failure state: `setTestFailed()` and `setLastFailure()`
- Provides a clean contract for test failure communication

---

## Variables Explained

### 1. `protected static final Logger logger`

```java
protected static final Logger logger = LogManager.getLogger(BaseTest.class);
```

**Type:** `Logger` (Apache Log4j2)

**Purpose:** Records test execution events and debugging information.

**Why static and final:**
- **static:** Shared across all instances; no need for multiple logger instances
- **final:** Cannot be reassigned; logger is a fixed dependency

**Used for:**
- `logger.info()` - Records test start/finish and navigation events
- `logger.error()` - Records teardown errors and configuration issues

**Example usage in code:**
```java
logger.info("Test started: {}", TestUtils.getTestMethodName());
logger.error("Error during teardown: {}", e.getMessage(), e);
```

---

### 2. `protected WebDriver driver`

```java
protected WebDriver driver;
```

**Type:** `WebDriver` (Selenium)

**Purpose:** The main browser automation object for interacting with the web application.

**Why protected:**
- Subclasses (actual test classes) need access to control the browser
- Allows tests to find elements, click buttons, enter text, verify page state, etc.

**Lifecycle:**
- **Created:** In `setUp()` via `createDriver()`
- **Used:** Throughout test execution for page interactions
- **Destroyed:** In `tearDown()` via `quitDriver(driver)`

**Example usage in test:**
```java
driver.findElement(By.id("username")).sendKeys("admin");
driver.findElement(By.id("password")).sendKeys("password");
```

---

### 3. `protected LoginPage loginPage`

```java
protected LoginPage loginPage;
```

**Type:** `LoginPage` (Custom Page Object)

**Purpose:** Represents the login page of the banking application.

**Why protected:**
- Test classes need to access page object methods
- Encapsulates all login-related element locators and actions

**Lifecycle:**
- **Created:** In `setUp()` after driver is created
- **Used:** In tests that require login functionality
- **Destroyed:** Implicitly when driver quits

**Example usage in test:**
```java
loginPage.loginAsCustomer("username", "password");
loginPage.verifyLoginSuccess();
```

---

### 4. `protected ManagerDashboardPage managerPage`

```java
protected ManagerDashboardPage managerPage;
```

**Type:** `ManagerDashboardPage` (Custom Page Object)

**Purpose:** Represents the manager dashboard page with actions like "Add Customer" and "Open Account".

**Why protected:**
- Test classes need to perform manager-specific operations
- Contains locators and methods for manager-only features

**Lifecycle:**
- **Created:** In `setUp()` after driver is created
- **Used:** In tests where manager operations are required
- **Destroyed:** Implicitly when driver quits

**Example usage in test:**
```java
managerPage.clickAddCustomer();
managerPage.fillCustomerDetails("John", "Doe", 25);
```

---

### 5. `protected CustomerDashboardPage customerPage`

```java
protected CustomerDashboardPage customerPage;
```

**Type:** `CustomerDashboardPage` (Custom Page Object)

**Purpose:** Represents the customer account dashboard with actions like "Deposit", "Withdraw", "Transactions".

**Why protected:**
- Test classes need to perform customer transactions
- Contains locators and methods for customer-only features

**Lifecycle:**
- **Created:** In `setUp()` after driver is created
- **Used:** In tests where customer operations are required
- **Destroyed:** Implicitly when driver quits

**Example usage in test:**
```java
customerPage.deposit(1000);
customerPage.verifyBalance(1500);
```

---

### 6. `protected boolean testFailed`

```java
protected boolean testFailed = false;
```

**Type:** `boolean` (primitive)

**Purpose:** Tracks whether the current test failed.

**Why protected:**
- Subclasses might need to check test status
- Initialized to `false` at the start of each test

**Set by:** `TestUtils.BaseTestWatcher` when a test fails
- When test throws exception → watcher calls `setTestFailed(true)`

**Used in:** `tearDown()` to decide whether to attach error overview to Allure
```java
if (testFailed && lastFailure != null) {
    TestUtils.attachErrorOverviewToAllure(lastFailure);
}
```

**Default value:** `false` (reset in setUp)
- Ensures each test starts with a clean state

---

### 7. `protected Throwable lastFailure`

```java
protected Throwable lastFailure;
```

**Type:** `Throwable` (any exception/error)

**Purpose:** Stores the exception that caused the test to fail.

**Why protected:**
- Subclasses might need to inspect the failure reason
- Allows tests to perform custom failure handling if needed

**Set by:** `TestUtils.BaseTestWatcher` when a test fails
- When test throws exception → watcher calls `setLastFailure(cause)`

**Used in:** `tearDown()` to create error overview attachment
```java
if (testFailed && lastFailure != null) {
    // Pass to TestUtils to attach error details to Allure report
    TestUtils.attachErrorOverviewToAllure(lastFailure);
}
```

**Default value:** `null` (reset in setUp)
- Ensures no stale failure information from previous tests

---

## Inner Classes

### Config Class

```java
public static final class Config {
    private static final Properties properties = new Properties();

    static {
        // Load config.properties file
    }

    // getRequired(), getOptional(), and getter methods
}
```

#### Purpose

Manages all configuration values used throughout the framework. **Single source of truth** for:
- Base URL
- Browser settings (headless mode, window maximize)
- Wait times (implicit, explicit, page load)

#### Why static final class

- **static:** No need to instantiate; accessed via `Config.getBaseUrl()`
- **final:** Cannot be extended; prevents accidental overrides
- **inner class:** Logically grouped with BaseTest; tightly coupled

#### Properties loaded

From `src/main/resources/config.properties`:
```properties
base.url=https://www.globalsqa.com/angularJs-protractor/BankingProject/
headless.mode=false
window.maximize=true
implicit.wait=5
explicit.wait=10
page.load.timeout=20
```

#### Override mechanism

System properties override config file values (CI/CD friendly):
```bash
mvn test -Dbase.url=https://staging.example.com
```

---

#### Inner Methods of Config

##### `getRequired(String key)`

```java
private static String getRequired(String key)
```

**Purpose:** Retrieves mandatory configuration properties.

**Why separate method:**
- Enforces that required properties must exist
- Fails fast with clear error message if missing
- Prevents silent failures with default values

**Logic:**
1. Check system properties first (highest priority)
2. If not found, check config.properties file
3. If still not found, throw `ConfigurationException`

**Example:**
```java
public static String getBaseUrl() {
    return getRequired("base.url"); // Fails if not set
}
```

**Error thrown:**
```
ConfigurationException: Required property 'base.url' is missing from
config.properties and not set as system property
```

---

##### `getOptional(String key, String defaultValue)`

```java
private static String getOptional(String key, String defaultValue)
```

**Purpose:** Retrieves optional configuration with fallback to default.

**Why separate method:**
- Provides sensible defaults for non-critical settings
- Allows flexibility without breaking tests
- Still respects system property overrides

**Logic:**
1. Check system properties first
2. If not found, check config.properties
3. If not found, use provided defaultValue

**Example:**
```java
public static boolean isHeadlessMode() {
    return Boolean.parseBoolean(getOptional("headless.mode", "false"));
}
```

---

##### `getBaseUrl()`

```java
public static String getBaseUrl()
```

**Returns:** The application URL to navigate to.

**Why required:** All tests must know which app to test.

**Example value:** `https://www.globalsqa.com/angularJs-protractor/BankingProject/`

**Usage in setUp:**
```java
driver.navigate().to(Config.getBaseUrl());
```

---

##### `isHeadlessMode()`

```java
public static boolean isHeadlessMode()
```

**Returns:** Whether to run Chrome without GUI.

**Why configurable:**
- Local development: `false` (see browser)
- CI/CD pipeline: `true` (faster, no display needed)

**Usage in createDriver:**
```java
if (headless) {
    options.addArguments("--headless=new", "--no-sandbox");
}
```

---

##### `getImplicitWait()`

```java
public static long getImplicitWait()
```

**Returns:** Implicit wait timeout in seconds.

**What is implicit wait:**
- Maximum time Selenium waits when finding elements
- Default: 5 seconds

**Why configurable:**
- Slow networks need longer waits
- Fast environments can use shorter waits

**Error handling:**
```java
try {
    return Long.parseLong(getOptional("implicit.wait", "5"));
} catch (NumberFormatException e) {
    throw new TestUtils.ConfigurationException(
        "Invalid value for 'implicit.wait': must be a valid number", e
    );
}
```

---

##### `getExplicitWait()`

```java
public static long getExplicitWait()
```

**Returns:** Explicit wait timeout in seconds.

**What is explicit wait:**
- Maximum time to wait for specific conditions (element visible, clickable, etc.)
- Default: 10 seconds

**Why separate from implicit:**
- Different use cases require different timeouts
- More fine-grained control

**Usage in SeleniumUtils:**
```java
new WebDriverWait(driver, Duration.ofSeconds(Config.getExplicitWait()))
    .until(ExpectedConditions.elementToBeClickable(element));
```

---

##### `getPageLoadTimeout()`

```java
public static long getPageLoadTimeout()
```

**Returns:** Page load timeout in seconds.

**What it controls:**
- Maximum time to wait for a page to fully load
- Default: 20 seconds

**Why separate:**
- Page loads can take longer than element waits
- Heavy pages need more time

**Usage in createDriver:**
```java
d.manage().timeouts().pageLoadTimeout(
    Duration.ofSeconds(Config.getPageLoadTimeout())
);
```

---

##### `shouldMaximizeWindow()`

```java
public static boolean shouldMaximizeWindow()
```

**Returns:** Whether to maximize the browser window.

**Why configurable:**
- Local testing: `true` (see full page)
- Headless mode: ignored (no window to maximize)
- CI/CD: may be `false` (window size doesn't matter)

**Usage in createDriver:**
```java
if (!headless && Config.shouldMaximizeWindow()) {
    d.manage().window().maximize();
}
```

---

## Methods Explained

### Interface Implementation Methods

#### `setTestFailed(boolean failed)`

```java
@Override
public void setTestFailed(boolean failed) {
    this.testFailed = failed;
}
```

**Purpose:** Called by TestWatcher to record test failure.

**Why needed:**
- Allows TestWatcher (external entity) to modify test state
- Decouples TestWatcher from directly accessing fields

**Called by:** `TestUtils.BaseTestWatcher.testFailed()`

---

#### `setLastFailure(Throwable cause)`

```java
@Override
public void setLastFailure(Throwable cause) {
    this.lastFailure = cause;
}
```

**Purpose:** Called by TestWatcher to store the exception that failed the test.

**Why needed:**
- Preserves exception details for error overview attachment
- TestWatcher captures exception immediately; tearDown uses it later

**Called by:** `TestUtils.BaseTestWatcher.testFailed()`

---

### Lifecycle Methods

#### `createDriver()`

```java
private static WebDriver createDriver()
```

**Purpose:** Creates and configures a Chrome WebDriver instance.

**Why private and static:**
- **private:** Only BaseTest should create drivers (encapsulation)
- **static:** No instance needed; pure driver factory

**Steps:**

1. **Read configuration:**
   ```java
   boolean headless = Config.isHeadlessMode();
   ```

2. **Create ChromeOptions:**
   ```java
   ChromeOptions options = new ChromeOptions();
   ```

3. **Configure headless mode (optional):**
   ```java
   if (headless) {
       options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
   }
   ```
   - `--headless=new`: Modern headless mode (faster, more stable)
   - `--no-sandbox`: Required in CI/Docker environments
   - `--disable-dev-shm-usage`: Prevents /dev/shm memory issues

4. **Use custom Chrome binary (if set):**
   ```java
   String chromeBin = System.getenv("CHROME_BIN");
   if (chromeBin != null && !chromeBin.isEmpty()) {
       options.setBinary(chromeBin);
   }
   ```
   - Allows CI/Docker to specify Chrome location
   - Useful when Chrome is not in default system PATH

5. **Create WebDriver:**
   ```java
   WebDriver d = new ChromeDriver(options);
   ```

6. **Set timeouts:**
   ```java
   d.manage().timeouts().implicitlyWait(Duration.ofSeconds(Config.getImplicitWait()));
   d.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.getPageLoadTimeout()));
   ```

7. **Maximize window (optional):**
   ```java
   if (!headless && Config.shouldMaximizeWindow()) {
       d.manage().window().maximize();
   }
   ```

8. **Return driver:**
   ```java
   return d;
   ```

**Why ChromeOptions are important:**
- Ensures consistent browser behavior across environments
- Prevents common issues (sandbox errors, memory issues, etc.)
- Allows headless testing in CI/CD

---

#### `quitDriver(WebDriver d)`

```java
private static void quitDriver(WebDriver d)
```

**Purpose:** Closes the browser and cleans up WebDriver resources.

**Why private and static:**
- **private:** Only BaseTest should close drivers
- **static:** No instance needed; pure cleanup utility

**Logic:**
```java
if (d != null) {
    d.quit();
}
```

**Why null check:**
- Driver might be `null` if `setUp()` failed
- Prevents `NullPointerException` in tearDown

**What `.quit()` does:**
- Closes all browser windows
- Terminates ChromeDriver process
- Releases all allocated memory
- Ensures no browser processes leak

---

#### `initConfigAndAllure()`

```java
@BeforeAll
static void initConfigAndAllure()
```

**Purpose:** One-time setup before ANY tests run in the class.

**Why `@BeforeAll`:**
- Runs once per test class, not per test method
- Must be `static` (JUnit requirement)
- Perfect for expensive one-time operations

**Steps:**

1. **Set explicit wait globally:**
   ```java
   SeleniumUtils.setExplicitWait(Config.getExplicitWait());
   ```
   - Makes explicit wait available to all page objects
   - Avoids hardcoding wait times

2. **Write Allure environment and executor info:**
   ```java
   AllureReportWriter.writeAllureEnvironmentAndExecutor(
       Config.getBaseUrl(),
       Config.isHeadlessMode()
   );
   ```
   - Creates `environment.properties` (base URL, headless flag)
   - Creates `executor.json` (who ran tests, when, etc.)
   - Used in Allure report generation

---

#### `setUp()`

```java
@BeforeEach
public void setUp()
```

**Purpose:** Prepares the test environment before EACH test method runs.

**Why `@BeforeEach`:**
- Runs before every single test
- Each test gets a fresh, clean state
- No test data or state leaks between tests

**Steps:**

1. **Reset failure flags:**
   ```java
   testFailed = false;
   lastFailure = null;
   ```
   - Ensures clean state from previous test
   - TestWatcher will set these if test fails

2. **Log test start:**
   ```java
   logger.info("Test started: {}", TestUtils.getTestMethodName());
   ```
   - Helps track test execution in logs
   - Useful for debugging and progress monitoring

3. **Create WebDriver:**
   ```java
   driver = createDriver();
   ```
   - Fresh browser instance for this test
   - Prevents state pollution from previous test

4. **Navigate to base URL:**
   ```java
   driver.navigate().to(Config.getBaseUrl());
   logger.info("Navigated to base URL: {}", Config.getBaseUrl());
   ```
   - Ensures test starts from known URL
   - Logs URL for verification

5. **Initialize page objects:**
   ```java
   loginPage = new LoginPage(driver);
   managerPage = new ManagerDashboardPage(driver);
   customerPage = new CustomerDashboardPage(driver);
   ```
   - Each test has fresh page object instances
   - Page objects now ready to use in test method

**Why this approach:**
- ✅ Isolation: Tests don't interfere with each other
- ✅ Repeatability: Each test runs in identical state
- ✅ Debugging: Known starting conditions
- ✅ Maintainability: Common setup in one place

---

#### `tearDown()`

```java
@AfterEach
public void tearDown()
```

**Purpose:** Cleans up after EACH test, regardless of pass/fail.

**Why `@AfterEach`:**
- Runs after every test
- Ensures cleanup even if test fails (finally block)
- Must free resources (browser, memory, etc.)

**Logic:**

1. **Check driver exists:**
   ```java
   if (driver != null) {
   ```
   - Driver might be null if setUp failed
   - Prevents errors in cleanup

2. **Try block - Attach test artifacts:**
   ```java
   try {
       TestUtils.attachScreenshot(driver);
   ```
   - Captures final state screenshot
   - Attached to Allure report
   - Helps visualize test failure

3. **Attach error overview (if failed):**
   ```java
   if (testFailed && lastFailure != null) {
       TestUtils.attachErrorOverviewToAllure(lastFailure);
   }
   ```
   - Only if test actually failed
   - Includes test name, error type, message, stack trace
   - Attached to Allure report as text file

4. **Catch block - Log errors:**
   ```java
   } catch (Exception e) {
       logger.error("Error during teardown: {}", e.getMessage(), e);
   }
   ```
   - Catches unexpected errors in cleanup
   - Logs them instead of failing
   - Teardown continues to finally block

5. **Finally block - Close driver:**
   ```java
   } finally {
       quitDriver(driver);
       logger.info("Test finished: {}", TestUtils.getTestMethodName());
   }
   ```
   - **Always** closes browser
   - Logs test completion
   - Prevents resource leaks

**Why try-catch-finally:**
- ✅ Attachments added even on failure
- ✅ Errors during attachment don't stop cleanup
- ✅ Driver always closed (finally block)
- ✅ No resource leaks

---

## Lifecycle Flow

### Complete Test Execution Sequence

```
1. Test Class Starts
   ↓
2. @BeforeAll: initConfigAndAllure()
   - Load config from properties
   - Set up Allure environment
   ↓
3. Test Method Starts
   ↓
4. @BeforeEach: setUp()
   - Reset testFailed = false, lastFailure = null
   - Create WebDriver
   - Navigate to base URL
   - Create page objects
   ↓
5. Test Method Executes
   - Test code runs (interact with pages, assert, etc.)
   ↓
   Test fails? → TestWatcher.testFailed() triggered
                 - Sets testFailed = true
                 - Sets lastFailure = exception
   ↓
6. @AfterEach: tearDown()
   - Attach screenshot
   - If failed: Attach error overview
   - Close WebDriver
   ↓
7. Allure Report Updated
   - Test result (pass/fail)
   - Screenshots and attachments
   ↓
8. Next Test Method (back to step 3)
   OR
   Test Class Ends (back to step 1)
```

---

## Integration with TestUtils

### How BaseTest uses TestUtils

#### 1. BaseTestWatcher

```java
@ExtendWith(TestUtils.BaseTestWatcher.class)
public class BaseTest implements TestUtils.TestFailureCapture
```

- **TestWatcher listens** for test failures
- **Calls `setTestFailed(true)`** when test fails
- **Calls `setLastFailure(cause)`** to store exception
- **No manual exception handling needed** in test classes

#### 2. Utility Methods

**In setUp():**
```java
logger.info("Test started: {}", TestUtils.getTestMethodName());
```

**In tearDown():**
```java
TestUtils.attachScreenshot(driver);
TestUtils.attachErrorOverviewToAllure(lastFailure);
logger.info("Test finished: {}", TestUtils.getTestMethodName());
```

#### 3. ConfigurationException

```java
throw new TestUtils.ConfigurationException(
    "Required property '" + key + "' is missing..."
);
```

- Custom exception for configuration errors
- Distinguishes config issues from test failures
- Provides clear error messages

---

## Real-World Example

### Sample Test Class

```java
public class CustomerLoginTest extends BaseTest {

    @Test
    public void testCustomerCanLogin() {
        // setUp() has already:
        // - Created driver
        // - Navigated to baseURL
        // - Created loginPage object

        // Use loginPage (inherited from BaseTest)
        loginPage.loginAsCustomer("john", "1234");

        // Use customerPage (inherited from BaseTest)
        customerPage.verifyAccountNumber("1000");

        // tearDown() will automatically:
        // - Attach screenshot
        // - Close browser
        // - If fails: attach error overview
    }
}
```

### Behind the scenes:

1. **Class initialization:**
   - `initConfigAndAllure()` runs once
   - Config loaded, Allure env written

2. **Before test:**
   - `setUp()` creates fresh driver, navigates, creates page objects

3. **During test:**
   - Test interacts with pages using page objects

4. **If test fails:**
   - Exception thrown
   - TestWatcher catches it, sets `testFailed = true`, `lastFailure = exception`

5. **After test:**
   - `tearDown()` runs
   - Attaches screenshot
   - Attaches error overview with exception details
   - Closes browser

---

## Summary Table

| Component | Type | Scope | Purpose |
|-----------|------|-------|---------|
| `logger` | static final | Class-level | Log test execution events |
| `driver` | instance | Test-level | Browser automation |
| `loginPage` | instance | Test-level | Login page interactions |
| `managerPage` | instance | Test-level | Manager operations |
| `customerPage` | instance | Test-level | Customer operations |
| `testFailed` | boolean flag | Test-level | Track failure status |
| `lastFailure` | Throwable | Test-level | Store exception details |
| `Config` | static class | Class-level | Configuration management |
| `createDriver()` | static method | Utility | Create WebDriver |
| `quitDriver()` | static method | Utility | Close WebDriver |
| `initConfigAndAllure()` | static method | @BeforeAll | One-time setup |
| `setUp()` | instance method | @BeforeEach | Pre-test setup |
| `tearDown()` | instance method | @AfterEach | Post-test cleanup |

---

## Key Design Principles

### 1. **Separation of Concerns**
- Configuration logic isolated in `Config` class
- Utility functions extracted to `TestUtils`
- Test methods inherit setup/teardown from BaseTest

### 2. **Fail-Fast Philosophy**
- Required config properties throw exceptions immediately
- Bad configuration values caught with clear error messages
- Tests fail early with actionable information

### 3. **Test Isolation**
- Fresh driver instance per test
- Failure flags reset per test
- No state leakage between tests

### 4. **Resource Management**
- Always close browser (finally block)
- Null checks prevent errors
- Proper exception chaining for debugging

### 5. **Reporting Integration**
- Automatic screenshot capture
- Error overview with context
- Allure environment metadata

---

## Common Pitfalls & Solutions

### Pitfall: Test Passes But Browser Not Closed

**Cause:** Exception in tearDown prevents driver.quit()

**Solution:** Try-catch-finally pattern ensures finally block always runs

### Pitfall: Missing Configuration Value

**Cause:** Using default value silently masks misconfiguration

**Solution:** `getRequired()` throws exception for required properties

### Pitfall: Tests Affect Each Other

**Cause:** Reusing driver instance across tests

**Solution:** Fresh driver created in setUp(), closed in tearDown()

### Pitfall: Hard to Debug Failed Tests

**Cause:** No screenshot or error context in report

**Solution:** Automatic screenshot and error overview attachment

---

## Conclusion

`BaseTest` is a **production-grade test foundation** that provides:
- ✅ Automatic setup/teardown management
- ✅ Flexible configuration system
- ✅ Failure tracking and reporting
- ✅ Test isolation
- ✅ Resource cleanup
- ✅ Allure integration

Every test in the framework inherits these capabilities, ensuring consistency, reliability, and maintainability across the entire test suite.

