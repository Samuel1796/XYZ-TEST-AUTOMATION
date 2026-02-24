# XYZ Bank Test Automation

Tests for [XYZ Bank (GlobalSQA)](https://www.globalsqa.com/angularJs-protractor/BankingProject/#/login).

## App URL structure

| View | URL | Description |
|------|-----|-------------|
| Home / Login | `#/login` | Customer Login, Bank Manager Login, Home |
| Bank Manager Login | `#/login` | Same page – click "Bank Manager Login" |
| Add Customer | `#/manager/addCust` | First Name, Last Name, Postal Code, Add Customer submit |
| Open Account | `#/manager/openAccount` | Customer dropdown, Currency dropdown, Process |
| Customers List | `#/manager/list` | Table of customers, Delete buttons |
| Customer Login | `#/customer` | Dropdown to select customer, Login button |
| Customer Account | `#/account` | Balance, Deposit, Withdraw, Transactions (form changes on Deposit/Withdraw) |
| Transactions | `#/listTx` | Transaction history table |

## User stories & test mapping

| User story | Test class | Acceptance criteria (nested groups) |
|------------|------------|-------------------------------------|
| **US1:** Bank Manager – add customers, create accounts, delete accounts | `ManagerTest` | AC1 Adding Customers, AC2 Creating Accounts, AC3 Deleting Accounts |
| **US2:** Customer – view transactions, deposit funds, withdraw money | `CustomerTest` | AC1 Viewing Transactions, AC2 Depositing Funds, AC3 Withdrawing Money, AC4 Transaction Security |

Tests use `@DisplayName` and `@Story` aligned to these AC. Tags: `us1`, `us2`, `adding_customers`, `creating_accounts`, `deleting_accounts`, `viewing_transactions`, `depositing_funds`, `withdrawing_money`, `transaction_security`, `smoke`.

## Requirements

- **Java 17**
- **Chrome** and **ChromeDriver** (ChromeDriver must be on PATH or set `webdriver.chrome.driver`)
- **Maven** (or run from IDE)

## Project structure

```
src/main/java/org/example/
├── config/
│   ├── ConfigManager.java      # Loads config from classpath (base URL, timeouts, headless, etc.)
│   └── AppUrls.java            # URL fragments (#/login, #/manager/..., #/customer, ...)
├── driver/
│   └── DriverManager.java      # Chrome only: creates/quits ChromeDriver (headless support for CI)
├── pages/
│   ├── manager/
│   │   ├── LoginPage.java      # Home, Customer/Manager login, user select
│   │   └── ManagerDashboardPage.java
│   └── customer/
│       └── CustomerDashboardPage.java
└── utils/
    ├── SeleniumUtils.java      # Waits, click, clearAndType, alerts, URL/dropdown helpers
    └── TestDataGenerator.java  # Test data (Faker): names, postal codes, amounts

src/test/java/org/example/
├── setup/
│   └── BaseTest.java            # Driver setup, navigate to base URL, tearDown (error attachment on failure)
├── tests/
    ├── manager/
    │   └── ManagerTest.java
    └── customer/
        └── CustomerTest.java
```

- **BaseTest** (in `setup` package): All UI tests extend this; driver creation, navigation, and tearDown (error overview on failure, quit) are centralised here. Kept in a dedicated setup package (not mixed with tests).
- **Page objects**: Take `WebDriver` in the constructor and use `SeleniumUtils` for waits, clicks, and typing.
- **Driver**: Chrome only via `DriverManager`. ChromeDriver must be on PATH (or set `webdriver.chrome.driver`); CI uses Chrome for Testing for a matching Chrome/ChromeDriver pair.

## Run tests

```bash
mvn clean test
```

### Run tests in Docker (local)

Use Selenium Standalone Chrome in Docker so you don’t need Chrome/ChromeDriver installed on your machine.

1. **Start the container** (from the project root):

   ```bash
   docker-compose up -d
   ```

   Or with plain Docker:

   ```bash
   docker run -d -p 4444:4444 -p 7900:7900 --shm-size=2g --name xyz-selenium selenium/standalone-chrome:latest
   ```

2. **Run tests** with the remote WebDriver URL:

   ```bash
   mvn test -Dselenium.remote.url=http://localhost:4444/wd/hub
   ```

3. **Stop the container** when done:

   ```bash
   docker-compose down
   ```
   or `docker stop xyz-selenium`.

You can also set `selenium.remote.url` in `src/main/resources/config.properties` instead of passing `-Dselenium.remote.url`. Optional: open http://localhost:7900 to view the browser (noVNC) while tests run.

### Allure report (local)

Reports are generated into `target/allure-report`. To view:

1. **Serve (opens in browser):**  
   ```bash
   mvn allure:serve
   ```  
   Runs a local server and opens the report. Run after `mvn test` so `target/allure-results` exists.

2. **Or open the report file:**  
   After `mvn test` (or `mvn allure:report`), open `target/allure-report/index.html` in your browser.

### Allure report on GitHub Pages (CI)

The CI workflow publishes the Allure report to the **gh-pages** branch. To serve it as a website:

1. In GitHub: **Settings** → **Pages** (under "Code and automation").
2. Under **Build and deployment** → **Source**, choose **Deploy from a branch**.
3. **Branch:** select `gh-pages`, folder **/ (root)**. Click **Save**.

The report will be available at:

**`https://<your-username>.github.io/XYZ-TEST-AUTOMATION/`**

(Replace `<your-username>` with your GitHub username and `XYZ-TEST-AUTOMATION` with your repo name if different.) Each CI run updates the report and keeps history for trends.

## Config

Edit `src/main/resources/config.properties` (loaded from classpath). Main options:

- **base.url** – application under test (default: GlobalSQA XYZ Bank). Override in CI with `-Dbase.url=...`
- **headless.mode** – run Chrome headless (CI uses `-Dheadless.mode=true`)
- **selenium.remote.url** – when set, tests use RemoteWebDriver (e.g. `http://localhost:4444/wd/hub` for Docker)
- **implicit.wait** / **explicit.wait** / **page.load.timeout** – wait timeouts (seconds)

CI and secrets are documented in [.github/REPO_SECRETS.md](.github/REPO_SECRETS.md).
