package dev.hexlord.hexicript.api;

import dev.hexlord.hexicript.core.Script;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Public API interface for hexicript
 * Other plugins can use this to interact with hexicript programmatically
 */
public interface HexicriptAPI {
    
    /**
     * Get the plugin instance
     */
    Plugin getPlugin();
    
    /**
     * Execute a hexicript script
     * @param script The script source code to execute
     * @param sender The command sender (can be null for console)
     * @return CompletableFuture that completes when the script finishes execution
     */
    CompletableFuture<Object> executeScript(String script, CommandSender sender);
    
    /**
     * Get a list of all loaded scripts
     */
    List<Script> getLoadedScripts();
    
    /**
     * Reload all scripts
     * @return true if reload was successful
     */
    boolean reloadScripts();
    
    /**
     * Register a custom function that can be called from hexicript scripts
     * @param namespace The namespace for the function (e.g., "myplugin")
     * @param functionName The name of the function
     * @param function The function implementation
     */
    void registerFunction(String namespace, String functionName, ScriptFunction function);
    
    /**
     * Unregister a previously registered function
     */
    void unregisterFunction(String namespace, String functionName);
    
    /**
     * Check if a script with the given name exists
     */
    boolean scriptExists(String scriptName);
    
    /**
     * Get the script with the given name, or null if not found
     */
    Script getScript(String scriptName);
}

/**
 * Functional interface for script functions
 */
@FunctionalInterface
interface ScriptFunction {
    /**
     * Execute the function with the given arguments
     * @param args The arguments passed to the function
     * @return The result of the function, or null if void
     */
    Object execute(Object... args);
}
