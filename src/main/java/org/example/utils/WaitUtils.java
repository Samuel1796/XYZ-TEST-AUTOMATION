package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Wait utilities for thread sleep operations.
 * Provides methods to pause test execution when needed.
 *
 * @author QA Team
 * @version 1.0
 */
public class WaitUtils {

    private static final Logger logger = LogManager.getLogger(WaitUtils.class);

    /**
     * Pauses execution for specified milliseconds
     *
     * @param milliseconds the time to wait in milliseconds
     */
    public static void waitForMilliseconds(long milliseconds) {
        logger.debug("Sleep {} ms", milliseconds);
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.warn("Wait interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Pauses execution for specified seconds
     *
     * @param seconds the time to wait in seconds
     */
    public static void waitForSeconds(long seconds) {
        logger.debug("Sleep {} s", seconds);
        waitForMilliseconds(seconds * 1000);
    }

    /**
     * Small wait (500ms) - useful for UI animations
     */
    public static void smallWait() {
        waitForMilliseconds(500);
    }

    /**
     * Medium wait (2 seconds) - useful for page loads
     */
    public static void mediumWait() {
        waitForSeconds(2);
    }

    /**
     * Large wait (5 seconds) - useful for modal dialogs
     */
    public static void largeWait() {
        waitForSeconds(5);
    }
}

