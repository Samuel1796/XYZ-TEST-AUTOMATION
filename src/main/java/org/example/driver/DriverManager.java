package org.example.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

/**
 * Creates and quits the Chrome WebDriver for UI tests. Headless when env HEADLESS or CI is set,
 * or when {@code -Dheadless.mode=true}. Supports remote driver when {@link ConfigManager#getSeleniumRemoteUrl()} is set.
 */
public class DriverManager {

    private static final Logger logger = LogManager.getLogger(DriverManager.class);

    /**
     * Creates a new Chrome WebDriver. Call {@link #quitDriver(WebDriver)} when done.
     */
    public static WebDriver createDriver() {
        boolean headless = Boolean.parseBoolean(
                System.getenv().getOrDefault("HEADLESS",
                        System.getenv().getOrDefault("CI", "false")))
                || ConfigManager.isHeadlessMode();

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            logger.info("Running Chrome in headless mode (CI/headless environment detected).");
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
        } else {
            logger.info("Running Chrome in headed mode.");
        }

        // CI sets CHROME_BIN to match the installed ChromeDriver version (e.g. /opt/chrome-for-testing/chrome)
        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isEmpty()) {
            options.setBinary(chromeBin);
            logger.info("Using Chrome binary: {}", chromeBin);
        }

        String remoteUrl = ConfigManager.getSeleniumRemoteUrl();
        WebDriver driver;
        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            try {
                driver = new RemoteWebDriver(URI.create(remoteUrl).toURL(), options);
            } catch (IllegalArgumentException | MalformedURLException e) {
                throw new RuntimeException("Invalid selenium.remote.url: " + remoteUrl, e);
            }
        } else {
            driver = new ChromeDriver(options);
        }


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
