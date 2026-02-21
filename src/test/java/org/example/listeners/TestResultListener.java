package org.example.listeners;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.ConfigManager;
import org.example.utils.SeleniumUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.WebDriver;

import java.io.FileInputStream;
import java.util.Optional;

/**
 * JUnit 5 Extension for test lifecycle management.
 * Handles test success/failure events and attaches screenshots.
 *
 * @author QA Team
 * @version 1.0
 */
public class TestResultListener implements TestWatcher {

    private static final Logger logger = LogManager.getLogger(TestResultListener.class);

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        logger.info("Test disabled: " + context.getDisplayName());
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        logger.info("✅ Test successful: " + context.getDisplayName());
        Allure.step("Test completed successfully");
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        logger.warn("⚠️ Test aborted: " + context.getDisplayName());
        Allure.step("Test was aborted: " + cause.getMessage());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        logger.error("❌ Test failed: " + context.getDisplayName(), cause);

        // Try to capture screenshot on failure
        try {
            WebDriver driver = (WebDriver) context.getStore(ExtensionContext.Namespace.GLOBAL)
                    .get("WebDriver");

            if (driver != null && ConfigManager.takeScreenshotOnFailure()) {
                String screenshotPath = SeleniumUtils.takeScreenshot(driver,
                        "failure_" + context.getDisplayName());

                if (screenshotPath != null) {
                    attachScreenshotToAllure(screenshotPath);
                    Allure.step("Screenshot attached to report");
                }
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot on test failure", e);
        }

        Allure.step("Test failed with error: " + cause.getMessage());
    }

    /**
     * Attaches screenshot to Allure report
     *
     * @param screenshotPath path to the screenshot file
     */
    private void attachScreenshotToAllure(String screenshotPath) {
        try {
            FileInputStream fis = new FileInputStream(screenshotPath);
            Allure.addAttachment("Failure Screenshot", "image/png", fis, ".png");
            logger.info("Screenshot attached to Allure report: " + screenshotPath);
        } catch (Exception e) {
            logger.error("Failed to attach screenshot to Allure: " + e.getMessage(), e);
        }
    }
}

