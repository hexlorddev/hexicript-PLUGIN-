package dev.hexlord.hexicript.core.parsing;

import dev.hexlord.hexicript.core.ScriptEngine;
import dev.hexlord.hexicript.core.script.ScriptStatement;
import dev.hexlord.hexicript.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced parser for hexicript syntax
 * Converts hexicript code into executable statements
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class ScriptParser {
    
    private final ScriptEngine engine;
    
    // Syntax patterns for hexicript
    private static final Pattern EVENT_PATTERN = Pattern.compile("^on\\s+(.+):");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^function\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*:");
    private static final Pattern CONDITION_PATTERN = Pattern.compile("^if\\s+(.+):");
    private static final Pattern ELSE_PATTERN = Pattern.compile("^else(\\s+if\\s+(.+))?:");
    private static final Pattern LOOP_PATTERN = Pattern.compile("^(loop|while)\\s+(.+):");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^on\\s+command\\s+[\"'](/.+)[\"']:");
    private static final Pattern VARIABLE_SET_PATTERN = Pattern.compile("^set\\s+(\\{[^}]+\\})\\s+to\\s+(.+)");
    private static final Pattern VARIABLE_ADD_PATTERN = Pattern.compile("^add\\s+(.+)\\s+to\\s+(\\{[^}]+\\})");
    private static final Pattern SEND_PATTERN = Pattern.compile("^send\\s+[\"'](.*)[\"']\\s+to\\s+(.+)");
    private static final Pattern BROADCAST_PATTERN = Pattern.compile("^broadcast\\s+[\"'](.*)[\"]");
    private static final Pattern GIVE_PATTERN = Pattern.compile("^give\\s+(\\w+)\\s+(\\d+\\s+)?(.+)");
    private static final Pattern TELEPORT_PATTERN = Pattern.compile("^teleport\\s+(\\w+)\\s+to\\s+(.+)");
    private static final Pattern ANIMATE_PATTERN = Pattern.compile("^animate_(.+)\\((.*)\\)");
    
    public ScriptParser(ScriptEngine engine) {
        this.engine = engine;
    }
    
    /**
     * Parse hexicript code into executable statements
     */
    public List<ScriptStatement> parseStatements(String code) {
        List<ScriptStatement> statements = new ArrayList<>();
        String[] lines = code.split("\\n");
        
        int lineNumber = 0;
        int indentLevel = 0;
        ScriptStatement currentBlock = null;
        
        for (String rawLine : lines) {
            lineNumber++;
            
            // Skip empty lines
            if (rawLine.trim().isEmpty()) {
                continue;
            }
            
            // Calculate indent level
            int currentIndent = getIndentLevel(rawLine);
            String line = rawLine.trim();
            
            try {
                // Parse the line into a statement
                ScriptStatement statement = parseStatement(line, lineNumber, currentIndent);
                
                if (statement != null) {
                    // Handle indentation and block structure
                    if (currentIndent == 0) {
                        // Top-level statement
                        statements.add(statement);
                        currentBlock = statement;
                    } else if (currentIndent > indentLevel && currentBlock != null) {
                        // Child statement
                        currentBlock.addChild(statement);
                    } else {
                        // Find the correct parent block
                        ScriptStatement parent = findParentBlock(statements, currentIndent);
                        if (parent != null) {
                            parent.addChild(statement);
                        } else {
                            statements.add(statement);
                        }
                    }
                    
                    indentLevel = currentIndent;
                }
                
            } catch (Exception e) {
                Logger.error("Parse error at line " + lineNumber + ": " + e.getMessage());
                Logger.error("Line content: " + line);
            }
        }
        
        return statements;
    }
    
    /**
     * Parse a single line into a statement
     */
    private ScriptStatement parseStatement(String line, int lineNumber, int indentLevel) {
        // Try to match against different statement patterns
        
        // Event handlers
        Matcher eventMatcher = EVENT_PATTERN.matcher(line);
        if (eventMatcher.matches()) {
            String eventType = eventMatcher.group(1);
            return new ScriptStatement(ScriptStatement.Type.EVENT, line, lineNumber, indentLevel)
                .setEventType(parseEventType(eventType));
        }
        
        // Function definitions
        Matcher functionMatcher = FUNCTION_PATTERN.matcher(line);
        if (functionMatcher.matches()) {
            String functionName = functionMatcher.group(1);
            String parameters = functionMatcher.group(2);
            return new ScriptStatement(ScriptStatement.Type.FUNCTION, line, lineNumber, indentLevel)
                .setFunctionName(functionName)
                .setParameters(parseParameters(parameters));
        }
        
        // Conditional statements
        Matcher conditionMatcher = CONDITION_PATTERN.matcher(line);
        if (conditionMatcher.matches()) {
            String condition = conditionMatcher.group(1);
            return new ScriptStatement(ScriptStatement.Type.CONDITION, line, lineNumber, indentLevel)
                .setCondition(parseCondition(condition));
        }
        
        // Else statements
        Matcher elseMatcher = ELSE_PATTERN.matcher(line);
        if (elseMatcher.matches()) {
            String elseCondition = elseMatcher.group(2);
            return new ScriptStatement(ScriptStatement.Type.ELSE, line, lineNumber, indentLevel)
                .setCondition(elseCondition != null ? parseCondition(elseCondition) : null);
        }
        
        // Loop statements
        Matcher loopMatcher = LOOP_PATTERN.matcher(line);
        if (loopMatcher.matches()) {
            String loopType = loopMatcher.group(1);
            String loopCondition = loopMatcher.group(2);
            return new ScriptStatement(ScriptStatement.Type.LOOP, line, lineNumber, indentLevel)
                .setLoopType(loopType)
                .setCondition(parseLoopCondition(loopCondition));
        }
        
        // Command handlers
        Matcher commandMatcher = COMMAND_PATTERN.matcher(line);
        if (commandMatcher.matches()) {
            String command = commandMatcher.group(1);
            return new ScriptStatement(ScriptStatement.Type.COMMAND, line, lineNumber, indentLevel)
                .setCommand(command);
        }
        
        // Variable operations
        Matcher varSetMatcher = VARIABLE_SET_PATTERN.matcher(line);
        if (varSetMatcher.matches()) {
            String variable = varSetMatcher.group(1);
            String value = varSetMatcher.group(2);
            return new ScriptStatement(ScriptStatement.Type.VARIABLE_SET, line, lineNumber, indentLevel)
                .setVariable(variable)
                .setValue(parseValue(value));
        }
        
        Matcher varAddMatcher = VARIABLE_ADD_PATTERN.matcher(line);
        if (varAddMatcher.matches()) {
            String value = varAddMatcher.group(1);
            String variable = varAddMatcher.group(2);
            return new ScriptStatement(ScriptStatement.Type.VARIABLE_ADD, line, lineNumber, indentLevel)
                .setVariable(variable)
                .setValue(parseValue(value));
        }
        
        // Player actions
        Matcher sendMatcher = SEND_PATTERN.matcher(line);
        if (sendMatcher.matches()) {
            String message = sendMatcher.group(1);
            String target = sendMatcher.group(2);
            return new ScriptStatement(ScriptStatement.Type.SEND_MESSAGE, line, lineNumber, indentLevel)
                .setMessage(message)
                .setTarget(target);
        }
        
        Matcher broadcastMatcher = BROADCAST_PATTERN.matcher(line);
        if (broadcastMatcher.matches()) {
            String message = broadcastMatcher.group(1);
            return new ScriptStatement(ScriptStatement.Type.BROADCAST, line, lineNumber, indentLevel)
                .setMessage(message);
        }
        
        Matcher giveMatcher = GIVE_PATTERN.matcher(line);
        if (giveMatcher.matches()) {
            String target = giveMatcher.group(1);
            String amount = giveMatcher.group(2);
            String item = giveMatcher.group(3);
            return new ScriptStatement(ScriptStatement.Type.GIVE_ITEM, line, lineNumber, indentLevel)
                .setTarget(target)
                .setAmount(amount != null ? amount.trim() : "1")
                .setItem(item);
        }
        
        Matcher teleportMatcher = TELEPORT_PATTERN.matcher(line);
        if (teleportMatcher.matches()) {
            String target = teleportMatcher.group(1);
            String location = teleportMatcher.group(2);
            return new ScriptStatement(ScriptStatement.Type.TELEPORT, line, lineNumber, indentLevel)
                .setTarget(target)
                .setLocation(location);
        }
        
        // Animation commands
        Matcher animateMatcher = ANIMATE_PATTERN.matcher(line);
        if (animateMatcher.matches()) {
            String animationType = animateMatcher.group(1);
            String parameters = animateMatcher.group(2);
            return new ScriptStatement(ScriptStatement.Type.ANIMATE, line, lineNumber, indentLevel)
                .setAnimationType(animationType)
                .setParameters(parseParameters(parameters));
        }
        
        // Generic action statement
        return new ScriptStatement(ScriptStatement.Type.ACTION, line, lineNumber, indentLevel);
    }
    
    /**
     * Parse event type from event declaration
     */
    private String parseEventType(String eventDeclaration) {
        // Handle different event types
        eventDeclaration = eventDeclaration.toLowerCase().trim();
        
        if (eventDeclaration.startsWith("player join")) {
            return "player_join";
        } else if (eventDeclaration.startsWith("player leave") || eventDeclaration.startsWith("player quit")) {
            return "player_leave";
        } else if (eventDeclaration.startsWith("player death")) {
            return "player_death";
        } else if (eventDeclaration.startsWith("block break")) {
            return "block_break";
        } else if (eventDeclaration.startsWith("block place")) {
            return "block_place";
        } else if (eventDeclaration.startsWith("damage")) {
            return "entity_damage";
        } else if (eventDeclaration.startsWith("chat")) {
            return "player_chat";
        }
        
        return eventDeclaration;
    }
    
    /**
     * Parse function parameters
     */
    private List<String> parseParameters(String parameterString) {
        List<String> parameters = new ArrayList<>();
        
        if (parameterString != null && !parameterString.trim().isEmpty()) {
            String[] parts = parameterString.split(",");
            for (String part : parts) {
                parameters.add(part.trim());
            }
        }
        
        return parameters;
    }
    
    /**
     * Parse condition expression
     */
    private String parseCondition(String condition) {
        // Handle complex conditions with natural language
        condition = condition.trim();
        
        // Replace natural language operators
        condition = condition.replaceAll("\\bis\\s+not\\b", "!=");
        condition = condition.replaceAll("\\bis\\b", "==");
        condition = condition.replaceAll("\\bhas\\s+permission\\b", "hasPermission");
        condition = condition.replaceAll("\\bhas\\b", "contains");
        condition = condition.replaceAll("\\band\\b", "&&");
        condition = condition.replaceAll("\\bor\\b", "||");
        
        return condition;
    }
    
    /**
     * Parse loop condition
     */
    private String parseLoopCondition(String loopCondition) {
        loopCondition = loopCondition.trim();
        
        // Handle different loop types
        if (loopCondition.startsWith("all players")) {
            return "players:*";
        } else if (loopCondition.startsWith("all entities")) {
            return "entities:*";
        } else if (loopCondition.matches("\\d+\\s+times")) {
            String count = loopCondition.replaceAll("\\s+times", "");
            return "times:" + count;
        }
        
        return loopCondition;
    }
    
    /**
     * Parse value expression
     */
    private String parseValue(String value) {
        value = value.trim();
        
        // Handle string literals
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        
        // Handle variable references
        if (value.startsWith("{") && value.endsWith("}")) {
            return value;
        }
        
        // Handle player properties
        if (value.contains("'s ")) {
            return value;
        }
        
        return value;
    }
    
    /**
     * Get indentation level of a line
     */
    private int getIndentLevel(String line) {
        int indent = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indent++;
            } else if (c == '\t') {
                indent += 4; // Tab = 4 spaces
            } else {
                break;
            }
        }
        return indent / 4; // Normalize to tab levels
    }
    
    /**
     * Find the appropriate parent block for a statement
     */
    private ScriptStatement findParentBlock(List<ScriptStatement> statements, int indentLevel) {
        for (int i = statements.size() - 1; i >= 0; i--) {
            ScriptStatement statement = statements.get(i);
            if (statement.getIndentLevel() < indentLevel && statement.canHaveChildren()) {
                return statement;
            }
        }
        return null;
    }
}