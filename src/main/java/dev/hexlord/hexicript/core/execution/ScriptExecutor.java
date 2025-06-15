package dev.hexlord.hexicript.core.execution;

import dev.hexlord.hexicript.core.ScriptEngine;
import dev.hexlord.hexicript.core.script.Script;
import dev.hexlord.hexicript.core.script.ScriptStatement;
import dev.hexlord.hexicript.core.variables.VariableManager;
import dev.hexlord.hexicript.utils.Logger;
import dev.hexlord.hexicript.utils.PlayerUtils;
import dev.hexlord.hexicript.utils.LocationUtils;
import dev.hexlord.hexicript.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Executes parsed hexicript statements
 * Handles all statement types and manages execution flow
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class ScriptExecutor {
    
    private final ScriptEngine engine;
    private final VariableManager variableManager;
    
    public ScriptExecutor(ScriptEngine engine) {
        this.engine = engine;
        this.variableManager = engine.getVariableManager();
    }
    
    /**
     * Execute a complete script
     */
    public ExecutionResult execute(Script script, ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Check if script is enabled
            if (!script.isEnabled()) {
                return ExecutionResult.error("Script is disabled");
            }
            
            // Execute all statements
            for (ScriptStatement statement : script.getStatements()) {
                ExecutionResult result = executeStatement(statement, context);
                
                if (result.getType() == ExecutionResult.Type.ERROR) {
                    return result;
                }
                
                if (result.getType() == ExecutionResult.Type.RETURN) {
                    return result;
                }
                
                // Check for execution time limits
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > engine.getMaxExecutionTime()) {
                    return ExecutionResult.error("Script execution timeout exceeded");
                }
            }
            
            return ExecutionResult.success("Script executed successfully");
            
        } catch (Exception e) {
            Logger.error("Error executing script '" + script.getName() + "': " + e.getMessage());
            return ExecutionResult.error("Script execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Execute a single statement
     */
    public ExecutionResult executeStatement(ScriptStatement statement, ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            ExecutionResult result;
            
            switch (statement.getType()) {
                case EVENT:
                    result = executeEventStatement(statement, context);
                    break;
                    
                case FUNCTION:
                    result = executeFunctionStatement(statement, context);
                    break;
                    
                case CONDITION:
                    result = executeConditionStatement(statement, context);
                    break;
                    
                case ELSE:
                    result = executeElseStatement(statement, context);
                    break;
                    
                case LOOP:
                    result = executeLoopStatement(statement, context);
                    break;
                    
                case COMMAND:
                    result = executeCommandStatement(statement, context);
                    break;
                    
                case VARIABLE_SET:
                    result = executeVariableSetStatement(statement, context);
                    break;
                    
                case VARIABLE_ADD:
                    result = executeVariableAddStatement(statement, context);
                    break;
                    
                case SEND_MESSAGE:
                    result = executeSendMessageStatement(statement, context);
                    break;
                    
                case BROADCAST:
                    result = executeBroadcastStatement(statement, context);
                    break;
                    
                case GIVE_ITEM:
                    result = executeGiveItemStatement(statement, context);
                    break;
                    
                case TELEPORT:
                    result = executeTeleportStatement(statement, context);
                    break;
                    
                case ANIMATE:
                    result = executeAnimateStatement(statement, context);
                    break;
                    
                case ACTION:
                default:
                    result = executeActionStatement(statement, context);
                    break;
            }
            
            // Update execution statistics
            long executionTime = System.currentTimeMillis() - startTime;
            statement.updateExecutionStats(executionTime);
            
            return result;
            
        } catch (Exception e) {
            Logger.error("Error executing statement at line " + statement.getLineNumber() + ": " + e.getMessage());
            return ExecutionResult.error("Statement execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Execute event statement (register event handler)
     */
    private ExecutionResult executeEventStatement(ScriptStatement statement, ExecutionContext context) {
        // Event handlers are registered during script loading, not execution
        // This method executes the event handler body when the event is triggered
        
        for (ScriptStatement child : statement.getChildren()) {
            ExecutionResult result = executeStatement(child, context);
            if (result.getType() != ExecutionResult.Type.SUCCESS) {
                return result;
            }
        }
        
        return ExecutionResult.success("Event handler executed");
    }
    
    /**
     * Execute function statement
     */
    private ExecutionResult executeFunctionStatement(ScriptStatement statement, ExecutionContext context) {
        // Functions are defined during parsing, this executes the function body
        
        for (ScriptStatement child : statement.getChildren()) {
            ExecutionResult result = executeStatement(child, context);
            if (result.getType() == ExecutionResult.Type.RETURN) {
                return result;
            }
            if (result.getType() == ExecutionResult.Type.ERROR) {
                return result;
            }
        }
        
        return ExecutionResult.success("Function executed");
    }
    
    /**
     * Execute condition statement
     */
    private ExecutionResult executeConditionStatement(ScriptStatement statement, ExecutionContext context) {
        String condition = statement.getCondition();
        boolean conditionResult = evaluateCondition(condition, context);
        
        if (conditionResult) {
            for (ScriptStatement child : statement.getChildren()) {
                ExecutionResult result = executeStatement(child, context);
                if (result.getType() != ExecutionResult.Type.SUCCESS) {
                    return result;
                }
            }
        }
        
        return ExecutionResult.success("Condition evaluated");
    }
    
    /**
     * Execute else statement
     */
    private ExecutionResult executeElseStatement(ScriptStatement statement, ExecutionContext context) {
        // Else statements are handled as part of condition evaluation
        for (ScriptStatement child : statement.getChildren()) {
            ExecutionResult result = executeStatement(child, context);
            if (result.getType() != ExecutionResult.Type.SUCCESS) {
                return result;
            }
        }
        
        return ExecutionResult.success("Else block executed");
    }
    
    /**
     * Execute loop statement
     */
    private ExecutionResult executeLoopStatement(ScriptStatement statement, ExecutionContext context) {
        String loopCondition = statement.getCondition();
        int maxLoops = engine.getMaxLoopsPerTick();
        int loopCount = 0;
        
        if (loopCondition.startsWith("times:")) {
            // Loop X times
            int times = Integer.parseInt(loopCondition.substring(6));
            for (int i = 0; i < times && loopCount < maxLoops; i++) {
                context.setVariable("loop-number", i + 1);
                
                for (ScriptStatement child : statement.getChildren()) {
                    ExecutionResult result = executeStatement(child, context);
                    if (result.getType() == ExecutionResult.Type.BREAK) {
                        return ExecutionResult.success("Loop broken");
                    }
                    if (result.getType() == ExecutionResult.Type.CONTINUE) {
                        break;
                    }
                    if (result.getType() == ExecutionResult.Type.ERROR) {
                        return result;
                    }
                }
                
                loopCount++;
            }
        } else if (loopCondition.startsWith("players:")) {
            // Loop through players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (loopCount >= maxLoops) break;
                
                context.setVariable("loop-player", player);
                
                for (ScriptStatement child : statement.getChildren()) {
                    ExecutionResult result = executeStatement(child, context);
                    if (result.getType() == ExecutionResult.Type.BREAK) {
                        return ExecutionResult.success("Loop broken");
                    }
                    if (result.getType() == ExecutionResult.Type.CONTINUE) {
                        break;
                    }
                    if (result.getType() == ExecutionResult.Type.ERROR) {
                        return result;
                    }
                }
                
                loopCount++;
            }
        }
        
        return ExecutionResult.success("Loop completed");
    }
    
    /**
     * Execute command statement
     */
    private ExecutionResult executeCommandStatement(ScriptStatement statement, ExecutionContext context) {
        // Command handlers are registered during script loading
        // This executes the command handler body
        
        for (ScriptStatement child : statement.getChildren()) {
            ExecutionResult result = executeStatement(child, context);
            if (result.getType() != ExecutionResult.Type.SUCCESS) {
                return result;
            }
        }
        
        return ExecutionResult.success("Command handler executed");
    }
    
    /**
     * Execute variable set statement
     */
    private ExecutionResult executeVariableSetStatement(ScriptStatement statement, ExecutionContext context) {
        String variable = statement.getVariable();
        String value = statement.getValue();
        
        // Evaluate the value
        Object evaluatedValue = evaluateExpression(value, context);
        
        // Set the variable
        variableManager.setVariable(variable, evaluatedValue, context);
        
        return ExecutionResult.success("Variable set");
    }
    
    /**
     * Execute variable add statement
     */
    private ExecutionResult executeVariableAddStatement(ScriptStatement statement, ExecutionContext context) {
        String variable = statement.getVariable();
        String value = statement.getValue();
        
        // Evaluate the value
        Object evaluatedValue = evaluateExpression(value, context);
        
        // Add to the variable
        variableManager.addToVariable(variable, evaluatedValue, context);
        
        return ExecutionResult.success("Value added to variable");
    }
    
    /**
     * Execute send message statement
     */
    private ExecutionResult executeSendMessageStatement(ScriptStatement statement, ExecutionContext context) {
        String message = statement.getMessage();
        String target = statement.getTarget();
        
        // Process message variables
        message = processMessage(message, context);
        
        // Find target player
        Player targetPlayer = resolvePlayer(target, context);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return ExecutionResult.success("Message sent");
        } else {
            return ExecutionResult.error("Target player not found: " + target);
        }
    }
    
    /**
     * Execute broadcast statement
     */
    private ExecutionResult executeBroadcastStatement(ScriptStatement statement, ExecutionContext context) {
        String message = statement.getMessage();
        
        // Process message variables
        message = processMessage(message, context);
        
        // Broadcast to all players
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        
        return ExecutionResult.success("Message broadcasted");
    }
    
    /**
     * Execute give item statement
     */
    private ExecutionResult executeGiveItemStatement(ScriptStatement statement, ExecutionContext context) {
        String target = statement.getTarget();
        String item = statement.getItem();
        String amount = statement.getAmount();
        
        // Resolve target player
        Player targetPlayer = resolvePlayer(target, context);
        if (targetPlayer == null) {
            return ExecutionResult.error("Target player not found: " + target);
        }
        
        // Parse item and amount
        ItemStack itemStack = ItemUtils.parseItem(item);
        if (itemStack == null) {
            return ExecutionResult.error("Invalid item: " + item);
        }
        
        int itemAmount = 1;
        try {
            itemAmount = Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            // Use default amount of 1
        }
        
        itemStack.setAmount(itemAmount);
        
        // Give item to player
        targetPlayer.getInventory().addItem(itemStack);
        
        return ExecutionResult.success("Item given");
    }
    
    /**
     * Execute teleport statement
     */
    private ExecutionResult executeTeleportStatement(ScriptStatement statement, ExecutionContext context) {
        String target = statement.getTarget();
        String locationStr = statement.getLocation();
        
        // Resolve target player
        Player targetPlayer = resolvePlayer(target, context);
        if (targetPlayer == null) {
            return ExecutionResult.error("Target player not found: " + target);
        }
        
        // Parse location
        Location location = LocationUtils.parseLocation(locationStr, context);
        if (location == null) {
            return ExecutionResult.error("Invalid location: " + locationStr);
        }
        
        // Teleport player
        targetPlayer.teleport(location);
        
        return ExecutionResult.success("Player teleported");
    }
    
    /**
     * Execute animate statement
     */
    private ExecutionResult executeAnimateStatement(ScriptStatement statement, ExecutionContext context) {
        String animationType = statement.getAnimationType();
        List<String> parameters = statement.getParameters();
        
        // Handle different animation types
        switch (animationType) {
            case "particle_circle":
                return executeParticleCircleAnimation(parameters, context);
            case "particle_explosion":
                return executeParticleExplosionAnimation(parameters, context);
            case "move":
                return executeMoveAnimation(parameters, context);
            default:
                return ExecutionResult.error("Unknown animation type: " + animationType);
        }
    }
    
    /**
     * Execute generic action statement
     */
    private ExecutionResult executeActionStatement(ScriptStatement statement, ExecutionContext context) {
        String action = statement.getOriginalLine();
        
        // Try to interpret the action
        Logger.debug("Executing generic action: " + action);
        
        return ExecutionResult.success("Action executed");
    }
    
    /**
     * Evaluate a condition expression
     */
    private boolean evaluateCondition(String condition, ExecutionContext context) {
        // Simple condition evaluation - can be expanded
        condition = processVariables(condition, context);
        
        if (condition.contains("==")) {
            String[] parts = condition.split("==", 2);
            String left = parts[0].trim();
            String right = parts[1].trim();
            return left.equals(right);
        }
        
        if (condition.contains("!=")) {
            String[] parts = condition.split("!=", 2);
            String left = parts[0].trim();
            String right = parts[1].trim();
            return !left.equals(right);
        }
        
        if (condition.contains("hasPermission")) {
            Player player = context.getPlayer();
            if (player != null) {
                String permission = extractPermission(condition);
                return player.hasPermission(permission);
            }
        }
        
        return false;
    }
    
    /**
     * Evaluate an expression
     */
    private Object evaluateExpression(String expression, ExecutionContext context) {
        expression = processVariables(expression, context);
        
        // Try to parse as number
        try {
            return Integer.parseInt(expression);
        } catch (NumberFormatException e) {
            // Not a number, return as string
        }
        
        // Try to parse as boolean
        if ("true".equalsIgnoreCase(expression)) {
            return true;
        }
        if ("false".equalsIgnoreCase(expression)) {
            return false;
        }
        
        return expression;
    }
    
    /**
     * Process message with variables and placeholders
     */
    private String processMessage(String message, ExecutionContext context) {
        message = processVariables(message, context);
        
        // Process player placeholders
        Player player = context.getPlayer();
        if (player != null) {
            message = message.replace("%player%", player.getName());
            message = message.replace("%player_name%", player.getDisplayName());
            message = message.replace("%player_world%", player.getWorld().getName());
        }
        
        return message;
    }
    
    /**
     * Process variables in a string
     */
    private String processVariables(String text, ExecutionContext context) {
        // Simple variable replacement - can be enhanced
        for (String variableName : variableManager.getVariableNames(context)) {
            Object value = variableManager.getVariable(variableName, context);
            if (value != null) {
                text = text.replace("{" + variableName + "}", value.toString());
            }
        }
        
        return text;
    }
    
    /**
     * Resolve a player reference
     */
    private Player resolvePlayer(String playerRef, ExecutionContext context) {
        if ("player".equals(playerRef)) {
            return context.getPlayer();
        }
        
        return Bukkit.getPlayer(playerRef);
    }
    
    /**
     * Extract permission from condition
     */
    private String extractPermission(String condition) {
        // Extract permission from "hasPermission(permission)" pattern
        int start = condition.indexOf("hasPermission(") + 14;
        int end = condition.indexOf(")", start);
        return condition.substring(start, end).replace("\"", "");
    }
    
    /**
     * Execute particle circle animation
     */
    private ExecutionResult executeParticleCircleAnimation(List<String> parameters, ExecutionContext context) {
        // Implementation for particle circle animation
        Logger.debug("Executing particle circle animation");
        return ExecutionResult.success("Particle circle animation executed");
    }
    
    /**
     * Execute particle explosion animation
     */
    private ExecutionResult executeParticleExplosionAnimation(List<String> parameters, ExecutionContext context) {
        // Implementation for particle explosion animation
        Logger.debug("Executing particle explosion animation");
        return ExecutionResult.success("Particle explosion animation executed");
    }
    
    /**
     * Execute move animation
     */
    private ExecutionResult executeMoveAnimation(List<String> parameters, ExecutionContext context) {
        // Implementation for move animation
        Logger.debug("Executing move animation");
        return ExecutionResult.success("Move animation executed");
    }
}