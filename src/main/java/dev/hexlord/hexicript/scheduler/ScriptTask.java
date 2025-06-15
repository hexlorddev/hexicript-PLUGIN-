package dev.hexlord.hexicript.scheduler;

import dev.hexlord.hexicript.HexicriptPlugin;
import dev.hexlord.hexicript.core.script.Script;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Represents a scheduled script execution task
 */
public class ScriptTask {
    private final UUID taskId;
    private final Script script;
    private final String[] args;
    private final boolean isSynchronous;
    private BukkitTask bukkitTask;
    private boolean cancelled = false;
    private long startTime;
    
    public ScriptTask(Script script, String[] args, boolean isSynchronous) {
        this.taskId = UUID.randomUUID();
        this.script = script;
        this.args = args;
        this.isSynchronous = isSynchronous;
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Get the unique ID of this task
     */
    public UUID getTaskId() {
        return taskId;
    }
    
    /**
     * Get the script being executed
     */
    public Script getScript() {
        return script;
    }
    
    /**
     * Get the arguments passed to the script
     */
    public String[] getArgs() {
        return args;
    }
    
    /**
     * Check if this is a synchronous task (runs on main thread)
     */
    public boolean isSynchronous() {
        return isSynchronous;
    }
    
    /**
     * Check if this task has been cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Get the time when this task was started
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Get the duration this task has been running in milliseconds
     */
    public long getRunTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Set the BukkitTask associated with this task
     */
    protected void setBukkitTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }
    
    /**
     * Get the plugin instance
     */
    public HexicriptPlugin getPlugin() {
        return script != null ? script.getPlugin() : null;
    }
    
    /**
     * Cancel this task
     */
    public void cancel() {
        if (cancelled) return;
        
        cancelled = true;
        
        if (bukkitTask != null) {
            try {
                bukkitTask.cancel();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
