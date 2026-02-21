package org.example.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * WebDriver factory to create and manage WebDriver instances.
 * Handles initialization of ChromeDriver with appropriate options based on configuration.
 *
 * @author QA Team
 * @version 1.0
 */
public class DriverFactory {

    private static final Logger logger = LogManager.getLogger(DriverFactory.class);

    /**
     * Creates and initializes a new WebDriver instance
     *
     * @return configured WebDriver instance
     */
    public static WebDriver createDriver() {
        logger.info("Creating WebDriver instance");

        String browser = ConfigManager.getBrowser().toLowerCase();
        WebDriver driver;

        switch (browser) {
            case "chrome":
                driver = createChromeDriver();
                break;
            default:
                logger.error("Unsupported browser: " + browser);
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        configureDriver(driver);
        logger.info("WebDriver initialized successfully");
        return driver;
    }

    /**
     * Creates a ChromeDriver with appropriate options
     *
     * @return configured ChromeDriver instance
     */
    private static WebDriver createChromeDriver() {
        logger.debug("Creating ChromeDriver");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        if (ConfigManager.isHeadlessMode()) {
            logger.debug("Enabling headless mode");
            options.addArguments("--headless=new");
        }

        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");

        return new ChromeDriver(options);
    }

    /**
     * Configures WebDriver with wait timeouts
     *
     * @param driver the WebDriver instance to configure
     */
    private static void configureDriver(WebDriver driver) {
        logger.debug("Configuring WebDriver timeouts");

        long implicitWait = ConfigManager.getImplicitWait();
        long pageLoadTimeout = ConfigManager.getPageLoadTimeout();

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        if (ConfigManager.shouldMaximizeWindow()) {
            logger.debug("Maximizing browser window");
            driver.manage().window().maximize();
        }
    }

    /**
     * Quits the WebDriver instance
     *
     * @param driver the WebDriver instance to quit
     */
    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            logger.info("Quitting WebDriver");
            driver.quit();
        }
    }

    /**
     * Creates a WebDriverWait instance
     *
     * @param driver the WebDriver instance
     * @return configured WebDriverWait instance
     */
    public static WebDriverWait getWait(WebDriver driver) {
        long explicitWait = ConfigManager.getExplicitWait();
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
    }
}

