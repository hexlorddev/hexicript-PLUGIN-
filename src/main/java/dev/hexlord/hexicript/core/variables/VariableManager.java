package dev.hexlord.hexicript.core.variables;

import dev.hexlord.hexicript.core.ScriptEngine;
import dev.hexlord.hexicript.core.execution.ExecutionContext;
import dev.hexlord.hexicript.utils.Logger;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages script variables for hexicript
 * Handles local, player-specific, and global variables
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class VariableManager {
    
    private final ScriptEngine engine;
    
    // Variable storage
    private final Map<String, Object> globalVariables;
    private final Map<String, Map<String, Object>> playerVariables;
    private final Map<String, Object> temporaryVariables;
    
    // Variable metadata
    private final Map<String, VariableType> variableTypes;
    private final Map<String, Long> variableLastAccess;
    
    /**
     * Types of variables
     */
    public enum VariableType {
        GLOBAL,      // {global_var}
        PLAYER,      // {player_var::%player%}
        LIST,        // {list_var::*}
        TEMPORARY,   // {_temp_var}
        LOCAL        // Context-specific variables
    }
    
    public VariableManager(ScriptEngine engine) {
        this.engine = engine;
        this.globalVariables = new ConcurrentHashMap<>();
        this.playerVariables = new ConcurrentHashMap<>();
        this.temporaryVariables = new ConcurrentHashMap<>();
        this.variableTypes = new ConcurrentHashMap<>();
        this.variableLastAccess = new ConcurrentHashMap<>();
    }
    
    /**
     * Set a variable value
     */
    public void setVariable(String name, Object value, ExecutionContext context) {
        // Determine variable type and storage location
        VariableType type = determineVariableType(name);
        String processedName = processVariableName(name, context);
        
        // Update last access time
        variableLastAccess.put(processedName, System.currentTimeMillis());
        variableTypes.put(processedName, type);
        
        switch (type) {
            case GLOBAL:
                globalVariables.put(processedName, value);
                break;
                
            case PLAYER:
                String playerName = extractPlayerName(name, context);
                playerVariables.computeIfAbsent(playerName, k -> new ConcurrentHashMap<>())
                              .put(processedName, value);
                break;
                
            case TEMPORARY:
                temporaryVariables.put(processedName, value);
                break;
                
            case LOCAL:
                context.setVariable(processedName, value);
                break;
                
            case LIST:
                handleListVariable(processedName, value, context);
                break;
        }
        
        Logger.debug("Set variable " + name + " = " + value + " (type: " + type + ")");
    }
    
    /**
     * Get a variable value
     */
    public Object getVariable(String name, ExecutionContext context) {
        VariableType type = determineVariableType(name);
        String processedName = processVariableName(name, context);
        
        // Update last access time
        variableLastAccess.put(processedName, System.currentTimeMillis());
        
        Object value = null;
        
        switch (type) {
            case GLOBAL:
                value = globalVariables.get(processedName);
                break;
                
            case PLAYER:
                String playerName = extractPlayerName(name, context);
                Map<String, Object> playerVars = playerVariables.get(playerName);
                if (playerVars != null) {
                    value = playerVars.get(processedName);
                }
                break;
                
            case TEMPORARY:
                value = temporaryVariables.get(processedName);
                break;
                
            case LOCAL:
                value = context.getVariable(processedName);
                break;
                
            case LIST:
                value = getListVariable(processedName, context);
                break;
        }
        
        Logger.debug("Get variable " + name + " = " + value + " (type: " + type + ")");
        return value;
    }
    
    /**
     * Add a value to a variable (for numeric variables or lists)
     */
    public void addToVariable(String name, Object value, ExecutionContext context) {
        Object currentValue = getVariable(name, context);
        
        if (currentValue == null) {
            setVariable(name, value, context);
            return;
        }
        
        // Handle numeric addition
        if (currentValue instanceof Number && value instanceof Number) {
            double current = ((Number) currentValue).doubleValue();
            double add = ((Number) value).doubleValue();
            setVariable(name, current + add, context);
        }
        // Handle string concatenation
        else if (currentValue instanceof String || value instanceof String) {
            setVariable(name, currentValue.toString() + value.toString(), context);
        }
        // Handle list addition
        else if (name.endsWith("::*")) {
            addToListVariable(name, value, context);
        }
        // Default: replace value
        else {
            setVariable(name, value, context);
        }
    }
    
    /**
     * Remove a variable
     */
    public void removeVariable(String name, ExecutionContext context) {
        VariableType type = determineVariableType(name);
        String processedName = processVariableName(name, context);
        
        switch (type) {
            case GLOBAL:
                globalVariables.remove(processedName);
                break;
                
            case PLAYER:
                String playerName = extractPlayerName(name, context);
                Map<String, Object> playerVars = playerVariables.get(playerName);
                if (playerVars != null) {
                    playerVars.remove(processedName);
                }
                break;
                
            case TEMPORARY:
                temporaryVariables.remove(processedName);
                break;
                
            case LOCAL:
                context.removeVariable(processedName);
                break;
                
            case LIST:
                removeListVariable(processedName, context);
                break;
        }
        
        variableTypes.remove(processedName);
        variableLastAccess.remove(processedName);
        
        Logger.debug("Removed variable " + name);
    }
    
    /**
     * Check if a variable exists
     */
    public boolean hasVariable(String name, ExecutionContext context) {
        return getVariable(name, context) != null;
    }
    
    /**
     * Get all variable names for a context
     */
    public Set<String> getVariableNames(ExecutionContext context) {
        Set<String> names = new java.util.HashSet<>();
        
        // Add global variables
        names.addAll(globalVariables.keySet());
        
        // Add player variables
        if (context.getPlayer() != null) {
            Map<String, Object> playerVars = playerVariables.get(context.getPlayer().getName());
            if (playerVars != null) {
                names.addAll(playerVars.keySet());
            }
        }
        
        // Add temporary variables
        names.addAll(temporaryVariables.keySet());
        
        // Add local variables
        names.addAll(context.getLocalVariables().keySet());
        
        return names;
    }
    
    /**
     * Clear variables by type
     */
    public void clearVariables(VariableType type, Player player) {
        switch (type) {
            case GLOBAL:
                globalVariables.clear();
                break;
                
            case PLAYER:
                if (player != null) {
                    playerVariables.remove(player.getName());
                } else {
                    playerVariables.clear();
                }
                break;
                
            case TEMPORARY:
                temporaryVariables.clear();
                break;
        }
        
        Logger.info("Cleared " + type + " variables" + (player != null ? " for " + player.getName() : ""));
    }
    
    /**
     * Determine the type of a variable based on its name
     */
    private VariableType determineVariableType(String name) {
        if (name.startsWith("{_")) {
            return VariableType.TEMPORARY;
        } else if (name.contains("::%") || name.contains("%::")) {
            return VariableType.PLAYER;
        } else if (name.endsWith("::*")) {
            return VariableType.LIST;
        } else if (name.startsWith("{")) {
            return VariableType.GLOBAL;
        } else {
            return VariableType.LOCAL;
        }
    }
    
    /**
     * Process variable name by replacing placeholders
     */
    private String processVariableName(String name, ExecutionContext context) {
        // Remove braces
        if (name.startsWith("{") && name.endsWith("}")) {
            name = name.substring(1, name.length() - 1);
        }
        
        // Replace player placeholder
        if (name.contains("%player%") && context.getPlayer() != null) {
            name = name.replace("%player%", context.getPlayer().getName());
        }
        
        // Replace other context variables
        for (Map.Entry<String, Object> entry : context.getLocalVariables().entrySet()) {
            name = name.replace("%" + entry.getKey() + "%", entry.getValue().toString());
        }
        
        return name;
    }
    
    /**
     * Extract player name from player variable
     */
    private String extractPlayerName(String name, ExecutionContext context) {
        if (name.contains("::%") && name.contains("%::")) {
            // Extract player name from pattern like {var::%player%::data}
            int start = name.indexOf("::%") + 3;
            int end = name.indexOf("%::", start);
            return name.substring(start, end);
        } else if (context.getPlayer() != null) {
            return context.getPlayer().getName();
        }
        return "unknown";
    }
    
    /**
     * Handle list variable operations
     */
    private void handleListVariable(String name, Object value, ExecutionContext context) {
        // List variables are stored as Map<String, Object> where key is index
        String baseName = name.replace("::*", "");
        Map<String, Object> list = getOrCreateList(baseName);
        
        if (value instanceof Map) {
            // Setting entire list
            list.clear();
            list.putAll((Map<String, Object>) value);
        } else {
            // Adding single value
            int nextIndex = list.size() + 1;
            list.put(String.valueOf(nextIndex), value);
        }
        
        globalVariables.put(baseName, list);
    }
    
    /**
     * Get list variable
     */
    private Object getListVariable(String name, ExecutionContext context) {
        String baseName = name.replace("::*", "");
        return globalVariables.get(baseName);
    }
    
    /**
     * Add to list variable
     */
    private void addToListVariable(String name, Object value, ExecutionContext context) {
        String baseName = name.replace("::*", "");
        Map<String, Object> list = getOrCreateList(baseName);
        
        int nextIndex = list.size() + 1;
        list.put(String.valueOf(nextIndex), value);
        
        globalVariables.put(baseName, list);
    }
    
    /**
     * Remove list variable
     */
    private void removeListVariable(String name, ExecutionContext context) {
        String baseName = name.replace("::*", "");
        globalVariables.remove(baseName);
    }
    
    /**
     * Get or create a list for list variables
     */
    private Map<String, Object> getOrCreateList(String baseName) {
        Object existing = globalVariables.get(baseName);
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        } else {
            return new HashMap<>();
        }
    }
    
    /**
     * Get variable statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("global_variables", globalVariables.size());
        stats.put("player_variables", playerVariables.size());
        stats.put("temporary_variables", temporaryVariables.size());
        stats.put("total_variables", globalVariables.size() + temporaryVariables.size() + 
                  playerVariables.values().stream().mapToInt(Map::size).sum());
        return stats;
    }
    
    /**
     * Cleanup old unused variables
     */
    public void cleanup() {
        long cutoffTime = System.currentTimeMillis() - (30 * 60 * 1000); // 30 minutes
        
        variableLastAccess.entrySet().removeIf(entry -> {
            if (entry.getValue() < cutoffTime) {
                // Remove from appropriate storage
                VariableType type = variableTypes.get(entry.getKey());
                if (type == VariableType.TEMPORARY) {
                    temporaryVariables.remove(entry.getKey());
                }
                variableTypes.remove(entry.getKey());
                return true;
            }
            return false;
        });
        
        Logger.debug("Cleaned up unused variables");
    }
}