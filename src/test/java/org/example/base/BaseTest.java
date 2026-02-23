package org.example.base;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.ConfigManager;
import org.example.driver.DriverManager;
import org.example.utils.SeleniumUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Base test class that provides WebDriver setup, teardown, and common functionality.
 * All test classes should extend this class.
 * Implements TestWatcher interface for automatic failure detection.
 *
 * @author QA Team
 * @version 1.0
 */
@ExtendWith(BaseTest.BaseTestWatcher.class)
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected boolean testFailed = false;
    /** Set by TestWatcher when test fails; used for Error overview attachment. */
    protected Throwable lastFailure;

    /**
     * Setup method that runs before each test
     * Initializes WebDriver and navigates to base URL
     */
    @BeforeEach
    public void setUp() {
        testFailed = false;
        lastFailure = null;
        logger.info("Test started: {}", getTestMethodName());
        driver = DriverManager.createDriver();
        driver.navigate().to(ConfigManager.getBaseUrl());
        logger.info("Navigated to base URL: {}", ConfigManager.getBaseUrl());
    }

    /**
     * Teardown method that runs after each test
     * Takes screenshot on failure and quits WebDriver
     */
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                if (testFailed) {
                    if (lastFailure != null) {
                        attachErrorOverviewToAllure(lastFailure);
                    }
                    if (ConfigManager.takeScreenshotOnFailure()) {
                        String screenshotPath = SeleniumUtils.takeScreenshot(driver,
                                "failure_" + getTestMethodName());
                        if (screenshotPath != null) {
                            attachScreenshotToAllure(screenshotPath);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error during teardown: {}", e.getMessage(), e);
            } finally {
                DriverManager.quitDriver(driver);
                logger.info("Test finished: {}", getTestMethodName());
            }
        }
    }

    /**
     * Gets the current test method name
     *
     * @return test method name
     */
    protected String getTestMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getMethodName().startsWith("test")) {
                return element.getMethodName();
            }
        }
        return "unknown";
    }

    /**
     * Attaches an Error overview (exception message + stack trace) to the Allure report for failed tests.
     */
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
     * Attaches the failure screenshot to Allure (bug report). Shown with the failed test.
     *
     * @param screenshotPath path to the screenshot file
     */
    protected void attachScreenshotToAllure(String screenshotPath) {
        try (FileInputStream fis = new FileInputStream(screenshotPath)) {
            String testName = getTestMethodName();
            String attachmentName = "Bug report – failure screenshot (" + testName + ")";
            Allure.addAttachment(attachmentName, "image/png", fis, "png");
            logger.info("Screenshot attached to Allure report for test [{}]", testName);
        } catch (Exception e) {
            logger.error("Failed to attach screenshot to Allure: {}", e.getMessage(), e);
        }
    }

    /**
     * Test watcher implementation for detecting test failures
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

        @Override
        public void testSuccessful(ExtensionContext context) {
            logger.info("Test PASSED: {}", context.getDisplayName());
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            logger.warn("Test ABORTED: {} | {}", context.getDisplayName(), cause.getMessage());
        }

        @Override
        public void testDisabled(ExtensionContext context, Optional<String> reason) {
            logger.info("Test DISABLED: {}", context.getDisplayName());
        }
    }
}


