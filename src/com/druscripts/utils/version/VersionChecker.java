package com.druscripts.utils.version;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for checking script version updates from the druscripts.com API.
 * Performs async version check on startup and provides methods for paint overlay display.
 */
public class VersionChecker {

    private static final String API_BASE = "https://druscripts.com/api/scripts/";
    private static final int TIMEOUT_MS = 5000;

    private final String scriptSlug;
    private final String currentVersion;
    private final Consumer<String> logger;

    private String latestVersion = null;
    private boolean checkComplete = false;
    private boolean updateAvailable = false;

    /**
     * Create a new VersionChecker without logging.
     *
     * @param scriptSlug     Script identifier (e.g., "piemaker", "roguesden")
     * @param currentVersion Current script version in major.minor format (e.g., "0.1", "1.0")
     */
    public VersionChecker(String scriptSlug, String currentVersion) {
        this(scriptSlug, currentVersion, null);
    }

    /**
     * Create a new VersionChecker with logging.
     *
     * @param scriptSlug     Script identifier (e.g., "piemaker", "roguesden")
     * @param currentVersion Current script version in major.minor format (e.g., "0.1", "1.0")
     * @param logger         Optional logger for debug messages (can be null)
     */
    public VersionChecker(String scriptSlug, String currentVersion, Consumer<String> logger) {
        this.scriptSlug = scriptSlug;
        this.currentVersion = currentVersion;
        this.logger = logger;
    }

    /**
     * Start async version check. Call this once at script start.
     * Results can be checked via isCheckComplete(), isUpdateAvailable(), etc.
     */
    public void checkVersionAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(API_BASE + scriptSlug + "/version");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Simple JSON parsing (avoid external dependencies)
                    String json = response.toString();
                    latestVersion = extractJsonValue(json, "version");

                    if (latestVersion != null) {
                        updateAvailable = compareVersions(currentVersion, latestVersion) < 0;
                        log("Version check complete. Current: " + currentVersion +
                            ", Latest: " + latestVersion +
                            ", Update available: " + updateAvailable);
                    }
                } else {
                    log("Version check failed with status: " + responseCode);
                }

                conn.disconnect();
            } catch (Exception e) {
                log("Version check error: " + e.getMessage());
            } finally {
                checkComplete = true;
            }
        });
    }

    /**
     * Compare two version strings in major.minor format.
     * Supports formats: "0.1", "1.0", "2.5"
     *
     * @return -1 if v1 < v2, 0 if equal, 1 if v1 > v2
     */
    private int compareVersions(String v1, String v2) {
        // Normalize versions by splitting on dots
        String[] parts1 = normalizeVersion(v1);
        String[] parts2 = normalizeVersion(v2);

        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;

            if (num1 < num2) return -1;
            if (num1 > num2) return 1;
        }
        return 0;
    }

    /**
     * Normalize version string to array of parts.
     * Splits on dots (e.g., "1.0" -> ["1", "0"]).
     */
    private String[] normalizeVersion(String version) {
        if (version == null || version.isEmpty()) {
            return new String[]{"0"};
        }
        return version.split("\\.");
    }

    /**
     * Parse a single version part to integer.
     */
    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Extract a string value from JSON response.
     * Simple pattern matching - avoids JSON library dependency.
     */
    private String extractJsonValue(String json, String key) {
        // Match: "key":"value" or "key": "value"
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void log(String message) {
        if (logger != null) {
            logger.accept("[VersionChecker] " + message);
        }
    }

    // ==================== Getters for paint usage ====================

    /**
     * @return true if the version check HTTP request has completed (success or failure)
     */
    public boolean isCheckComplete() {
        return checkComplete;
    }

    /**
     * @return true if a newer version is available on the server
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    /**
     * @return the latest version from the server, or null if check hasn't completed
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * @return the current script version
     */
    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * @return the script slug used for the API call
     */
    public String getScriptSlug() {
        return scriptSlug;
    }
}
