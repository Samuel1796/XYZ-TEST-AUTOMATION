package org.example.driver;

import org.example.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        if (ConfigManager.isHeadlessMode()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.getPageLoadTimeout()));
        if (ConfigManager.shouldMaximizeWindow()) {
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


}
