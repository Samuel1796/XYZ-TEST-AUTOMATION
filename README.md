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
│   └── ConfigManager.java      # Reads config.properties (URL, timeouts, etc.)
├── driver/
│   └── DriverManager.java      # Creates/quits ChromeDriver only (no WebDriverManager)
├── pages/
│   ├── manager/
│   │   ├── LoginPage.java
│   │   └── ManagerDashboardPage.java
│   └── customer/
│       └── CustomerDashboardPage.java
└── utils/
    ├── SeleniumUtils.java      # Helper methods: click, sendKeys, wait, screenshot
    ├── WaitUtils.java          # Thread sleep helpers (e.g. for alerts)
    ├── FileUtils.java          # File/dir operations
    └── TestDataGenerator.java  # Test data (Faker): names, amounts, postal codes

src/test/java/org/example/
├── base/
│   └── BaseTest.java           # All setup/teardown: driver, navigate, screenshot on failure
└── tests/
    ├── manager/
    │   └── ManagerTest.java
    └── customer/
        └── CustomerTest.java
```

- **BaseTest**: Every test extends this. Driver creation, open base URL, and tearDown (screenshot on failure, quit driver) are done here. Test classes only add a small `@BeforeEach` to create page objects and, if needed, test data.
- **No base page**: Page classes take `WebDriver` in the constructor and use `SeleniumUtils` for clicks, sends, and waits.
- **Driver**: Chrome only, via `DriverManager`. Use ChromeDriver on PATH.

## Run tests

```bash
mvn clean test
```

Allure report:

```bash
mvn allure:serve
```

## Config

Edit `src/main/resources/config.properties` for:

- `base.url` – application URL (default: XYZ Bank login)
- `headless.mode` – run Chrome headless
- `implicit.wait` / `explicit.wait` / `page.load.timeout`
- `screenshot.on.failure` and `screenshot.path`
