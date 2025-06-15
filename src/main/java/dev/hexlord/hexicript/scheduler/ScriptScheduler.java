package dev.hexlord.hexicript.scheduler;

import dev.hexlord.hexicript.HexicriptPlugin;
import dev.hexlord.hexicript.core.script.Script;
import dev.hexlord.hexicript.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Handles scheduling and execution of scripts
 */
public class ScriptScheduler {
    
    private final HexicriptPlugin plugin;
    private final Map<UUID, ScriptTask> activeTasks;
    private final Executor asyncExecutor;
    
    public ScriptScheduler(HexicriptPlugin plugin) {
        this.plugin = plugin;
        this.activeTasks = new HashMap<>();
        this.asyncExecutor = Executors.newCachedThreadPool();
    }
    
    /**
     * Run a script asynchronously
     * @param script The script to run
     * @param callback Callback to execute when the script completes
     * @return A ScriptTask that can be used to cancel the execution
     */
    public ScriptTask runAsync(Script script, Consumer<Object> callback) {
        return runAsync(script, new String[0], callback);
    }
    
    /**
     * Run a script asynchronously with arguments
     */
    public ScriptTask runAsync(Script script, String[] args, Consumer<Object> callback) {
        ScriptTask task = new ScriptTask(script, args, false);
        CompletableFuture.runAsync(() -> {
            try {
                Object result = script.execute(args);
                if (callback != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result));
                }
            } catch (Exception e) {
                Logger.severe("Error executing script asynchronously: " + e.getMessage());
                if (callback != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
                }
            } finally {
                activeTasks.remove(task.getTaskId());
            }
        }, asyncExecutor);
        
        activeTasks.put(task.getTaskId(), task);
        return task;
    }
    
    /**
     * Schedule a script to run after a delay
     * @param script The script to run
     * @param delayTicks The delay in ticks (20 ticks = 1 second)
     * @return The BukkitTask for this scheduled execution
     */
    public ScriptTask runLater(Script script, long delayTicks) {
        return runLater(script, new String[0], delayTicks);
    }
    
    /**
     * Schedule a script to run after a delay with arguments
     */
    public ScriptTask runLater(Script script, String[] args, long delayTicks) {
        ScriptTask task = new ScriptTask(script, args, true);
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                script.execute(args);
            } catch (Exception e) {
                Logger.severe("Error in scheduled script: " + e.getMessage());
            } finally {
                activeTasks.remove(task.getTaskId());
            }
        }, delayTicks);
        
        task.setBukkitTask(bukkitTask);
        activeTasks.put(task.getTaskId(), task);
        return task;
    }
    
    /**
     * Schedule a repeating script
     * @param script The script to run
     * @param delayTicks The delay before first execution (in ticks)
     * @param periodTicks The period between executions (in ticks)
     * @return The ScriptTask for this repeating execution
     */
    public ScriptTask runRepeating(Script script, long delayTicks, long periodTicks) {
        return runRepeating(script, new String[0], delayTicks, periodTicks);
    }
    
    /**
     * Schedule a repeating script with arguments
     */
    public ScriptTask runRepeating(Script script, String[] args, long delayTicks, long periodTicks) {
        ScriptTask task = new ScriptTask(script, args, true);
        BukkitTask bukkitTask = new ScriptRunnable(plugin, script, args, task).runTaskTimer(plugin, delayTicks, periodTicks);
        task.setBukkitTask(bukkitTask);
        activeTasks.put(task.getTaskId(), task);
        return task;
    }
    
    /**
     * Cancel a scheduled task
     * @param taskId The ID of the task to cancel
     * @return true if the task was found and cancelled
     */
    public boolean cancelTask(UUID taskId) {
        ScriptTask task = activeTasks.remove(taskId);
        if (task != null) {
            task.cancel();
            return true;
        }
        return false;
    }
    
    /**
     * Cancel all active tasks
     */
    public void cancelAllTasks() {
        for (ScriptTask task : activeTasks.values()) {
            task.cancel();
        }
        activeTasks.clear();
    }
    
    /**
     * Get the number of active tasks
     */
    public int getActiveTaskCount() {
        return activeTasks.size();
    }
    
    /**
     * Internal runnable for repeating scripts
     */
    private static class ScriptRunnable extends org.bukkit.scheduler.BukkitRunnable {
        private final HexicriptPlugin plugin;
        private final Script script;
        private final String[] args;
        private final ScriptTask task;
        
        public ScriptRunnable(HexicriptPlugin plugin, Script script, String[] args, ScriptTask task) {
            this.plugin = plugin;
            this.script = script;
            this.args = args;
            this.task = task;
        }
        
        @Override
        public void run() {
            try {
                script.execute(args);
            } catch (Exception e) {
                Logger.severe("Error in repeating script: " + e.getMessage());
                // Cancel the task if there's an error
                this.cancel();
                task.getPlugin().getScheduler().cancelTask(task.getTaskId());
            }
        }
    }
}
