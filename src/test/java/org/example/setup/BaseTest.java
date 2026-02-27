package org.example.setup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.SeleniumUtils;
import org.example.pages.customer.CustomerDashboardPage;
import org.example.pages.manager.LoginPage;
import org.example.pages.manager.ManagerDashboardPage;
import org.example.utils.AllureReportWriter;
import org.example.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Base test class (setup layer) for config, WebDriver lifecycle, teardown, and Allure integration.
 * All UI test classes extend this. All setup (config and driver manager) is kept here.
 * <p>
 * Lifecycle: {@code @BeforeAll} loads config, writes Allure env/executor;
 * {@code @BeforeEach} creates driver and navigates to base URL; {@code @AfterEach} on failure attaches
 * error overview to Allure, then quits driver. The inner {@link TestUtils.BaseTestWatcher} sets {@link #testFailed}
 * and {@link #lastFailure} so tearDown can attach the error.
 * </p>
 */
@ExtendWith(TestUtils.BaseTestWatcher.class)
public class BaseTest implements TestUtils.TestFailureCapture {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;

    /** Login page (Customer / Bank Manager Login). Created in {@link #setUp()}. */
    protected LoginPage loginPage;
    /** Manager dashboard (Add Customer, Open Account, Customers list). Created in {@link #setUp()}. */
    protected ManagerDashboardPage managerPage;
    /** Customer account (Deposit, Withdraw, Transactions). Created in {@link #setUp()}. */
    protected CustomerDashboardPage customerPage;
    /** Set by {@link TestUtils.BaseTestWatcher#testFailed}; used in tearDown to decide whether to attach error overview. */
    protected boolean testFailed = false;
    /** Set by TestWatcher when test fails; used for Error overview attachment. */
    protected Throwable lastFailure;

    @Override
    public void setTestFailed(boolean failed) {
        this.testFailed = failed;
    }

    @Override
    public void setLastFailure(Throwable cause) {
        this.lastFailure = cause;
    }

    /** Central config loaded from {@code config.properties}; system properties override (e.g. {@code -Dbase.url=...}). */
    public static final class Config {
        private static final Properties properties = new Properties();

        static {
            try (InputStream in = BaseTest.class.getResourceAsStream("/config.properties")) {
                if (in == null) {
                    throw new RuntimeException("config.properties not found on classpath");
                }
                properties.load(in);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load config.properties: " + e.getMessage(), e);
            }
        }

        /**
         * Retrieves a required string property from system properties or config.properties.
         * Throws ConfigurationException if the property is missing or empty.
         */
        private static String getRequired(String key) {
            String fromSystem = System.getProperty(key);
            if (fromSystem != null && !fromSystem.isEmpty()) return fromSystem;
            String fromProperties = properties.getProperty(key);
            if (fromProperties == null || fromProperties.isEmpty()) {
                throw new TestUtils.ConfigurationException("Required property '" + key + "' is missing from config.properties and not set as system property");
            }
            return fromProperties;
        }

        /**
         * Retrieves an optional string property from system properties or config.properties.
         * Returns the provided default value if the property is missing or empty.
         */
        private static String getOptional(String key, String defaultValue) {
            String fromSystem = System.getProperty(key);
            if (fromSystem != null && !fromSystem.isEmpty()) return fromSystem;
            String fromProperties = properties.getProperty(key);
            return fromProperties != null && !fromProperties.isEmpty() ? fromProperties : defaultValue;
        }

        public static String getBaseUrl() {
            return getRequired("base.url");
        }

        public static boolean isHeadlessMode() {
            return Boolean.parseBoolean(getOptional("headless.mode", "false"));
        }

        public static long getImplicitWait() {
            try {
                return Long.parseLong(getOptional("implicit.wait", "5"));
            } catch (NumberFormatException e) {
                throw new TestUtils.ConfigurationException("Invalid value for 'implicit.wait': must be a valid number", e);
            }
        }

        public static long getExplicitWait() {
            try {
                return Long.parseLong(getOptional("explicit.wait", "10"));
            } catch (NumberFormatException e) {
                throw new TestUtils.ConfigurationException("Invalid value for 'explicit.wait': must be a valid number", e);
            }
        }

        public static long getPageLoadTimeout() {
            try {
                return Long.parseLong(getOptional("page.load.timeout", "20"));
            } catch (NumberFormatException e) {
                throw new TestUtils.ConfigurationException("Invalid value for 'page.load.timeout': must be a valid number", e);
            }
        }

        public static boolean shouldMaximizeWindow() {
            return Boolean.parseBoolean(getOptional("window.maximize", "true"));
        }
    }

    /** Creates Chrome WebDriver using {@link Config}; used only from this class. */
    private static WebDriver createDriver() {
        boolean headless = Config.isHeadlessMode();
        ChromeOptions options = new ChromeOptions();


        if (headless) {
            logger.info("Running Chrome in headless mode.");
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }

        WebDriver d = new ChromeDriver(options);
        d.manage().timeouts().implicitlyWait(Duration.ofSeconds(Config.getImplicitWait()));
        d.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.getPageLoadTimeout()));
        if (!headless && Config.shouldMaximizeWindow()) {
            d.manage().window().maximize();
        }
        return d;
    }

    /** Quits the driver; no-op if null. */
    private static void quitDriver(WebDriver d) {
        if (d != null) {
            d.quit();
        }
    }

    @BeforeAll
    static void initConfigAndAllure() {
        SeleniumUtils.setExplicitWait(Config.getExplicitWait());
        AllureReportWriter.writeAllureEnvironmentAndExecutor(Config.getBaseUrl(), Config.isHeadlessMode());
    }

    /** Creates driver, navigates to base URL, and initializes page objects. Runs before every test method. */
    @BeforeEach
    public void setUp() {
        testFailed = false;
        lastFailure = null;
        logger.info("Test started: {}", TestUtils.getTestMethodName());
        driver = createDriver();
        driver.navigate().to(Config.getBaseUrl());
        logger.info("Navigated to base URL: {}", Config.getBaseUrl());
        loginPage = new LoginPage(driver);
        managerPage = new ManagerDashboardPage(driver);
        customerPage = new CustomerDashboardPage(driver);
    }

    /** On failure: attaches error overview to Allure, then quits driver. Always quits driver in finally. */
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                TestUtils.attachScreenshot(driver);
                if (testFailed && lastFailure != null) {
                    TestUtils.attachErrorOverviewToAllure(lastFailure);
                }
            } catch (Exception e) {
                logger.error("Error during teardown: {}", e.getMessage(), e);
            } finally {
                quitDriver(driver);
                logger.info("Test finished: {}", TestUtils.getTestMethodName());
            }
        }
    }
}
