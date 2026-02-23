package org.example.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager to load properties from config.properties (classpath).
 * Provides centralized access to configuration values used throughout the test framework.
 */
public class ConfigManager {

    private static final Properties properties = new Properties();

    static {
        try (InputStream in = ConfigManager.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new RuntimeException("config.properties not found on classpath");
            }
            properties.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage(), e);
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
        return getProperty("base.url", "https://www.globalsqa.com/angularJs-protractor/BankingProject");
    }

    /**
     * Checks if headless mode is enabled.
     * System property {@code headless.mode} overrides config (e.g. CI: -Dheadless.mode=true).
     *
     * @return true if headless mode is enabled
     */
    public static boolean isHeadlessMode() {
        String fromSystem = System.getProperty("headless.mode");
        if (fromSystem != null && !fromSystem.isEmpty()) {
            return Boolean.parseBoolean(fromSystem);
        }
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

