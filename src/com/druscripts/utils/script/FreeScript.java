package com.druscripts.utils.script;

import com.druscripts.utils.stats.StatsClient;
import com.druscripts.utils.version.VersionChecker;
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.visual.drawing.Canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all free DruScripts.
 * Provides common functionality like title management, stats sending, and task execution.
 *
 * When synced to free scripts, the package is transformed:
 * - Package: com.druscripts.utils.script -> com.druscripts.free.utils
 * - Imports: com.druscripts.utils.stats -> com.druscripts.free.utils
 */
public abstract class FreeScript extends Script {

    private String title;
    private String version;
    private StatsClient statsClient;
    protected VersionChecker versionChecker;
    protected List<Task> tasks = new ArrayList<>();

    // Update notification styling
    private static final int UPDATE_BG_COLOR = 0xFF2B2B2B;  // Dark gray background
    private static final int UPDATE_BORDER_COLOR = 0xFFFF9900;  // Orange border
    private static final int UPDATE_TEXT_COLOR = 0xFFFF9900;  // Orange text
    private static final java.awt.Font UPDATE_FONT = new java.awt.Font("Arial", java.awt.Font.BOLD, 12);

    public FreeScript(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public void onStart() {
        // Extract title and version from ScriptDefinition annotation
        ScriptDefinition annotation = getClass().getAnnotation(ScriptDefinition.class);
        if (annotation != null) {
            this.title = annotation.name();
            this.version = String.valueOf(annotation.version());
        } else {
            this.title = "Unknown Script";
            this.version = "0.0";
        }

        // Initialize stats client
        this.statsClient = new StatsClient(getScriptSlug(), this.version);

        // Initialize version checker and start async check
        this.versionChecker = new VersionChecker(getScriptSlug(), this.version);
        this.versionChecker.checkVersionAsync();
    }

    @Override
    public int poll() {
        for (Task t : tasks) {
            if (t.activate()) {
                t.execute();
                return 0;
            }
        }
        return 100;
    }

    /**
     * Gets the script title.
     */
    public String getTitle() {
        return title != null ? title : "Unknown Script";
    }

    /**
     * Gets the script title with version.
     */
    public String getTitleWithVersion() {
        return getTitle() + " v" + (version != null ? version : "0.0");
    }

    /**
     * Gets the script version.
     */
    public String getVersion() {
        return version != null ? version : "0.0";
    }

    /**
     * Get the script slug for API calls.
     * Extracts the short name from "ScriptName.druscripts.com" format.
     * Override if your script name doesn't match the expected slug.
     */
    protected String getScriptSlug() {
        String name = getTitle().toLowerCase().replace(" ", "").replace("'", "");
        // Extract short name from "scriptname.druscripts.com" format
        int dotIndex = name.indexOf('.');
        if (dotIndex > 0) {
            return name.substring(0, dotIndex);
        }
        return name;
    }

    /**
     * Send stat to remote server (fire-and-forget).
     */
    public void sendStat(String statName, Object value) {
        if (statsClient != null) {
            statsClient.sendStat(statName, value);
        }
    }

    /**
     * Get the stats client for direct access if needed.
     */
    public StatsClient getStatsClient() {
        return statsClient;
    }

    /**
     * Get the version checker for direct access if needed.
     */
    public VersionChecker getVersionChecker() {
        return versionChecker;
    }

    /**
     * Paint update notification if a newer version is available.
     * Positions the notification below the main paint panel.
     * Call this in your onPaint() method to display the update notice.
     *
     * @param c Canvas to draw on
     * @param panelWidth Width of the main paint panel (to match width)
     * @param panelLines Number of lines in the main paint panel (to calculate Y position)
     */
    protected void paintUpdateNotice(Canvas c, int panelWidth, int panelLines) {
        if (versionChecker == null || !versionChecker.isUpdateAvailable()) {
            return;
        }

        String notice = "Update available: v" + versionChecker.getLatestVersion();
        String subtext = "Visit druscripts.com to download";

        // Calculate position below main panel (using PaintStyle constants)
        int x = 5;  // PaintStyle.START_X
        int mainPanelY = 35;  // PaintStyle.START_Y
        int padding = 10;
        int titleHeight = 22;
        int lineHeight = 18;
        int mainPanelHeight = padding * 2 + titleHeight + (panelLines * lineHeight);
        int y = mainPanelY + mainPanelHeight + 5;  // 5px gap between panels

        // Draw background box (same width as main panel)
        int boxHeight = 40;
        c.fillRect(x, y, panelWidth, boxHeight, UPDATE_BG_COLOR, 0.9);

        // Draw border
        c.fillRect(x, y, panelWidth, 2, UPDATE_BORDER_COLOR, 1.0);  // Top
        c.fillRect(x, y + boxHeight - 2, panelWidth, 2, UPDATE_BORDER_COLOR, 1.0);  // Bottom
        c.fillRect(x, y, 2, boxHeight, UPDATE_BORDER_COLOR, 1.0);  // Left
        c.fillRect(x + panelWidth - 2, y, 2, boxHeight, UPDATE_BORDER_COLOR, 1.0);  // Right

        // Draw text
        c.drawText(notice, x + 10, y + 16, UPDATE_TEXT_COLOR, UPDATE_FONT);
        c.drawText(subtext, x + 10, y + 32, 0xFFCCCCCC, new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
    }

    /**
     * Check if an update is available.
     * Useful for conditional logic in scripts.
     *
     * @return true if a newer version is available
     */
    public boolean isUpdateAvailable() {
        return versionChecker != null && versionChecker.isUpdateAvailable();
    }
}
