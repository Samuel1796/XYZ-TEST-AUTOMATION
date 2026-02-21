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

import java.io.FileInputStream;
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

    /**
     * Setup method that runs before each test
     * Initializes WebDriver and navigates to base URL
     */
    @BeforeEach
    public void setUp() {
        testFailed = false;
        logger.info("===== Starting Test: " + getTestMethodName() + " =====");
        driver = DriverManager.createDriver();
        driver.navigate().to(ConfigManager.getBaseUrl());
        logger.info("Navigation to base URL successful");
    }

    /**
     * Teardown method that runs after each test
     * Takes screenshot on failure and quits WebDriver
     */
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                if (testFailed && ConfigManager.takeScreenshotOnFailure()) {
                    String screenshotPath = SeleniumUtils.takeScreenshot(driver,
                            "failure_" + getTestMethodName());
                    if (screenshotPath != null) {
                        attachScreenshotToAllure(screenshotPath);
                    }
                }
            } catch (Exception e) {
                logger.error("Error during teardown: " + e.getMessage(), e);
            } finally {
                DriverManager.quitDriver(driver);
                logger.info("===== Completed Test: " + getTestMethodName() + " =====\n");
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
     * Attaches screenshot to Allure report
     *
     * @param screenshotPath path to the screenshot file
     */
    protected void attachScreenshotToAllure(String screenshotPath) {
        try {
            FileInputStream fis = new FileInputStream(screenshotPath);
            Allure.addAttachment("Failure Screenshot", "image/png", fis, ".png");
            logger.info("Screenshot attached to Allure report: " + screenshotPath);
        } catch (Exception e) {
            logger.error("Failed to attach screenshot to Allure: " + e.getMessage(), e);
        }
    }

    /**
     * Test watcher implementation for detecting test failures
     */
    public static class BaseTestWatcher implements TestWatcher {
        private static final Logger logger = LogManager.getLogger(BaseTestWatcher.class);

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            logger.error("❌ Test failed: " + context.getDisplayName(), cause);
            // Set test failed flag on the test instance
            try {
                Object testInstance = context.getTestInstance().orElse(null);
                if (testInstance instanceof BaseTest) {
                    ((BaseTest) testInstance).testFailed = true;
                }
            } catch (Exception e) {
                logger.error("Failed to set test failed flag", e);
            }
        }

        @Override
        public void testSuccessful(ExtensionContext context) {
            logger.info("✅ Test successful: " + context.getDisplayName());
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            logger.warn("⚠️ Test aborted: " + context.getDisplayName());
        }

        @Override
        public void testDisabled(ExtensionContext context, Optional<String> reason) {
            logger.info("⏭️ Test disabled: " + context.getDisplayName());
        }
    }
}


