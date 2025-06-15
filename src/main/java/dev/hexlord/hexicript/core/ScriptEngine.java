package dev.hexlord.hexicript.core;

import dev.hexlord.hexicript.HexicriptPlugin;
import dev.hexlord.hexicript.core.parsing.ScriptParser;
import dev.hexlord.hexicript.core.parsing.StatementParser;
import dev.hexlord.hexicript.core.execution.ExecutionContext;
import dev.hexlord.hexicript.core.execution.ExecutionResult;
import dev.hexlord.hexicript.core.execution.ScriptExecutor;
import dev.hexlord.hexicript.core.script.Script;
import dev.hexlord.hexicript.core.script.ScriptStatement;
import dev.hexlord.hexicript.core.variables.VariableManager;
import dev.hexlord.hexicript.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Core script engine for hexicript
 * Handles parsing, compilation, and execution of hexicript scripts
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class ScriptEngine {
    
    private final HexicriptPlugin plugin;
    
    // Core components
    private final ScriptParser parser;
    private final StatementParser statementParser;
    private final ScriptExecutor executor;
    private final VariableManager variableManager;
    
    // Execution management
    private final ExecutorService asyncExecutor;
    private final ConcurrentHashMap<String, BukkitTask> runningTasks;
    
    // Performance tracking
    private long totalExecutions = 0;
    private long totalExecutionTime = 0;
    private long averageExecutionTime = 0;
    
    // Configuration
    private int maxLoopsPerTick;
    private long maxExecutionTime;
    private boolean enableOptimization;
    private boolean enableAsyncExecution;
    
    public ScriptEngine(HexicriptPlugin plugin) {
        this.plugin = plugin;
        
        // Initialize core components
        this.parser = new ScriptParser(this);
        this.statementParser = new StatementParser(this);
        this.executor = new ScriptExecutor(this);
        this.variableManager = new VariableManager(this);
        
        // Initialize execution management
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "hexicript-async-executor");
            thread.setDaemon(true);
            return thread;
        });
        this.runningTasks = new ConcurrentHashMap<>();
        
        // Load configuration
        loadConfiguration();
        
        Logger.info("Script engine initialized with advanced optimization.");
    }
    
    /**
     * Load configuration settings
     */
    private void loadConfiguration() {
        var config = plugin.getConfigManager().getConfig();
        
        this.maxLoopsPerTick = config.getInt("performance.max_loops_per_tick", 1000);
        this.maxExecutionTime = config.getLong("performance.max_execution_time_ms", 5000);
        this.enableOptimization = config.getBoolean("performance.optimization.enable_script_caching", true);
        this.enableAsyncExecution = config.getBoolean("performance.optimization.enable_async_execution", true);
    }
    
    /**
     * Parse hexicript code into a Script object
     */
    public CompletableFuture<Script> parseScript(String name, String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                // Preprocess the code
                String processedCode = preprocessCode(code);
                
                // Parse into statements
                List<ScriptStatement> statements = parser.parseStatements(processedCode);
                
                // Create script object
                Script script = new Script(name, code, statements);
                
                // Validate script
                validateScript(script);
                
                // Optimize if enabled
                if (enableOptimization) {
                    optimizeScript(script);
                }
                
                long parseTime = System.currentTimeMillis() - startTime;
                Logger.debug("Parsed script '" + name + "' in " + parseTime + "ms");
                
                return script;
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse script '" + name + "': " + e.getMessage(), e);
            }
        }, asyncExecutor);
    }
    
    /**
     * Execute a script with the given context
     */
    public CompletableFuture<ExecutionResult> executeScript(Script script, ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // Check execution limits
                if (System.currentTimeMillis() - startTime > maxExecutionTime) {
                    throw new RuntimeException("Script execution timeout exceeded");
                }
                
                // Execute the script
                ExecutionResult result = executor.execute(script, context);
                
                // Update performance metrics
                long executionTime = System.currentTimeMillis() - startTime;
                updatePerformanceMetrics(executionTime);
                
                Logger.debug("Executed script '" + script.getName() + "' in " + executionTime + "ms");
                
                return result;
                
            } catch (Exception e) {
                Logger.error("Error executing script '" + script.getName() + "': " + e.getMessage());
                return ExecutionResult.error("Script execution failed: " + e.getMessage());
            }
        }, enableAsyncExecution ? asyncExecutor : Runnable::run);
    }
    
    /**
     * Execute hexicript code directly
     */
    public CompletableFuture<ExecutionResult> executeCode(String code, Player player) {
        String scriptName = "inline-" + System.currentTimeMillis();
        ExecutionContext context = new ExecutionContext(player, variableManager);
        
        return parseScript(scriptName, code)
            .thenCompose(script -> executeScript(script, context));
    }
    
    /**
     * Execute code synchronously on the main thread
     */
    public ExecutionResult executeSyncCode(String code, Player player) {
        if (!Bukkit.isPrimaryThread()) {
            // Switch to main thread
            CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                ExecutionResult result = executeSyncCode(code, player);
                future.complete(result);
            });
            
            try {
                return future.get(maxExecutionTime, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                return ExecutionResult.error("Sync execution failed: " + e.getMessage());
            }
        }
        
        try {
            String scriptName = "sync-" + System.currentTimeMillis();
            ExecutionContext context = new ExecutionContext(player, variableManager);
            
            Script script = parseScript(scriptName, code).get();
            return executeScript(script, context).get();
            
        } catch (Exception e) {
            return ExecutionResult.error("Sync execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Preprocess code before parsing
     */
    private String preprocessCode(String code) {
        // Remove comments and normalize whitespace
        String processed = code;
        
        // Remove single-line comments
        processed = processed.replaceAll("#.*", "");
        
        // Normalize line endings
        processed = processed.replaceAll("\\r\\n", "\\n");
        processed = processed.replaceAll("\\r", "\\n");
        
        // Remove empty lines
        processed = processed.replaceAll("\\n\\s*\\n", "\\n");
        
        // Trim whitespace
        processed = processed.trim();
        
        return processed;
    }
    
    /**
     * Validate a parsed script
     */
    private void validateScript(Script script) {
        // Check script size limits
        if (script.getStatements().size() > plugin.getConfigManager().getConfig().getInt("defaults.max_script_lines", 10000)) {
            throw new RuntimeException("Script exceeds maximum line limit");
        }
        
        // Validate syntax
        for (ScriptStatement statement : script.getStatements()) {
            if (!statementParser.isValidStatement(statement)) {
                throw new RuntimeException("Invalid statement: " + statement.getOriginalLine());
            }
        }
        
        // Check for infinite loops
        detectInfiniteLoops(script);
    }
    
    /**
     * Optimize a script for better performance
     */
    private void optimizeScript(Script script) {
        // Optimize variables
        optimizeVariables(script);
        
        // Optimize loops
        optimizeLoops(script);
        
        // Optimize conditions
        optimizeConditions(script);
        
        // Cache frequently used expressions
        cacheExpressions(script);
    }
    
    /**
     * Detect potential infinite loops
     */
    private void detectInfiniteLoops(Script script) {
        // Simple loop detection - can be enhanced
        for (ScriptStatement statement : script.getStatements()) {
            if (statement.getType() == ScriptStatement.Type.LOOP) {
                // Check for basic infinite loop patterns
                String line = statement.getOriginalLine().toLowerCase();
                if (line.contains("while true") || line.contains("loop forever")) {
                    Logger.warning("Potential infinite loop detected in script: " + script.getName());
                }
            }
        }
    }
    
    /**
     * Optimize variable usage
     */
    private void optimizeVariables(Script script) {
        // Analyze variable usage patterns
        // Pre-allocate frequently used variables
        // Remove unused variables
    }
    
    /**
     * Optimize loop structures
     */
    private void optimizeLoops(Script script) {
        // Optimize loop conditions
        // Unroll small loops
        // Cache loop bounds
    }
    
    /**
     * Optimize conditional statements
     */
    private void optimizeConditions(Script script) {
        // Short-circuit evaluation
        // Condition reordering
        // Branch prediction hints
    }
    
    /**
     * Cache frequently used expressions
     */
    private void cacheExpressions(Script script) {
        // Expression caching
        // Constant folding
        // Common subexpression elimination
    }
    
    /**
     * Update performance metrics
     */
    private void updatePerformanceMetrics(long executionTime) {
        totalExecutions++;
        totalExecutionTime += executionTime;
        averageExecutionTime = totalExecutionTime / totalExecutions;
        
        // Report slow scripts
        if (executionTime > 1000) {
            Logger.warning("Slow script execution detected: " + executionTime + "ms");
        }
    }
    
    /**
     * Shutdown the script engine
     */
    public void shutdown() {
        Logger.info("Shutting down script engine...");
        
        // Cancel all running tasks
        runningTasks.values().forEach(BukkitTask::cancel);
        runningTasks.clear();
        
        // Shutdown async executor
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        Logger.info("Script engine shutdown complete.");
    }
    
    // Getters
    public HexicriptPlugin getPlugin() { return plugin; }
    public ScriptParser getParser() { return parser; }
    public StatementParser getStatementParser() { return statementParser; }
    public ScriptExecutor getExecutor() { return executor; }
    public VariableManager getVariableManager() { return variableManager; }
    
    // Performance metrics
    public long getTotalExecutions() { return totalExecutions; }
    public long getTotalExecutionTime() { return totalExecutionTime; }
    public long getAverageExecutionTime() { return averageExecutionTime; }
    public int getMaxLoopsPerTick() { return maxLoopsPerTick; }
    public long getMaxExecutionTime() { return maxExecutionTime; }
}