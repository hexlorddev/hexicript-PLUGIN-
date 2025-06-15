package dev.hexlord.hexicript.core.execution;

/**
 * Represents the result of script execution
 * Contains information about success, failure, or special control flow
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class ExecutionResult {
    
    /**
     * Types of execution results
     */
    public enum Type {
        SUCCESS,    // Normal successful execution
        ERROR,      // Error occurred during execution
        RETURN,     // Return statement encountered
        BREAK,      // Break statement encountered
        CONTINUE,   // Continue statement encountered
        CANCEL      // Event cancelled
    }
    
    private final Type type;
    private final String message;
    private final Object value;
    private final Throwable exception;
    
    private ExecutionResult(Type type, String message, Object value, Throwable exception) {
        this.type = type;
        this.message = message;
        this.value = value;
        this.exception = exception;
    }
    
    /**
     * Create a successful execution result
     */
    public static ExecutionResult success(String message) {
        return new ExecutionResult(Type.SUCCESS, message, null, null);
    }
    
    /**
     * Create a successful execution result with a value
     */
    public static ExecutionResult success(String message, Object value) {
        return new ExecutionResult(Type.SUCCESS, message, value, null);
    }
    
    /**
     * Create an error execution result
     */
    public static ExecutionResult error(String message) {
        return new ExecutionResult(Type.ERROR, message, null, null);
    }
    
    /**
     * Create an error execution result with exception
     */
    public static ExecutionResult error(String message, Throwable exception) {
        return new ExecutionResult(Type.ERROR, message, null, exception);
    }
    
    /**
     * Create a return execution result
     */
    public static ExecutionResult returnValue(Object value) {
        return new ExecutionResult(Type.RETURN, "Return", value, null);
    }
    
    /**
     * Create a break execution result
     */
    public static ExecutionResult breakLoop() {
        return new ExecutionResult(Type.BREAK, "Break", null, null);
    }
    
    /**
     * Create a continue execution result
     */
    public static ExecutionResult continueLoop() {
        return new ExecutionResult(Type.CONTINUE, "Continue", null, null);
    }
    
    /**
     * Create a cancel execution result
     */
    public static ExecutionResult cancel() {
        return new ExecutionResult(Type.CANCEL, "Cancel", null, null);
    }
    
    /**
     * Check if the execution was successful
     */
    public boolean isSuccess() {
        return type == Type.SUCCESS;
    }
    
    /**
     * Check if an error occurred
     */
    public boolean isError() {
        return type == Type.ERROR;
    }
    
    /**
     * Check if this is a control flow result (return, break, continue, cancel)
     */
    public boolean isControlFlow() {
        return type == Type.RETURN || type == Type.BREAK || type == Type.CONTINUE || type == Type.CANCEL;
    }
    
    // Getters
    public Type getType() { return type; }
    public String getMessage() { return message; }
    public Object getValue() { return value; }
    public Throwable getException() { return exception; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExecutionResult{");
        sb.append("type=").append(type);
        sb.append(", message='").append(message).append("'");
        
        if (value != null) {
            sb.append(", value=").append(value);
        }
        
        if (exception != null) {
            sb.append(", exception=").append(exception.getMessage());
        }
        
        sb.append("}");
        return sb.toString();
    }
}