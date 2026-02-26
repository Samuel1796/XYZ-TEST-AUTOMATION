package org.example.utils;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for test-related operations including error attachment, screenshots, and test watcher.
 */
public class TestUtils {

    /**
     * Retrieves the current test method name from the stack trace.
     * Looks for methods starting with "test" or "should".
     *
     * @return the test method name, or "unknown" if not found
     */
    public static String getTestMethodName() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String name = element.getMethodName();
            if (name != null && (name.startsWith("test") || name.startsWith("should"))) {
                return name;
            }
        }
        return "unknown";
    }

    /**
     * Converts a throwable's stack trace to a formatted string.
     *
     * @param t the throwable to convert
     * @return the stack trace as a string
     */
    public static String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Attaches an error overview to the Allure report containing test name, error type, message, and stack trace.
     *
     * @param cause the exception that caused the test to fail
     */
    public static void attachErrorOverviewToAllure(Throwable cause) {
        if (cause == null) return;
        String overview = "Test: " + getTestMethodName() + "\n\n"
                + "Error: " + cause.getClass().getSimpleName() + "\n"
                + "Message: " + (cause.getMessage() != null ? cause.getMessage() : "(no message)") + "\n\n"
                + "Stack trace:\n" + getStackTraceString(cause);
        Allure.addAttachment("Error overview", "text/plain",
                new ByteArrayInputStream(overview.getBytes(StandardCharsets.UTF_8)), "txt");
    }

    /**
     * Attaches a screenshot to the Allure report from the given WebDriver.
     *
     * @param driver the WebDriver instance to take a screenshot from
     */
    public static void attachScreenshot(WebDriver driver) {
        if (driver instanceof TakesScreenshot) {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.getLifecycle().addAttachment("Final screenshot", "image/png", "png", png);
        }
    }

    /**
     * Exception thrown when a required configuration property is missing or invalid.
     */
    public static class ConfigurationException extends RuntimeException {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * JUnit 5 TestWatcher that records failure on a test instance so tearDown can attach error overview.
     * Also logs pass/abort/disabled for each test.
     */
    public static class BaseTestWatcher implements TestWatcher {
        private static final Logger logger = LogManager.getLogger(BaseTestWatcher.class);

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            logger.error("Test FAILED: {} | {}", context.getDisplayName(), cause.getMessage(), cause);
            try {
                Object testInstance = context.getTestInstance().orElse(null);
                if (testInstance instanceof TestFailureCapture) {
                    TestFailureCapture capture = (TestFailureCapture) testInstance;
                    capture.setTestFailed(true);
                    capture.setLastFailure(cause);
                }
            } catch (Exception e) {
                logger.error("Failed to set test failed flag: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Interface that test classes should implement to support test failure capture by the BaseTestWatcher.
     */
    public interface TestFailureCapture {
        void setTestFailed(boolean failed);
        void setLastFailure(Throwable cause);
    }
}

