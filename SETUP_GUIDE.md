# XYZ Bank Test Automation - Setup & Execution Guide

## Table of Contents
1. [Local Setup](#local-setup)
2. [Configuration](#configuration)
3. [Running Tests](#running-tests)
4. [Docker Setup](#docker-setup)
5. [CI/CD Pipeline](#cicd-pipeline)
6. [Reports & Logs](#reports--logs)
7. [Troubleshooting](#troubleshooting)

---

## Local Setup

### System Requirements
- **OS**: Windows, macOS, or Linux
- **Java**: JDK 17 or higher
- **Maven**: 3.8.1 or higher
- **Browser**: Chrome/Chromium (automatically managed)
- **RAM**: Minimum 4GB (8GB recommended)
- **Disk Space**: 2GB for Maven dependencies

### Step 1: Install Java 17
```bash
# Windows (using Chocolatey)
choco install openjdk17

# macOS (using Homebrew)
brew install openjdk@17

# Verify installation
java -version
javac -version
```

### Step 2: Install Maven 3.8.1+
```bash
# Windows (using Chocolatey)
choco install maven

# macOS (using Homebrew)
brew install maven

# Verify installation
mvn --version
```

### Step 3: Clone Repository
```bash
git clone https://github.com/your-org/QAM06_XYZ_TEST_AUTOMATION.git
cd QAM06_XYZ_TEST_AUTOMATION
```

### Step 4: Install Dependencies
```bash
mvn clean install
# This downloads all required dependencies and builds the project
```

### Step 5: Verify Installation
```bash
# Run a quick test to verify everything works
mvn test -Dtest=ManagerTest#testSuccessfulAdditionOfNewCustomer
```

---

## Configuration

### Key Configuration Files

#### 1. `config.properties` - Environment Configuration
**Location**: `src/main/resources/config.properties`

```properties
# Browser Settings
browser=chrome                    # Only chrome is currently supported
headless.mode=false              # Set to false for local development
window.maximize=true             # Maximize browser window on launch

# Wait Timeouts (in seconds)
implicit.wait=10                 # Implicit wait for element presence
explicit.wait=15                 # WebDriverWait timeout
page.load.timeout=30             # Page load timeout

# Application URL
base.url=https://www.demo.globalqa.com/banking/index.php

# Logging
log.level=DEBUG                  # DEBUG for detailed logs, INFO for less verbose
log.path=logs                    # Directory for log files

# Report Configuration
allure.results.directory=target/allure-results
allure.report.directory=target/allure-report

# Screenshot Settings
screenshot.on.failure=true       # Capture screenshot on test failure
screenshot.path=screenshots      # Directory to save screenshots
```

#### 2. `log4j2.xml` - Logging Configuration
**Location**: `src/main/resources/log4j2.xml`

Logging is already configured to:
- Output to console for immediate feedback
- Create rolling log files in `logs/` directory
- Separate logs for different packages

### Customizing Configuration

#### For Headless Mode (CI/CD)
```properties
browser=chrome
headless.mode=true              # Enable headless mode
window.maximize=true
implicit.wait=10
explicit.wait=15
page.load.timeout=30
```

#### For Local Development (Headed Mode)
```properties
browser=chrome
headless.mode=false             # Disable headless mode
window.maximize=true
implicit.wait=10
explicit.wait=15
page.load.timeout=30
```

#### For Debug Mode (Increased Logging)
```properties
log.level=DEBUG                 # Detailed logging
# All Selenium commands will be logged
# Page object interactions will be visible in logs
```

---

## Running Tests

### All Tests
```bash
# Clean and run all tests (headed mode)
mvn clean test

# Run all tests (headless mode for CI)
mvn clean test -Dheadless.mode=true
```

### By Test Class
```bash
# Run only manager tests
mvn clean test -Dtest=ManagerTest

# Run only customer tests
mvn clean test -Dtest=CustomerTest

# Run specific test method
mvn clean test -Dtest=ManagerTest#testSuccessfulAdditionOfNewCustomer
```

### By Tag
```bash
# Run only smoke tests
mvn clean test -Dgroups=smoke

# Run validation tests
mvn clean test -Dgroups=validation

# Run security tests
mvn clean test -Dgroups=security

# Run boundary tests
mvn clean test -Dgroups=boundary_test

# Run negative tests
mvn clean test -Dgroups=negative
```

### By Feature
```bash
# Account management tests
mvn clean test -Dtest=ManagerTest

# Transaction tests
mvn clean test -Dtest=CustomerTest
```

### Custom Maven Parameters
```bash
# Set browser type (if multiple supported in future)
mvn clean test -Dbrowser=chrome

# Set implicit wait timeout
mvn clean test -Dimplicit.wait=15

# Disable screenshots on failure
mvn clean test -Dscreenshot.on.failure=false

# Combined example
mvn clean test -Dtest=ManagerTest -Dheadless.mode=true -Dimplicit.wait=15
```

### Test Execution Examples

#### Example 1: Quick Smoke Test (Local)
```bash
mvn clean test -Dtest=ManagerTest#testSuccessfulAdditionOfNewCustomer -Dheadless.mode=false
```

#### Example 2: Full Test Suite (Local)
```bash
mvn clean test
# All tests run in headed mode
# Logs saved to logs/
# Screenshots saved to screenshots/
# Allure results in target/allure-results/
```

#### Example 3: CI/CD Pipeline Run
```bash
mvn clean test \
  -Dheadless.mode=true \
  -Dbrowser=chrome \
  -Dwindow.maximize=true \
  -Dscreenshot.on.failure=true
```

---

## Docker Setup

### Build Docker Image
```bash
# Build image with default tag
docker build -t xyz-bank-test-automation .

# Build image with version tag
docker build -t xyz-bank-test-automation:1.0 .

# Build with specific Java version
docker build --build-arg JAVA_VERSION=17 -t xyz-bank-test-automation .
```

### Run Tests in Docker
```bash
# Simple test run
docker run --rm xyz-bank-test-automation

# Run with volume mounting (save reports locally)
docker run --rm \
  -v $(pwd)/target:/app/target \
  -v $(pwd)/screenshots:/app/screenshots \
  xyz-bank-test-automation

# Run specific tests
docker run --rm \
  xyz-bank-test-automation \
  mvn clean test -Dtest=ManagerTest

# Run with environment variables
docker run --rm \
  -e BROWSER=chrome \
  -e HEADLESS_MODE=true \
  xyz-bank-test-automation
```

### Using Docker Compose
```bash
# Start all services (Test + Selenoid + Allure)
docker-compose up --build

# Run tests only
docker-compose up test-automation

# View Allure Report
# Navigate to http://localhost:4040

# View Selenoid UI
# Navigate to http://localhost:8080

# Cleanup
docker-compose down -v
```

### Docker Compose Services
- **test-automation**: Main test service
- **selenoid**: Browser grid server on port 4444
- **selenoid-ui**: Selenoid dashboard on port 8080
- **allure**: Allure report server on port 4040

---

## CI/CD Pipeline

### GitHub Actions Workflow
**File**: `.github/workflows/test-automation.yml`

The pipeline automatically:
1. Checks out code
2. Sets up JDK 17
3. Runs tests in headless mode
4. Generates Allure report
5. Publishes report to GitHub Pages
6. Comments on PRs with results
7. Uploads artifacts

### Triggering the Pipeline
```bash
# Automatically triggered on:
- Push to main/develop branches
- Pull requests to main/develop
- Schedule: Daily at 2 AM UTC

# Manual trigger (if configured):
# Navigate to Actions tab > Workflow > Run workflow
```

### Viewing Results
- **Allure Report**: `https://<username>.github.io/<repo>/allure-report`
- **Artifacts**: Actions tab > Latest run > Artifacts
- **Logs**: Actions tab > Latest run > Test execution logs

---

## Reports & Logs

### Generating Allure Report

#### Locally
```bash
# Generate report from results
mvn allure:report

# Serve report (opens in browser)
mvn allure:serve
# This will open http://localhost:4040

# View specific report
# allure-report/index.html in your browser
```

#### Report Contents
- **Tests**: All executed tests with pass/fail status
- **Epics**: Grouped by User Story
- **Features**: Grouped by functionality
- **Stories**: Detailed requirements and steps
- **Severity**: Critical, Major, Minor categorization
- **Tags**: smoke, validation, security, boundary_test, etc.
- **Timeline**: Test execution timeline
- **History**: Trend analysis across runs
- **Attachments**: Screenshots for failed tests

### Accessing Logs

#### Log Files Location
- **Main log**: `logs/app.log`
- **Rolling files**: `logs/app-YYYY-MM-DD-*.log`

#### Log Example Content
```
2024-01-20 14:32:15.123 [INFO ] [main] ConfigManager - Loading configuration properties
2024-01-20 14:32:16.456 [DEBUG] [main] DriverFactory - Creating WebDriver instance
2024-01-20 14:32:18.789 [INFO ] [main] LoginPage - Logging in as customer: John
2024-01-20 14:32:19.234 [DEBUG] [main] SeleniumUtils - Waiting for element to be visible: By.cssSelector
2024-01-20 14:32:20.567 [ERROR] [main] BaseTest - Test failed: testSomeTest
```

#### Viewing Logs
```bash
# View all logs
cat logs/app.log

# Follow log file in real-time
tail -f logs/app.log

# Search in logs
grep "ERROR" logs/app.log
grep "TestName" logs/app.log

# View last N lines
tail -20 logs/app.log
```

### Screenshots on Failure
- **Location**: `screenshots/` directory
- **Naming**: `failure_<testname>_<timestamp>.png`
- **Automatic Attachment**: Screenshots are automatically attached to Allure report

---

## Test Execution Flow

### Local Execution Steps
1. **Setup Phase**
   - JVM starts
   - Configuration loaded from `config.properties`
   - Log4j initialized
   - WebDriver created (headed mode)
   
2. **Test Phase**
   - Browser launches
   - Navigation to base URL
   - Test steps execute
   - Page objects interact with elements
   - Assertions validate behavior

3. **Teardown Phase**
   - On failure: Screenshot captured
   - Driver quits
   - Logs written
   - Report data saved

### Example Test Execution Sequence
```
[14:30:00] ===== Starting Test: testSuccessfulAdditionOfNewCustomer =====
[14:30:01] Creating WebDriver instance
[14:30:02] WebDriver initialized successfully
[14:30:03] Navigating to base URL
[14:30:05] Manager Dashboard page initialized
[14:30:06] Logging in as manager: Harry Potter
[14:30:08] Clicking Add Customer button
[14:30:10] Entering customer name: Jane
[14:30:11] Entering postal code: 12345
[14:30:12] Submitting customer form
[14:30:14] Getting success message
[14:30:15] Assertion passed
[14:30:16] Quitting WebDriver
[14:30:18] ===== Completed Test: testSuccessfulAdditionOfNewCustomer =====
```

---

## Troubleshooting

### Common Issues & Solutions

#### 1. Maven Not Found
```bash
# Error: 'mvn' is not recognized
# Solution: Add Maven to PATH or use full path

# Windows
C:\maven\bin\mvn --version

# macOS/Linux
/usr/local/bin/mvn --version

# Or reinstall Maven
choco install maven
brew install maven
```

#### 2. Java Version Mismatch
```bash
# Error: Unsupported major.minor version
# Solution: Ensure Java 17 or higher

java -version
# Should show: openjdk version "17"

# Set JAVA_HOME
# Windows: set JAVA_HOME=C:\Program Files\Java\jdk-17
# macOS/Linux: export JAVA_HOME=/usr/libexec/java_home -v 17
```

#### 3. Chrome Driver Issues
```bash
# Issue: chromedriver not found
# Solution: WebDriverManager handles this automatically

# If still issues:
# 1. Clear Maven cache: mvn clean
# 2. Delete .m2 folder: rm -rf ~/.m2/repository
# 3. Run: mvn clean install
```

#### 4. Tests Timeout
```properties
# In config.properties, increase timeouts:
implicit.wait=20         # Increased from 10
explicit.wait=25         # Increased from 15
page.load.timeout=60     # Increased from 30
```

#### 5. No Tests Found
```bash
# Issue: No tests executed
# Solution: Ensure test methods follow naming convention

# Correct:
public void testSomething()     // ✓ Correct
public void testSomething2()    // ✓ Correct

# Incorrect:
public void somethingTest()     // ✗ Won't run
public void testmethod()        // ✗ case sensitive
```

#### 6. Allure Report Not Generated
```bash
# Solution 1: Check if tests created results
ls target/allure-results/

# Solution 2: Generate report explicitly
mvn allure:report

# Solution 3: Clear cache and regenerate
mvn clean
mvn allure:report

# Solution 4: Verify allure-junit5 in pom.xml
```

#### 7. Screenshots Not Taken
```properties
# In config.properties:
screenshot.on.failure=true      # Must be true
screenshot.path=screenshots      # Path must exist

# Solution:
# Create directory if doesn't exist
# Ensure write permissions
# chmod 755 screenshots  (Linux/Mac)
```

#### 8. Docker Build Fails
```bash
# Solution 1: Clear Docker cache
docker system prune -a

# Solution 2: Build with no cache
docker build --no-cache -t xyz-bank-test-automation .

# Solution 3: Check Docker version
docker --version
docker-compose --version
# Should be: Docker 20.10+, Compose 2.0+
```

#### 9. Permission Denied Errors
```bash
# macOS/Linux solution:
chmod +x logs/
chmod +x screenshots/
chmod 755 target/

# Or fix during build:
docker build --user root -t xyz-bank-test-automation .
```

### Getting Help

#### 1. Check Logs First
```bash
# View test execution logs
cat logs/app.log

# Search for errors
grep "ERROR" logs/app.log
grep "Exception" logs/app.log
```

#### 2. Run with Verbose Output
```bash
mvn clean test -X    # Very verbose Maven output
mvn clean test -v    # Verbose output
```

#### 3. Enable Debug Logging
```properties
# In config.properties:
log.level=DEBUG

# This provides detailed information about:
# - WebDriver actions
# - Element locators
# - Wait conditions
# - Configuration values
```

#### 4. Check Browser Console
```bash
# Take screenshot manually to see error messages
# Browser DevTools > Console tab usually shows useful errors
```

---

## Next Steps

1. ✅ Complete local setup
2. ✅ Run sample tests
3. ✅ Review Allure reports
4. ✅ Customize tests for your application
5. ✅ Setup CI/CD pipeline in your repository
6. ✅ Configure GitHub Pages for reports

For more information, see the main [README.md](./README.md)

