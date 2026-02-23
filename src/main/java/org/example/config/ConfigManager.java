package org.example.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration manager to load properties from config.properties file.
 * Provides centralized access to configuration values used throughout the test framework.
 *
 * @author QA Team
 * @version 1.0
 */
public class ConfigManager {

    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE_PATH = "src/main/resources/config.properties";

    static {
        try (FileInputStream fileInputStream = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration properties: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the value of a configuration property
     *
     * @param key the property key
     * @return the property value
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets the value of a configuration property with a default value
     *
     * @param key the property key
     * @param defaultValue the default value if key not found
     * @return the property value or default value
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets browser name from configuration
     *
     * @return browser name (chrome, firefox, etc.)
     */
    public static String getBrowser() {
        return getProperty("browser", "chrome");
    }

    /**
     * Gets base URL for the application.
     * System property {@code base.url} overrides config (e.g. CI: -Dbase.url=...).
     *
     * @return the base URL
     */
    public static String getBaseUrl() {
        String fromSystem = System.getProperty("base.url");
        if (fromSystem != null && !fromSystem.isEmpty()) {
            return fromSystem;
        }
        return getProperty("base.url");
    }

    /**
     * Checks if headless mode is enabled
     *
     * @return true if headless mode is enabled
     */
    public static boolean isHeadlessMode() {
        return Boolean.parseBoolean(getProperty("headless.mode", "false"));
    }

    /**
     * Gets implicit wait timeout in seconds
     *
     * @return implicit wait timeout
     */
    public static long getImplicitWait() {
        return Long.parseLong(getProperty("implicit.wait", "10"));
    }

    /**
     * Gets explicit wait timeout in seconds
     *
     * @return explicit wait timeout
     */
    public static long getExplicitWait() {
        return Long.parseLong(getProperty("explicit.wait", "15"));
    }

    /**
     * Gets page load timeout in seconds
     *
     * @return page load timeout
     */
    public static long getPageLoadTimeout() {
        return Long.parseLong(getProperty("page.load.timeout", "30"));
    }

    /**
     * Checks if window should be maximized
     *
     * @return true if window should be maximized
     */
    public static boolean shouldMaximizeWindow() {
        return Boolean.parseBoolean(getProperty("window.maximize", "true"));
    }

    /**
     * Checks if screenshots should be taken on test failure
     *
     * @return true if screenshots should be taken on failure
     */
    public static boolean takeScreenshotOnFailure() {
        return Boolean.parseBoolean(getProperty("screenshot.on.failure", "true"));
    }

    /**
     * Gets screenshot directory path
     *
     * @return screenshot directory path
     */
    public static String getScreenshotPath() {
        return getProperty("screenshot.path", "screenshots");
    }
}

