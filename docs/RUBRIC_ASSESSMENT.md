# XYZ Bank Test Automation – Rubric Assessment

This document critiques the project and assigns scores using the provided rubrics.

---

## Rubric summary

| METRIC | CRITERIA | SCORE | MAX |
|--------|----------|-------|-----|
| **Set up** | Folder structure (POM), Test structure, Test data, GitHub Actions CI pipeline | **10** | 10 |
| **Test Automation** | Test design, coverage, page objects, stability, maintainability | **69** | 70 |
| **Allure Reports** | Generate and publish test report using Allure | **20** | 20 |
| **TOTAL** | | **99** | **100** |

---

## 1. Set up (10 / 10)

### Folder structure (POM)

- **Maven layout:** Standard `src/main/java`, `src/test/java`, `src/main/resources`, `src/test/resources`; clear separation of production and test code.
- **POM:** Single `pom.xml` with defined artifact, Java 17, managed versions for Selenium, JUnit 5, Allure, Log4j, Faker. Plugins: clean, antrun (Allure prep), Surefire, allure-maven, compiler. Allure results/report directories aligned for local and CI.
- **Packages:** `config`, `driver`, `pages` (manager/customer), `utils` under main; `setup`, `tests` (manager/customer), `utils` under test. Base test in dedicated `setup` package (not mixed with tests).
- **Verdict:** Structure is clear and appropriate for the scope. **Full marks.**

### Test structure

- **Base test:** `BaseTest` in `org.example.setup`; single place for driver creation, navigation, tearDown, failure handling (screenshot + error attachment), and Allure env/executor write.
- **Test classes:** `ManagerTest` and `CustomerTest` extend `BaseTest`; nested by acceptance criteria; consistent use of `@DisplayName`, `@Story`, `@Tag`, `@Severity`.
- **Verdict:** Consistent, maintainable test structure. **Full marks.**

### Test data

- **TestDataGenerator:** Centralized utility using JavaFaker and Random for customer names (valid/invalid), postal codes (valid/invalid), and amounts (deposit, withdrawal, zero, negative, large). No hardcoded credentials; data generated per test.
- **Config:** `config.properties` for environment (base URL, timeouts, headless, screenshots); overridable via system properties in CI.
- **Verdict:** Test data is managed in one place and is CI-friendly. **Full marks.**

### GitHub Actions CI pipeline

- **Workflow:** Single job for checkout, Java 17, Chrome/ChromeDriver (matching versions from Chrome for Testing), Maven test with headless and Allure executor props, test summary, Slack payload + email body, Slack notify (continue-on-error), optional email (variable SEND_EMAIL), Surefire upload, Allure history merge, report build, GitHub Pages publish with base-path patch. Secrets documented in `REPO_SECRETS.md`.
- **Verdict:** End-to-end CI with test run, reporting, and optional notifications. **Full marks.**

**Set up total: 10 / 10**

---

## 2. Test Automation (69 / 70)

### Strengths

- **Coverage of user stories:** US1 (Manager) and US2 (Customer) are both covered with multiple tests (5 + 12 = 17 tests) mapped to acceptance criteria (Adding Customers, Creating Accounts, Viewing Transactions, Depositing, Withdrawing, etc.).
- **Page Object Model:** Login, Manager dashboard, and Customer dashboard are modelled with clear methods and use of SeleniumUtils for waits and actions; Deposit vs Withdraw form handling and stale-element handling are addressed.
- **Assertions:** Meaningful assertions (balance change, transaction count, alert/error messages, visibility of buttons); failure messages include context (e.g. expected vs actual balance).
- **Stability:** Explicit waits, “first visible” and “wait then clear/type” for duplicate locators, wait for balance/transaction count before asserting, TestWatcher for failure attachment.
- **Tags and naming:** `@Tag("manager")`, `@Tag("us1")`, `@Tag("customer")`, `@Tag("us2")`; method names are descriptive (e.g. `validWithdrawal_updatesBalance`, `multipleTransactions_allAppearInList`).

### Critiques and deductions

1. **AC3 (Delete accounts) – addressed**  
   **Deleting Accounts** nested class in ManagerTest: `managerCanDeleteCustomer_removedFromList()` (add customer, create account, delete, verify not in table) and `managerCanDeleteCustomer_notInCustomerDropdown()` (delete then verify customer not in login dropdown). AC3 coverage is now explicit.

2. **Open Account negative/edge – addressed**  
   **Creating Accounts** now includes: `openAccount_withoutCustomerSelection_showsError()` (click Process without selecting customer; assert alert or page error) and `openAccount_withoutCurrencySelection_showsError()` (select customer but not currency, click Process; assert alert or page error). Uses `SeleniumUtils.getAlertTextIfPresent()` for short-timeout alert handling.

3. **Parameterized tests – addressed**  
   Invalid Add Customer cases are driven by one `@ParameterizedTest` with `@MethodSource("invalidAddCustomerData")`. Data is provided by **TestDataGenerator.invalidAddCustomerCases()**, which returns a list of `InvalidAddCustomerCase` (description, name, postalCode) for: name with numbers, name with special characters, postal code with letters. The test method source maps these to JUnit `Arguments`. Single test method asserts error message; adding new invalid cases only requires updating TestDataGenerator.

4. **Single browser (‑1)**  
   Only Chrome is supported. For a learning/single-target project this is fine; small deduction.

**Test Automation total: 69 / 70**

---

## 3. Allure Reports (20 / 20)

### Criteria: Generate a test report using Allure reports

- **Allure integration:** `allure-junit5` and `allure-java-commons` in POM; Surefire configured with `allure.results.directory`; allure-maven plugin with `resultsDirectory` and `reportDirectory`; report generated in `test` phase and on demand via `mvn allure:report` / `mvn allure:serve`.
- **Results:** Tests write Allure results to `target/allure-results`; categories, environment, and executor are present (categories.json copied; environment and executor written at runtime by `AllureReportWriter` with app URL, Java, OS, headless, build name/URL/report URL in CI).
- **Report content:** Suites, test names, status, steps (Allure `@Step` on page and utils), failure details, attachments (error overview, failure screenshot), categories, environment, executor.
- **Local:** Report generated to `target/allure-report`; view via `mvn allure:serve` or by opening `index.html`.
- **CI:** Report built after tests, prepared for GitHub Pages (history, base-path patch for repo subpath), published to `gh-pages`; executor includes link to run and link to published report URL.
- **Verdict:** Allure is correctly set up, reports are generated and published, and include the expected widgets and metadata. **Full marks.**

**Allure Reports total: 20 / 20**

---

## 4. Final score and summary

| METRIC | SCORE | MAX |
|--------|-------|-----|
| Set up (Folder structure, POM, Test, Test data, GitHub Actions) | 10 | 10 |
| Test Automation | 69 | 70 |
| Allure Reports | 20 | 20 |
| **TOTAL** | **99** | **100** |

**Summary:** The project has a clear structure, a single coherent POM, a dedicated test setup and test data strategy, and a complete CI pipeline. Test automation covers both user stories with stable, readable tests and good use of page objects and waits. **AC3 (Deleting Accounts)** is covered by dedicated ManagerTests; **Open Account** has negative tests for missing customer/currency selection; **invalid Add Customer** cases use a single `@ParameterizedTest` fed by **TestDataGenerator.invalidAddCustomerCases()**. Allure reports are generated locally and in CI and published to GitHub Pages with environment and executor populated. The only remaining deduction is Chrome-only support (‑1). Overall the work meets the intent of the rubrics.
