package org.example.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
     * Call once at the start of the test run from BaseTest @BeforeAll with config values from BaseTest.Config.
     *
     * @param baseUrl  base URL for the app (from config)
     * @param headless whether browser runs in headless mode
     */
    public static void writeAllureEnvironmentAndExecutor(String baseUrl, boolean headless) {
        Path dir = Paths.get(RESULTS_DIR);
        try {
            Files.createDirectories(dir);
            copyCategoriesJson(dir);
            writeEnvironmentProperties(dir, baseUrl, headless);
            writeExecutorJson(dir);
        } catch (IOException e) {
            // Log but do not fail tests; report will still work, just without env/executor
            System.err.println("AllureReportWriter: could not write env/executor: " + e.getMessage());
        }
    }

    /** Copies categories.json into the Allure results directory when present on the test classpath. */
    private static void copyCategoriesJson(Path dir) throws IOException {
        try (InputStream in = AllureReportWriter.class.getResourceAsStream("/allure/categories.json")) {
            if (in == null) {
                System.err.println("AllureReportWriter: categories.json not found on classpath");
                return;
            }
            Files.copy(in, dir.resolve("categories.json"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Writes key-value pairs (app, URL, browser, Java/OS, headless, framework) for the Allure Environment widget. */
    private static void writeEnvironmentProperties(Path dir, String baseUrl, boolean headless) throws IOException {
        Properties p = new Properties();
        p.setProperty("Application", "XYZ Bank");
        p.setProperty("Base.URL", baseUrl);
        p.setProperty("Browser", "Chrome");
        p.setProperty("Java.Version", System.getProperty("java.version", "unknown"));
        p.setProperty("Java.Vendor", System.getProperty("java.vendor", "unknown"));
        p.setProperty("OS", System.getProperty("os.name", "unknown"));
        p.setProperty("OS.Arch", System.getProperty("os.arch", "unknown"));
        p.setProperty("Headless.Mode", String.valueOf(headless));
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
