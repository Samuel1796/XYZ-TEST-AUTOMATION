package org.example.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.driver.DriverFactory;
import org.example.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base page class that provides common functionality for all page objects.
 * Implements Page Object Model and Page Factory patterns.
 *
 * @author QA Team
 * @version 1.0
 */
public class BasePage {

    protected static final Logger logger = LogManager.getLogger(BasePage.class);
    protected WebDriver driver;
    protected WebDriverWait wait;

    /**
     * Constructor that initializes the page with WebDriver
     *
     * @param driver the WebDriver instance
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = DriverFactory.getWait(driver);
        PageFactory.initElements(driver, this);
        logger.debug("Page object initialized: " + this.getClass().getSimpleName());
    }

    /**
     * Finds an element using the provided By locator
     *
     * @param locator the By locator
     * @return the WebElement
     */
    protected WebElement findElement(By locator) {
        return driver.findElement(locator);
    }

    /**
     * Clicks on an element
     *
     * @param locator the By locator
     */
    protected void click(By locator) {
        logger.debug("Clicking element: " + locator);
        SeleniumUtils.waitForElementToBeClickable(driver, locator).click();
    }

    /**
     * Sends text to an input field
     *
     * @param locator the By locator
     * @param text the text to send
     */
    protected void sendText(By locator, String text) {
        logger.debug("Sending text to element: " + locator + " Text: " + text);
        WebElement element = SeleniumUtils.waitForElementToBeVisible(driver, locator);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Gets text from an element
     *
     * @param locator the By locator
     * @return the element text
     */
    protected String getText(By locator) {
        logger.debug("Getting text from element: " + locator);
        return SeleniumUtils.getElementText(SeleniumUtils.waitForElementToBeVisible(driver, locator));
    }

    /**
     * Gets text from a WebElement
     *
     * @param element the WebElement
     * @return the element text
     */
    protected String getText(WebElement element) {
        logger.debug("Getting text from element");
        return SeleniumUtils.getElementText(element);
    }

    /**
     * Checks if an element is displayed
     *
     * @param locator the By locator
     * @return true if element is displayed
     */
    protected boolean isElementDisplayed(By locator) {
        logger.debug("Checking if element is displayed: " + locator);
        try {
            return SeleniumUtils.waitForElementToBeVisible(driver, locator).isDisplayed();
        } catch (Exception e) {
            logger.debug("Element not displayed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Waits for page title to contain text
     *
     * @param title the title text to wait for
     */
    protected void waitForPageTitle(String title) {
        logger.debug("Waiting for page title: " + title);
        SeleniumUtils.waitForPageTitle(driver, title);
    }

    /**
     * Navigates to a URL
     *
     * @param url the URL to navigate to
     */
    public void navigateTo(String url) {
        logger.info("Navigating to: " + url);
        driver.navigate().to(url);
    }

    /**
     * Gets current page URL
     *
     * @return current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Refreshes the current page
     */
    protected void refreshPage() {
        logger.debug("Refreshing page");
        driver.navigate().refresh();
    }
}

