package dev.hexlord.hexicript.core.script;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents a complete hexicript script with metadata and statements
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class Script {
    
    private final String name;
    private final String originalCode;
    private final List<ScriptStatement> statements;
    
    // Script metadata
    private File sourceFile;
    private String author;
    private String version;
    private String description;
    private LocalDateTime created;
    private LocalDateTime lastModified;
    private LocalDateTime lastExecuted;
    
    // Script properties
    private boolean enabled = true;
    private boolean compiled = false;
    private boolean hasErrors = false;
    private List<String> errors;
    private List<String> warnings;
    
    // Dependencies and requirements
    private Set<String> requiredPermissions;
    private Set<String> requiredPlugins;
    private Set<String> usedVariables;
    private Set<String> definedFunctions;
    private Set<String> usedEvents;
    
    // Performance metrics
    private int executionCount = 0;
    private long totalExecutionTime = 0;
    private long averageExecutionTime = 0;
    private long lastExecutionTime = 0;
    
    // Configuration
    private Map<String, Object> config;
    
    public Script(String name, String originalCode, List<ScriptStatement> statements) {
        this.name = name;
        this.originalCode = originalCode;
        this.statements = new ArrayList<>(statements);
        this.created = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        
        // Initialize collections
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.requiredPermissions = new HashSet<>();
        this.requiredPlugins = new HashSet<>();
        this.usedVariables = new HashSet<>();
        this.definedFunctions = new HashSet<>();
        this.usedEvents = new HashSet<>();
        this.config = new HashMap<>();
        
        // Analyze script content
        analyzeScript();
    }
    
    /**
     * Analyze the script to extract metadata and dependencies
     */
    private void analyzeScript() {
        for (ScriptStatement statement : statements) {
            analyzeStatement(statement);
        }
        
        // Extract metadata from comments or special statements
        extractMetadata();
    }
    
    /**
     * Analyze a single statement for dependencies and usage
     */
    private void analyzeStatement(ScriptStatement statement) {
        switch (statement.getType()) {
            case EVENT:
                if (statement.getEventType() != null) {
                    usedEvents.add(statement.getEventType());
                }
                break;
                
            case FUNCTION:
                if (statement.getFunctionName() != null) {
                    definedFunctions.add(statement.getFunctionName());
                }
                break;
                
            case VARIABLE_SET:
            case VARIABLE_ADD:
                if (statement.getVariable() != null) {
                    usedVariables.add(statement.getVariable());
                }
                break;
                
            case COMMAND:
                // Commands might require permissions
                if (statement.getCommand() != null) {
                    String permission = extractPermissionFromCommand(statement.getCommand());
                    if (permission != null) {
                        requiredPermissions.add(permission);
                    }
                }
                break;
        }
        
        // Analyze child statements
        for (ScriptStatement child : statement.getChildren()) {
            analyzeStatement(child);
        }
    }
    
    /**
     * Extract metadata from script comments or special directives
     */
    private void extractMetadata() {
        String[] lines = originalCode.split("\\n");
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.startsWith("# @author ")) {
                this.author = line.substring(10).trim();
            } else if (line.startsWith("# @version ")) {
                this.version = line.substring(11).trim();
            } else if (line.startsWith("# @description ")) {
                this.description = line.substring(15).trim();
            } else if (line.startsWith("# @requires ")) {
                String plugin = line.substring(12).trim();
                this.requiredPlugins.add(plugin);
            } else if (line.startsWith("# @permission ")) {
                String permission = line.substring(14).trim();
                this.requiredPermissions.add(permission);
            }
        }
    }
    
    /**
     * Extract permission requirement from command
     */
    private String extractPermissionFromCommand(String command) {
        // Map common commands to permissions
        if (command.startsWith("/gamemode")) {
            return "minecraft.command.gamemode";
        } else if (command.startsWith("/give")) {
            return "minecraft.command.give";
        } else if (command.startsWith("/tp") || command.startsWith("/teleport")) {
            return "minecraft.command.teleport";
        } else if (command.startsWith("/op")) {
            return "minecraft.command.op";
        }
        
        return null;
    }
    
    /**
     * Add an error to the script
     */
    public void addError(String error) {
        this.errors.add(error);
        this.hasErrors = true;
    }
    
    /**
     * Add a warning to the script
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    /**
     * Clear all errors and warnings
     */
    public void clearIssues() {
        this.errors.clear();
        this.warnings.clear();
        this.hasErrors = false;
    }
    
    /**
     * Update execution statistics
     */
    public void updateExecutionStats(long executionTime) {
        this.executionCount++;
        this.totalExecutionTime += executionTime;
        this.averageExecutionTime = totalExecutionTime / executionCount;
        this.lastExecutionTime = executionTime;
        this.lastExecuted = LocalDateTime.now();
    }
    
    /**
     * Get script summary information
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Script: ").append(name).append("\n");
        summary.append("Statements: ").append(statements.size()).append("\n");
        summary.append("Functions: ").append(definedFunctions.size()).append("\n");
        summary.append("Events: ").append(usedEvents.size()).append("\n");
        summary.append("Variables: ").append(usedVariables.size()).append("\n");
        summary.append("Enabled: ").append(enabled).append("\n");
        summary.append("Compiled: ").append(compiled).append("\n");
        summary.append("Errors: ").append(errors.size()).append("\n");
        summary.append("Warnings: ").append(warnings.size()).append("\n");
        summary.append("Executions: ").append(executionCount).append("\n");
        
        if (executionCount > 0) {
            summary.append("Avg Execution Time: ").append(averageExecutionTime).append("ms\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if script has any issues
     */
    public boolean hasIssues() {
        return hasErrors || !warnings.isEmpty();
    }
    
    /**
     * Get all issues (errors and warnings) combined
     */
    public List<String> getAllIssues() {
        List<String> allIssues = new ArrayList<>();
        
        for (String error : errors) {
            allIssues.add("ERROR: " + error);
        }
        
        for (String warning : warnings) {
            allIssues.add("WARNING: " + warning);
        }
        
        return allIssues;
    }
    
    /**
     * Create a copy of this script
     */
    public Script copy() {
        List<ScriptStatement> copiedStatements = new ArrayList<>();
        for (ScriptStatement statement : statements) {
            copiedStatements.add(statement.copy());
        }
        
        Script copy = new Script(name, originalCode, copiedStatements);
        
        copy.sourceFile = this.sourceFile;
        copy.author = this.author;
        copy.version = this.version;
        copy.description = this.description;
        copy.enabled = this.enabled;
        copy.compiled = this.compiled;
        copy.config = new HashMap<>(this.config);
        
        return copy;
    }
    
    // Setters
    public void setSourceFile(File sourceFile) { 
        this.sourceFile = sourceFile; 
        this.lastModified = LocalDateTime.now();
    }
    
    public void setAuthor(String author) { this.author = author; }
    public void setVersion(String version) { this.version = version; }
    public void setDescription(String description) { this.description = description; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCompiled(boolean compiled) { this.compiled = compiled; }
    public void setConfig(String key, Object value) { this.config.put(key, value); }
    
    // Getters
    public String getName() { return name; }
    public String getOriginalCode() { return originalCode; }
    public List<ScriptStatement> getStatements() { return new ArrayList<>(statements); }
    public File getSourceFile() { return sourceFile; }
    public String getAuthor() { return author; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getLastModified() { return lastModified; }
    public LocalDateTime getLastExecuted() { return lastExecuted; }
    public boolean isEnabled() { return enabled; }
    public boolean isCompiled() { return compiled; }
    public boolean hasErrors() { return hasErrors; }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
    public Set<String> getRequiredPermissions() { return new HashSet<>(requiredPermissions); }
    public Set<String> getRequiredPlugins() { return new HashSet<>(requiredPlugins); }
    public Set<String> getUsedVariables() { return new HashSet<>(usedVariables); }
    public Set<String> getDefinedFunctions() { return new HashSet<>(definedFunctions); }
    public Set<String> getUsedEvents() { return new HashSet<>(usedEvents); }
    public int getExecutionCount() { return executionCount; }
    public long getTotalExecutionTime() { return totalExecutionTime; }
    public long getAverageExecutionTime() { return averageExecutionTime; }
    public long getLastExecutionTime() { return lastExecutionTime; }
    public Map<String, Object> getConfig() { return new HashMap<>(config); }
    public Object getConfig(String key) { return config.get(key); }
}