package org.example.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * Creates and quits the Chrome WebDriver for UI tests. Headless when {@link ConfigManager#isHeadlessMode()}
 * is true (config or {@code -Dheadless.mode=true}). Uses local ChromeDriver only.
 */
public class DriverManager {

    private static final Logger logger = LogManager.getLogger(DriverManager.class);

    /**
     * Creates a new Chrome WebDriver. Call {@link #quitDriver(WebDriver)} when done.
     */
    public static WebDriver createDriver() {
        boolean headless = ConfigManager.isHeadlessMode();

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            logger.info("Running Chrome in headless mode.");
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }

        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isEmpty()) {
            options.setBinary(chromeBin);
            logger.info("Using Chrome binary: {}", chromeBin);
        }

        WebDriver driver = new ChromeDriver(options);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.getPageLoadTimeout()));
        if (!headless && ConfigManager.shouldMaximizeWindow()) {
            driver.manage().window().maximize();
        }
        return driver;
    }

    /** Quits the driver; no-op if null. */
    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }
}
