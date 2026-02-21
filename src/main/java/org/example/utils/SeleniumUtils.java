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
            logger.info("Screenshot taken: " + filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("Failed to take screenshot: " + e.getMessage(), e);
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
        logger.debug("Waiting for element to be visible: " + locator);
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
        logger.debug("Waiting for element to be clickable: " + locator);
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
        logger.debug("Waiting for element to be present: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Waits for element to be invisible
     *
     * @param driver the WebDriver instance
     * @param locator the By locator for the element
     */
    public static void waitForElementToBeInvisible(WebDriver driver, By locator) {
        logger.debug("Waiting for element to be invisible: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Scrolls to element and clicks it
     *
     * @param driver the WebDriver instance
     * @param element the WebElement to click
     */
    public static void scrollAndClick(WebDriver driver, WebElement element) {
        logger.debug("Scrolling to element and clicking");
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        element.click();
    }

    /**
     * Executes JavaScript in the browser
     *
     * @param driver the WebDriver instance
     * @param script the JavaScript code to execute
     * @param args arguments for the script
     * @return the script execution result
     */
    public static Object executeJavaScript(WebDriver driver, String script, Object... args) {
        logger.debug("Executing JavaScript: " + script);
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    /**
     * Gets text from an element with trimming
     *
     * @param element the WebElement to get text from
     * @return trimmed text content
     */
    public static String getElementText(WebElement element) {
        String text = element.getText().trim();
        logger.debug("Element text: " + text);
        return text;
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
            logger.debug("Element not found or displayed");
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
            logger.debug("Element not displayed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clicks element after waiting for it to be clickable.
     */
    public static void click(WebDriver driver, By locator) {
        waitForElementToBeClickable(driver, locator).click();
    }

    /**
     * Clears and sends keys to element.
     */
    public static void sendKeys(WebDriver driver, By locator, String text) {
        WebElement el = waitForElementToBeVisible(driver, locator);
        el.clear();
        el.sendKeys(text);
    }

    /**
     * Gets visible text of element.
     */
    public static String getText(WebDriver driver, By locator) {
        return getElementText(waitForElementToBeVisible(driver, locator));
    }

    /**
     * Switches to alert and accepts it
     *
     * @param driver the WebDriver instance
     */
    public static void acceptAlert(WebDriver driver) {
        logger.debug("Accepting alert");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    /**
     * Switches to alert and dismisses it
     *
     * @param driver the WebDriver instance
     */
    public static void dismissAlert(WebDriver driver) {
        logger.debug("Dismissing alert");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.dismiss();
    }

    /**
     * Gets alert text
     *
     * @param driver the WebDriver instance
     * @return alert text
     */
    public static String getAlertText(WebDriver driver) {
        logger.debug("Getting alert text");
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
            logger.debug("Screenshot directory created/verified: " + SCREENSHOT_DIRECTORY);
        } catch (IOException e) {
            logger.error("Failed to create screenshot directory: " + e.getMessage(), e);
        }
    }

    /**
     * Navigates to a URL
     *
     * @param driver the WebDriver instance
     * @param url the URL to navigate to
     */
    public static void navigateTo(WebDriver driver, String url) {
        logger.info("Navigating to: " + url);
        driver.navigate().to(url);
    }

    /**
     * Waits for page title to contain a specific text
     *
     * @param driver the WebDriver instance
     * @param title the title text to wait for
     */
    public static void waitForPageTitle(WebDriver driver, String title) {
        logger.debug("Waiting for page title: " + title);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        wait.until(ExpectedConditions.titleContains(title));
    }

    /**
     * Waits for current URL to contain the given fragment (e.g. "#/manager/addCust").
     *
     * @param driver   the WebDriver instance
     * @param urlFragment fragment that must appear in getCurrentUrl()
     */
    public static void waitForUrlContains(WebDriver driver, String urlFragment) {
        logger.debug("Waiting for URL to contain: " + urlFragment);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
        wait.until(ExpectedConditions.urlContains(urlFragment));
    }

    /**
     * Waits until the given select has an option with the exact visible text (for async-loaded dropdowns).
     */
    public static void waitForDropdownToContainOption(WebDriver driver, By selectLocator, String optionText) {
        logger.debug("Waiting for dropdown to contain option: " + optionText);
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

    /**
     * Clears and sends text to an input field
     *
     * @param driver the WebDriver instance
     * @param element the input element
     * @param text the text to send
     */
    public static void clearAndSendKeys(WebDriver driver, WebElement element, String text) {
        logger.debug("Clearing field and sending text: " + text);
        element.clear();
        element.sendKeys(text);
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
        logger.debug("Waiting for element to be visible: " + element);
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
        logger.debug("Waiting for element to be clickable: " + element);
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
        logger.debug("Clearing field and typing: " + text);
        waitUntilVisible(driver, element);
        element.clear();
        element.sendKeys(text);
    }
}

