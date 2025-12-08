package com.druscripts.utils.script;

import com.druscripts.utils.stats.StatsClient;
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;

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
    protected List<Task> tasks = new ArrayList<>();

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
     * Override if your script name doesn't match the expected slug.
     */
    protected String getScriptSlug() {
        return getTitle().toLowerCase().replace(" ", "").replace("'", "");
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
}
