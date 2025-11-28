package com.druscripts.utils;



/**
 * Abstract base class for free script tasks.
 * Uses the base Script class instead of DruScript for simpler dependencies.
 *
 * When synced to free scripts, the package and class name are transformed:
 * - Package: com.druscripts.utils.tasks -> com.druscripts.utils
 * - Class: FreeTask -> Task
 * - Type: Script -> FreeScript
 */
public abstract class Task {

    protected final FreeScript script;

    public Task(FreeScript script) {
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
     *
     * @return true if task completed successfully
     */
    public abstract boolean execute();
}
