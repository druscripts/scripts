package com.druscripts.utils.stats;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Fire-and-forget stats client for sending script statistics to the API.
 * Thread-safe and non-blocking.
 */
public class StatsClient {

    private static final String STATS_BASE_URL = "https://druscripts.com/api";
    private static final String STATS_ENDPOINT = STATS_BASE_URL + "/stats/event";

    private final String scriptSlug;
    private final String version;
    private final String sessionId;
    private final Consumer<String> logger;

    /**
     * Create a new stats client.
     *
     * @param scriptSlug Script identifier (e.g., "roguesden", "dyemaker")
     * @param version    Script version (e.g., "0.1")
     * @param logger     Optional logger for debug messages (can be null)
     */
    public StatsClient(String scriptSlug, String version, Consumer<String> logger) {
        this.scriptSlug = scriptSlug;
        this.version = version;
        this.sessionId = UUID.randomUUID().toString().substring(0, 8);
        this.logger = logger;
    }

    /**
     * Create a new stats client without logging.
     */
    public StatsClient(String scriptSlug, String version) {
        this(scriptSlug, version, null);
    }

    /**
     * Send a stat event (fire-and-forget).
     *
     * @param statName Name of the statistic
     * @param value    Value of the statistic
     */
    public void sendStat(String statName, Object value) {
        CompletableFuture.runAsync(() -> {
            try {
                sendStatImpl(statName, value);
            } catch (Exception e) {
                // Silently fail - stats are best-effort
                log("[STAT] Error sending: " + e.getMessage());
            }
        });
    }

    private void sendStatImpl(String statName, Object value) {
        try {
            String jsonPayload = String.format(
                "{\"script\":\"%s\",\"version\":\"%s\",\"stat\":\"%s\",\"value\":%s,\"sessionId\":\"%s\"}",
                escapeJson(scriptSlug),
                escapeJson(version),
                escapeJson(statName),
                value,
                sessionId
            );

            URL url = new URL(STATS_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 201 && responseCode != 200) {
                log(String.format("[STAT] Server returned %d for %s", responseCode, statName));
            }

            conn.disconnect();
        } catch (Exception e) {
            log(String.format("[STAT] Failed to send %s: %s", statName, e.getMessage()));
        }
    }

    private void log(String message) {
        if (logger != null) {
            logger.accept(message);
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getScriptSlug() {
        return scriptSlug;
    }
}
