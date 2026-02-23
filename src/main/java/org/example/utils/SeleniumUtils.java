package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.ConfigManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for common Selenium operations and helper methods.
 * Includes screenshot capture, waits, and element interaction helpers.
 *
 * @author QA Team
 * @version 1.0
 */
public class SeleniumUtils {

    private static final Logger logger = LogManager.getLogger(SeleniumUtils.class);
    private static final String SCREENSHOT_DIRECTORY = ConfigManager.getScreenshotPath();

    static {
        createScreenshotDirectory();
    }

    /**
     * Takes a screenshot and saves it to the screenshots directory
     *
     * @param driver the WebDriver instance
     * @param fileName name of the screenshot file (without extension)
     * @return path to the saved screenshot
     */
    public static String takeScreenshot(WebDriver driver, String fileName) {
        try {
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filePath = SCREENSHOT_DIRECTORY + File.separator + fileName + "_" + timestamp + ".png";

            Files.copy(sourceFile.toPath(), Paths.get(filePath));
            logger.info("Screenshot saved: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("Screenshot failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Waits for an element to be visible
     *
     * @param driver the WebDriver instance
     * @param locator the By locator for the element
     * @return the WebElement when visible
     */
    public static WebElement waitForElementToBeVisible(WebDriver driver, By locator) {
        logger.debug("Wait visible: {}", locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits for an element to be clickable
     *
     * @param driver the WebDriver instance
     * @param locator the By locator for the element
     * @return the WebElement when clickable
     */
    public static WebElement waitForElementToBeClickable(WebDriver driver, By locator) {
        logger.debug("Wait clickable: {}", locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Waits for an element to be present in DOM
     *
     * @param driver the WebDriver instance
     * @param locator the By locator for the element
     * @return the WebElement when present
     */
    public static WebElement waitForElementToBePresent(WebDriver driver, By locator) {
        logger.debug("Wait present: {}", locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Checks if element is displayed
     *
     * @param element the WebElement to check
     * @return true if element is displayed
     */
    public static boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException e) {
            logger.debug("Element not displayed");
            return false;
        }
    }

    /**
     * Checks if element located by By is displayed (with short wait).
     *
     * @param driver  the WebDriver instance
     * @param locator the By locator
     * @return true if element is visible
     */
    public static boolean isElementDisplayed(WebDriver driver, By locator) {
        try {
            return waitForElementToBeVisible(driver, locator).isDisplayed();
        } catch (Exception e) {
            logger.debug("Element not displayed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Switches to alert and accepts it
     *
     * @param driver the WebDriver instance
     */
    public static void acceptAlert(WebDriver driver) {
        logger.debug("Accept alert");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    /**
     * Gets alert text
     *
     * @param driver the WebDriver instance
     * @return alert text
     */
    public static String getAlertText(WebDriver driver) {
        logger.debug("Get alert text");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        return alert.getText();
    }

    /**
     * Creates screenshot directory if it doesn't exist
     */
    private static void createScreenshotDirectory() {
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIRECTORY));
            logger.debug("Screenshot directory: {}", SCREENSHOT_DIRECTORY);
        } catch (IOException e) {
            logger.error("Screenshot directory creation failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Waits for current URL to contain the given fragment (e.g. "#/manager/addCust").
     *
     * @param driver   the WebDriver instance
     * @param urlFragment fragment that must appear in getCurrentUrl()
     */
    public static void waitForUrlContains(WebDriver driver, String urlFragment) {
        logger.debug("Wait URL contains: {}", urlFragment);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        wait.until(ExpectedConditions.urlContains(urlFragment));
    }

    /**
     * Waits until the given select has an option with the exact visible text (for async-loaded dropdowns).
     */
    public static void waitForDropdownToContainOption(WebDriver driver, By selectLocator, String optionText) {
        logger.debug("Wait dropdown option: {}", optionText);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        wait.until(d -> {
            try {
                Select sel = new Select(driver.findElement(selectLocator));
                return sel.getOptions().stream().anyMatch(o -> optionText.equals(o.getText().trim()));
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ─── WebElement-based overloads (Page Factory support) ───────────────

    /**
     * Waits for a WebElement to be visible.
     *
     * @param driver  the WebDriver instance
     * @param element the WebElement (may be a PageFactory proxy)
     * @return the WebElement when visible
     */
    public static WebElement waitUntilVisible(WebDriver driver, WebElement element) {
        logger.debug("Wait element visible");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits for a WebElement to be clickable.
     *
     * @param driver  the WebDriver instance
     * @param element the WebElement (may be a PageFactory proxy)
     * @return the WebElement when clickable
     */
    public static WebElement waitUntilClickable(WebDriver driver, WebElement element) {
        logger.debug("Wait element clickable");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits for a WebElement to be clickable, then clicks it.
     *
     * @param driver  the WebDriver instance
     * @param element the WebElement (may be a PageFactory proxy)
     */
    public static void waitAndClick(WebDriver driver, WebElement element) {
        waitUntilClickable(driver, element).click();
    }

    /**
     * Waits for a WebElement to be visible, clears it, and types text.
     *
     * @param driver  the WebDriver instance
     * @param element the WebElement (may be a PageFactory proxy)
     * @param text    the text to type
     */
    public static void clearAndType(WebDriver driver, WebElement element, String text) {
        logger.debug("Clear and type (length={})", text != null ? text.length() : 0);
        waitUntilVisible(driver, element);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * When multiple elements match the locator (e.g. Deposit and Withdraw forms both have input[ng-model='amount']),
     * waits for and returns the first one that is visible.
     */
    public static WebElement waitForFirstVisible(WebDriver driver, By locator) {
        logger.debug("Wait first visible: {}", locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        return wait.until(d -> {
            List<WebElement> elements = d.findElements(locator);
            return elements.stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        });
    }

    /**
     * Waits for the first visible element matching the locator, then immediately clears and types.
     * Retries once on stale element (e.g. Angular re-render after tab switch).
     */
    public static void waitFirstVisibleThenClearAndType(WebDriver driver, By locator, String text) {
        logger.debug("Wait first visible then clear and type (length={})", text != null ? text.length() : 0);
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                WebElement el = waitForFirstVisible(driver, locator);
                el.clear();
                el.sendKeys(text);
                return;
            } catch (StaleElementReferenceException e) {
                if (attempt == 1) throw e;
                logger.debug("Stale element, retrying: {}", locator);
            }
        }
    }
}

