package dev.hexlord.hexicript.core.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a single statement in a hexicript script
 * Contains parsed information about the statement type and parameters
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class ScriptStatement {
    
    /**
     * Types of statements supported by hexicript
     */
    public enum Type {
        // Control structures
        EVENT,
        FUNCTION,
        CONDITION,
        ELSE,
        LOOP,
        
        // Commands and actions
        COMMAND,
        ACTION,
        
        // Variable operations
        VARIABLE_SET,
        VARIABLE_ADD,
        VARIABLE_REMOVE,
        VARIABLE_CLEAR,
        
        // Player actions
        SEND_MESSAGE,
        BROADCAST,
        GIVE_ITEM,
        TELEPORT,
        
        // Effects and animations
        ANIMATE,
        PARTICLE,
        SOUND,
        
        // Game mechanics
        DAMAGE,
        HEAL,
        EFFECT,
        
        // World manipulation
        SET_BLOCK,
        SPAWN_ENTITY,
        
        // Flow control
        RETURN,
        BREAK,
        CONTINUE,
        CANCEL,
        
        // Advanced features
        GUI,
        DATABASE,
        INTEGRATION
    }
    
    private final Type type;
    private final String originalLine;
    private final int lineNumber;
    private final int indentLevel;
    
    // Statement properties
    private String eventType;
    private String functionName;
    private String condition;
    private String loopType;
    private String command;
    private String variable;
    private String value;
    private String message;
    private String target;
    private String item;
    private String amount;
    private String location;
    private String animationType;
    private List<String> parameters;
    private Map<String, Object> properties;
    
    // Child statements for block structures
    private List<ScriptStatement> children;
    
    // Execution metadata
    private boolean isAsync = false;
    private int executionCount = 0;
    private long totalExecutionTime = 0;
    
    public ScriptStatement(Type type, String originalLine, int lineNumber, int indentLevel) {
        this.type = type;
        this.originalLine = originalLine;
        this.lineNumber = lineNumber;
        this.indentLevel = indentLevel;
        this.children = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.properties = new HashMap<>();
    }
    
    /**
     * Check if this statement can have child statements
     */
    public boolean canHaveChildren() {
        return type == Type.EVENT || 
               type == Type.FUNCTION || 
               type == Type.CONDITION || 
               type == Type.ELSE || 
               type == Type.LOOP ||
               type == Type.COMMAND;
    }
    
    /**
     * Add a child statement
     */
    public void addChild(ScriptStatement child) {
        children.add(child);
    }
    
    /**
     * Get all child statements
     */
    public List<ScriptStatement> getChildren() {
        return new ArrayList<>(children);
    }
    
    /**
     * Check if this statement has children
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    /**
     * Set statement properties using method chaining
     */
    public ScriptStatement setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }
    
    public ScriptStatement setFunctionName(String functionName) {
        this.functionName = functionName;
        return this;
    }
    
    public ScriptStatement setCondition(String condition) {
        this.condition = condition;
        return this;
    }
    
    public ScriptStatement setLoopType(String loopType) {
        this.loopType = loopType;
        return this;
    }
    
    public ScriptStatement setCommand(String command) {
        this.command = command;
        return this;
    }
    
    public ScriptStatement setVariable(String variable) {
        this.variable = variable;
        return this;
    }
    
    public ScriptStatement setValue(String value) {
        this.value = value;
        return this;
    }
    
    public ScriptStatement setMessage(String message) {
        this.message = message;
        return this;
    }
    
    public ScriptStatement setTarget(String target) {
        this.target = target;
        return this;
    }
    
    public ScriptStatement setItem(String item) {
        this.item = item;
        return this;
    }
    
    public ScriptStatement setAmount(String amount) {
        this.amount = amount;
        return this;
    }
    
    public ScriptStatement setLocation(String location) {
        this.location = location;
        return this;
    }
    
    public ScriptStatement setAnimationType(String animationType) {
        this.animationType = animationType;
        return this;
    }
    
    public ScriptStatement setParameters(List<String> parameters) {
        this.parameters = new ArrayList<>(parameters);
        return this;
    }
    
    public ScriptStatement addParameter(String parameter) {
        this.parameters.add(parameter);
        return this;
    }
    
    public ScriptStatement setProperty(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }
    
    public ScriptStatement setAsync(boolean async) {
        this.isAsync = async;
        return this;
    }
    
    /**
     * Update execution statistics
     */
    public void updateExecutionStats(long executionTime) {
        this.executionCount++;
        this.totalExecutionTime += executionTime;
    }
    
    /**
     * Get average execution time
     */
    public long getAverageExecutionTime() {
        if (executionCount == 0) {
            return 0;
        }
        return totalExecutionTime / executionCount;
    }
    
    /**
     * Create a copy of this statement
     */
    public ScriptStatement copy() {
        ScriptStatement copy = new ScriptStatement(type, originalLine, lineNumber, indentLevel);
        
        copy.eventType = this.eventType;
        copy.functionName = this.functionName;
        copy.condition = this.condition;
        copy.loopType = this.loopType;
        copy.command = this.command;
        copy.variable = this.variable;
        copy.value = this.value;
        copy.message = this.message;
        copy.target = this.target;
        copy.item = this.item;
        copy.amount = this.amount;
        copy.location = this.location;
        copy.animationType = this.animationType;
        copy.parameters = new ArrayList<>(this.parameters);
        copy.properties = new HashMap<>(this.properties);
        copy.isAsync = this.isAsync;
        
        // Copy children
        for (ScriptStatement child : this.children) {
            copy.addChild(child.copy());
        }
        
        return copy;
    }
    
    /**
     * Get string representation for debugging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ScriptStatement{");
        sb.append("type=").append(type);
        sb.append(", line=").append(lineNumber);
        sb.append(", indent=").append(indentLevel);
        sb.append(", originalLine='").append(originalLine).append("'");
        
        if (eventType != null) sb.append(", eventType='").append(eventType).append("'");
        if (functionName != null) sb.append(", functionName='").append(functionName).append("'");
        if (condition != null) sb.append(", condition='").append(condition).append("'");
        if (variable != null) sb.append(", variable='").append(variable).append("'");
        if (value != null) sb.append(", value='").append(value).append("'");
        if (target != null) sb.append(", target='").append(target).append("'");
        
        if (!children.isEmpty()) {
            sb.append(", children=").append(children.size());
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    // Getters
    public Type getType() { return type; }
    public String getOriginalLine() { return originalLine; }
    public int getLineNumber() { return lineNumber; }
    public int getIndentLevel() { return indentLevel; }
    public String getEventType() { return eventType; }
    public String getFunctionName() { return functionName; }
    public String getCondition() { return condition; }
    public String getLoopType() { return loopType; }
    public String getCommand() { return command; }
    public String getVariable() { return variable; }
    public String getValue() { return value; }
    public String getMessage() { return message; }
    public String getTarget() { return target; }
    public String getItem() { return item; }
    public String getAmount() { return amount; }
    public String getLocation() { return location; }
    public String getAnimationType() { return animationType; }
    public List<String> getParameters() { return new ArrayList<>(parameters); }
    public Map<String, Object> getProperties() { return new HashMap<>(properties); }
    public Object getProperty(String key) { return properties.get(key); }
    public boolean isAsync() { return isAsync; }
    public int getExecutionCount() { return executionCount; }
    public long getTotalExecutionTime() { return totalExecutionTime; }
}