package org.example.setup;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.ConfigManager;
import org.example.driver.DriverManager;
import org.example.utils.AllureReportWriter;
import org.example.utils.SeleniumUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Base test class (setup layer) for WebDriver lifecycle, teardown, and Allure integration.
 * All test classes extend this. Lives in {@code org.example.setup} to keep test setup separate from tests.
 * Implements TestWatcher for failure detection and attachments.
 */
@ExtendWith(BaseTest.BaseTestWatcher.class)
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected boolean testFailed = false;
    /** Set by TestWatcher when test fails; used for Error overview attachment. */
    protected Throwable lastFailure;

    @BeforeAll
    static void writeAllureEnvironmentAndExecutor() {
        AllureReportWriter.writeAllureEnvironmentAndExecutor();
    }

    @BeforeEach
    public void setUp() {
        testFailed = false;
        lastFailure = null;
        logger.info("Test started: {}", getTestMethodName());
        driver = DriverManager.createDriver();
        driver.navigate().to(ConfigManager.getBaseUrl());
        logger.info("Navigated to base URL: {}", ConfigManager.getBaseUrl());
    }

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

    /** Detects test method name from stack (supports BDD-style should* and legacy test*). */
    protected String getTestMethodName() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String name = element.getMethodName();
            if (name != null && (name.startsWith("test") || name.startsWith("should"))) {
                return name;
            }
        }
        return "unknown";
    }

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
