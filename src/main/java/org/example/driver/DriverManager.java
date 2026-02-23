package org.example.driver;

import org.example.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * Manages ChromeDriver creation, configuration, and quit.
 * Uses ChromeDriver (must be on PATH or set webdriver.chrome.driver).
 */
public class DriverManager {

    /**
     * Creates and configures a Chrome WebDriver instance.
     */
    public static WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--disable-gpu");

        if (ConfigManager.isHeadlessMode()) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-software-rasterizer", "--disable-extensions");
            options.addArguments("--disable-background-networking", "--disable-default-apps", "--disable-sync");
            options.addArguments("--no-first-run", "--disable-setuid-sandbox", "--remote-debugging-port=0");
            String chromeBin = System.getenv("CHROME_BIN");
            if (chromeBin == null && isLinux()) {
                chromeBin = "/usr/bin/google-chrome";
            }
            if (chromeBin != null && !chromeBin.isEmpty()) {
                options.setBinary(chromeBin);
            }
        }

        WebDriver driver = new ChromeDriver(options);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.getPageLoadTimeout()));
        if (!ConfigManager.isHeadlessMode() && ConfigManager.shouldMaximizeWindow()) {
            driver.manage().window().maximize();
        }
        return driver;
    }

    /**
     * Quits the given driver.
     */
    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }

    private static boolean isLinux() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("linux");
    }
}
