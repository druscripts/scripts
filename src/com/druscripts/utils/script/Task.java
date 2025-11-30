package com.druscripts.utils.script;

import com.osmb.api.script.Script;

/**
 * Abstract base class for free script tasks.
 * Uses the base Script class instead of PremiumScript for simpler dependencies.
 */
public abstract class Task {

    protected final Script script;

    public Task(Script script) {
        this.script = script;
    }

    /**
     * Check if this task should be executed.
     *
     * @return true if this task should run
     */
    public abstract boolean activate();

    /**
     * Execute the task.
     */
    public abstract void execute();
}
