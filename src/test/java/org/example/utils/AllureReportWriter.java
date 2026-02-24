package org.example.utils;

import org.example.config.ConfigManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Writes Allure {@code environment.properties} and {@code executor.json} into the Allure results directory
 * (default {@code target/allure-results}) so the report shows Environment and Executor widgets with current run values.
 * CI can pass {@code allure.executor.buildName}, {@code allure.executor.reportUrl}, etc. via Maven {@code -D} or env.
 */
public final class AllureReportWriter {

    private static final String RESULTS_DIR = System.getProperty("allure.results.directory", "target/allure-results");

    /** Utility class; not instantiable. */
    private AllureReportWriter() {
    }

    /**
     * Writes environment.properties and executor.json to the Allure results directory.
     * Call once at the start of the test run (e.g. from BaseTest @BeforeAll).
     */
    public static void writeAllureEnvironmentAndExecutor() {
        Path dir = Paths.get(RESULTS_DIR);
        try {
            Files.createDirectories(dir);
            writeEnvironmentProperties(dir);
            writeExecutorJson(dir);
        } catch (IOException e) {
            // Log but do not fail tests; report will still work, just without env/executor
            System.err.println("AllureReportWriter: could not write env/executor: " + e.getMessage());
        }
    }

    /** Writes key-value pairs (app, URL, browser, Java/OS, headless, framework) for the Allure Environment widget. */
    private static void writeEnvironmentProperties(Path dir) throws IOException {
        Properties p = new Properties();
        p.setProperty("Application", "XYZ Bank");
        p.setProperty("Base.URL", ConfigManager.getBaseUrl());
        p.setProperty("Browser", "Chrome");
        p.setProperty("Java.Version", System.getProperty("java.version", "unknown"));
        p.setProperty("Java.Vendor", System.getProperty("java.vendor", "unknown"));
        p.setProperty("OS", System.getProperty("os.name", "unknown"));
        p.setProperty("OS.Arch", System.getProperty("os.arch", "unknown"));
        p.setProperty("Headless.Mode", String.valueOf(ConfigManager.isHeadlessMode()));
        p.setProperty("Test.Framework", "JUnit 5");
        p.setProperty("Automation", "Selenium WebDriver");

        Path file = dir.resolve("environment.properties");
        try (var w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            p.store(w, "Allure environment – generated at run time");
        }
    }

    /** Writes executor.json (buildName, buildOrder, reportUrl, url) for the Allure Executor widget; values from system properties. */
    private static void writeExecutorJson(Path dir) throws IOException {
        String buildName = System.getProperty("allure.executor.buildName", "XYZ Bank Automation");
        String buildOrder = System.getProperty("allure.executor.buildOrder", "1");
        String reportUrl = System.getProperty("allure.executor.reportUrl", "");
        String runUrl = System.getProperty("allure.executor.url", "");

        Map<String, String> map = new LinkedHashMap<>();
        map.put("reportName", "XYZ Bank – Test Report");
        map.put("name", "Maven");
        map.put("type", "maven");
        map.put("buildName", buildName);
        map.put("buildOrder", buildOrder);
        if (reportUrl != null && !reportUrl.isEmpty()) {
            map.put("reportUrl", reportUrl);
        }
        if (runUrl != null && !runUrl.isEmpty()) {
            map.put("url", runUrl);
        }

        StringBuilder sb = new StringBuilder("{\n");
        int i = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (i++ > 0) sb.append(",\n");
            sb.append("  \"").append(escapeJson(e.getKey())).append("\": \"")
                    .append(escapeJson(e.getValue())).append("\"");
        }
        sb.append("\n}\n");

        Path file = dir.resolve("executor.json");
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    /** Escapes backslash, quote, newline, carriage return, tab for use inside JSON string values. */
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
