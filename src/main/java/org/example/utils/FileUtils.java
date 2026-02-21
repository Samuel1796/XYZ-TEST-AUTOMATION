package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File utilities for test data and report operations.
 * Handles file creation, reading, and cleanup operations.
 *
 * @author QA Team
 * @version 1.0
 */
public class FileUtils {

    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    /**
     * Creates a directory if it doesn't exist
     *
     * @param directoryPath the path to the directory
     * @return true if directory exists or was created
     */
    public static boolean createDirectoryIfNotExists(String directoryPath) {
        try {
            Files.createDirectories(Paths.get(directoryPath));
            logger.debug("Directory created or verified: " + directoryPath);
            return true;
        } catch (IOException e) {
            logger.error("Failed to create directory: " + directoryPath, e);
            return false;
        }
    }

    /**
     * Checks if a file exists
     *
     * @param filePath the path to the file
     * @return true if file exists
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Reads file content as string
     *
     * @param filePath the path to the file
     * @return file content as string
     */
    public static String readFileAsString(String filePath) {
        try {
            logger.debug("Reading file: " + filePath);
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            logger.error("Failed to read file: " + filePath, e);
            return "";
        }
    }

    /**
     * Writes content to a file
     *
     * @param filePath the path to the file
     * @param content the content to write
     * @return true if write was successful
     */
    public static boolean writeToFile(String filePath, String content) {
        try {
            logger.debug("Writing to file: " + filePath);
            Files.write(Paths.get(filePath), content.getBytes());
            return true;
        } catch (IOException e) {
            logger.error("Failed to write to file: " + filePath, e);
            return false;
        }
    }

    /**
     * Deletes a file
     *
     * @param filePath the path to the file
     * @return true if file was deleted
     */
    public static boolean deleteFile(String filePath) {
        try {
            logger.debug("Deleting file: " + filePath);
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Failed to delete file: " + filePath, e);
            return false;
        }
    }

    /**
     * Clears directory contents
     *
     * @param directoryPath the path to the directory
     * @return true if directory was cleared
     */
    public static boolean clearDirectory(String directoryPath) {
        try {
            logger.debug("Clearing directory: " + directoryPath);
            Files.walk(Paths.get(directoryPath))
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            logger.error("Failed to delete: " + path, e);
                        }
                    });
            return true;
        } catch (IOException e) {
            logger.error("Failed to clear directory: " + directoryPath, e);
            return false;
        }
    }
}

