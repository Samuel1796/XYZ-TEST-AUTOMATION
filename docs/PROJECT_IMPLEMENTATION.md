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

### 3.2 Configuration – `config/`

- **ConfigManager.java**
  - Loads `config.properties` from classpath.
  - Exposes: `getBaseUrl()`, `isHeadlessMode()`, `getImplicitWait()`, `getExplicitWait()`, `getPageLoadTimeout()`, `shouldMaximizeWindow()`, `getSeleniumRemoteUrl()`.
  - System properties override config (e.g. `-Dbase.url`, `-Dheadless.mode` for CI).
- **AppUrls.java**
  - Constants for URL fragments: `#/login`, `#/manager`, `#/manager/addCust`, `#/manager/openAccount`, `#/manager/list`, `#/customer`, `#/account`, `#/listTx`.

Config is centralized and CI-friendly.

---

### 3.3 Driver – `driver/`

- **DriverManager.java**
  - Creates ChromeDriver only (no other browsers).
  - Uses `CHROME_BIN` when set (CI); otherwise default Chrome.
  - Common options: `--disable-dev-shm-usage`, `--no-sandbox`, `--disable-gpu`; when headless, adds headless-specific flags.
  - `createDriver()` and `quitDriver(driver)` are the single entry points for driver lifecycle.

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
