package dev.hexlord.hexicript.core.execution;

import dev.hexlord.hexicript.core.variables.VariableManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Execution context for script execution
 * Contains information about the current execution environment
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class ExecutionContext {
    
    private final Player player;
    private final VariableManager variableManager;
    private final Map<String, Object> localVariables;
    
    // Event context (if executing from an event)
    private Event triggerEvent;
    private String eventType;
    
    // Execution metadata
    private final long startTime;
    private int loopDepth = 0;
    private boolean asyncExecution = false;
    
    public ExecutionContext(Player player, VariableManager variableManager) {
        this.player = player;
        this.variableManager = variableManager;
        this.localVariables = new HashMap<>();
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Create a copy of this context for nested execution
     */
    public ExecutionContext copy() {
        ExecutionContext copy = new ExecutionContext(player, variableManager);
        copy.triggerEvent = this.triggerEvent;
        copy.eventType = this.eventType;
        copy.loopDepth = this.loopDepth;
        copy.asyncExecution = this.asyncExecution;
        copy.localVariables.putAll(this.localVariables);
        return copy;
    }
    
    /**
     * Set a local variable in this context
     */
    public void setVariable(String name, Object value) {
        localVariables.put(name, value);
    }
    
    /**
     * Get a local variable from this context
     */
    public Object getVariable(String name) {
        return localVariables.get(name);
    }
    
    /**
     * Check if a local variable exists
     */
    public boolean hasVariable(String name) {
        return localVariables.containsKey(name);
    }
    
    /**
     * Remove a local variable
     */
    public void removeVariable(String name) {
        localVariables.remove(name);
    }
    
    /**
     * Clear all local variables
     */
    public void clearVariables() {
        localVariables.clear();
    }
    
    /**
     * Get execution time in milliseconds
     */
    public long getExecutionTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Enter a loop (increment loop depth)
     */
    public void enterLoop() {
        loopDepth++;
    }
    
    /**
     * Exit a loop (decrement loop depth)
     */
    public void exitLoop() {
        if (loopDepth > 0) {
            loopDepth--;
        }
    }
    
    /**
     * Check if currently inside a loop
     */
    public boolean isInLoop() {
        return loopDepth > 0;
    }
    
    // Setters
    public void setTriggerEvent(Event event) { this.triggerEvent = event; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setAsyncExecution(boolean asyncExecution) { this.asyncExecution = asyncExecution; }
    
    // Getters
    public Player getPlayer() { return player; }
    public VariableManager getVariableManager() { return variableManager; }
    public Map<String, Object> getLocalVariables() { return new HashMap<>(localVariables); }
    public Event getTriggerEvent() { return triggerEvent; }
    public String getEventType() { return eventType; }
    public long getStartTime() { return startTime; }
    public int getLoopDepth() { return loopDepth; }
    public boolean isAsyncExecution() { return asyncExecution; }
}