package org.example.setup;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.ConfigManager;
import org.example.driver.DriverManager;
import org.example.pages.customer.CustomerDashboardPage;
import org.example.pages.manager.LoginPage;
import org.example.pages.manager.ManagerDashboardPage;
import org.example.utils.AllureReportWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Base test class (setup layer) for WebDriver lifecycle, teardown, and Allure integration.
 * All UI test classes extend this. Lives in {@code org.example.setup} to keep test setup separate from tests.
 * <p>
 * Lifecycle: {@code @BeforeAll} writes Allure environment/executor; {@code @BeforeEach} creates driver and navigates
 * to base URL; {@code @AfterEach} on failure attaches error overview to Allure, then quits driver.
 * The inner {@link BaseTestWatcher} sets {@link #testFailed} and {@link #lastFailure} so tearDown can attach the error.
 * </p>
 */
@ExtendWith(BaseTest.BaseTestWatcher.class)
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;


    /** Login page (Customer / Bank Manager Login). Created in {@link #setUp()}. */
    protected LoginPage loginPage;

    /** Manager dashboard (Add Customer, Open Account, Customers list). Created in {@link #setUp()}. */

    protected ManagerDashboardPage managerPage;
    /** Customer account (Deposit, Withdraw, Transactions). Created in {@link #setUp()}. */

    protected CustomerDashboardPage customerPage;
    /** Set by {@link BaseTestWatcher#testFailed}; used in tearDown to decide whether to attach error overview. */

    protected boolean testFailed = false;
    /** Set by TestWatcher when test fails; used for Error overview attachment. */

    protected Throwable lastFailure;

    /** Writes Allure environment.properties and executor.json once per test class so the report shows current run info. */
    @BeforeAll
    static void writeAllureEnvironmentAndExecutor() {
        AllureReportWriter.writeAllureEnvironmentAndExecutor();
    }

    /** Creates driver, navigates to base URL, and initializes page objects. Runs before every test method. */
    @BeforeEach
    public void setUp() {
        testFailed = false;
        lastFailure = null;
        logger.info("Test started: {}", getTestMethodName());
        driver = DriverManager.createDriver();
        driver.navigate().to(ConfigManager.getBaseUrl());
        logger.info("Navigated to base URL: {}", ConfigManager.getBaseUrl());
        loginPage = new LoginPage(driver);
        managerPage = new ManagerDashboardPage(driver);
        customerPage = new CustomerDashboardPage(driver);
    }

    /** On failure: attaches error overview to Allure, then quits driver. Always quits driver in finally. */
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                if (testFailed && lastFailure != null) {
                    attachErrorOverviewToAllure(lastFailure);
                }
            } catch (Exception e) {
                logger.error("Error during teardown: {}", e.getMessage(), e);
            } finally {
                DriverManager.quitDriver(driver);
                logger.info("Test finished: {}", getTestMethodName());
            }
        }
    }

    protected String getTestMethodName() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String name = element.getMethodName();
            if (name != null && (name.startsWith("test") || name.startsWith("should"))) {
                return name;
            }
        }
        return "unknown";
    }

    /** Builds a text attachment (test name, exception type, message, stack trace) and adds it to the Allure report. */
    protected void attachErrorOverviewToAllure(Throwable cause) {
        if (cause == null) return;
        String overview = "Test: " + getTestMethodName() + "\n\n"
                + "Error: " + cause.getClass().getSimpleName() + "\n"
                + "Message: " + (cause.getMessage() != null ? cause.getMessage() : "(no message)") + "\n\n"
                + "Stack trace:\n" + getStackTraceString(cause);
        Allure.addAttachment("Error overview", "text/plain",
                new ByteArrayInputStream(overview.getBytes(StandardCharsets.UTF_8)), "txt");
    }

    private static String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * JUnit 5 TestWatcher that records failure on the BaseTest instance so tearDown can attach error overview.
     * Also logs pass/abort/disabled for each test.
     */
    public static class BaseTestWatcher implements TestWatcher {
        private static final Logger logger = LogManager.getLogger(BaseTestWatcher.class);

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            logger.error("Test FAILED: {} | {}", context.getDisplayName(), cause.getMessage(), cause);
            try {
                Object testInstance = context.getTestInstance().orElse(null);
                if (testInstance instanceof BaseTest) {
                    BaseTest base = (BaseTest) testInstance;
                    base.testFailed = true;
                    base.lastFailure = cause;
                }
            } catch (Exception e) {
                logger.error("Failed to set test failed flag: {}", e.getMessage(), e);
            }
        }


    }
}
