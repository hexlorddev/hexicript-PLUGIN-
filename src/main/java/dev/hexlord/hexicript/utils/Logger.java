package dev.hexlord.hexicript.utils;

import dev.hexlord.hexicript.HexicriptPlugin;
import org.bukkit.Bukkit;

/**
 * Enhanced logging utility for hexicript
 * Provides colored console output and different log levels
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class Logger {
    
    private static final String PREFIX = "§6[§bhexicript§6]§r ";
    private static boolean debugMode = false;
    
    /**
     * Set debug mode
     */
    public static void setDebugMode(boolean debug) {
        debugMode = debug;
    }
    
    /**
     * Log info message
     */
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }
    
    /**
     * Log warning message
     */
    public static void warning(String message) {
        log(LogLevel.WARNING, message);
    }
    
    /**
     * Log error message
     */
    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }
    
    /**
     * Log debug message (only shown if debug mode is enabled)
     */
    public static void debug(String message) {
        if (debugMode) {
            log(LogLevel.DEBUG, message);
        }
    }
    
    /**
     * Log success message
     */
    public static void success(String message) {
        log(LogLevel.SUCCESS, message);
    }
    
    /**
     * Log message with specified level
     */
    private static void log(LogLevel level, String message) {
        String coloredMessage = PREFIX + level.getColor() + level.getPrefix() + message;
        
        // Send to console
        Bukkit.getConsoleSender().sendMessage(coloredMessage);
        
        // Also log to plugin logger if available
        HexicriptPlugin plugin = HexicriptPlugin.getInstance();
        if (plugin != null) {
            switch (level) {
                case ERROR:
                    plugin.getLogger().severe(stripColors(message));
                    break;
                case WARNING:
                    plugin.getLogger().warning(stripColors(message));
                    break;
                case DEBUG:
                    plugin.getLogger().fine(stripColors(message));
                    break;
                default:
                    plugin.getLogger().info(stripColors(message));
                    break;
            }
        }
    }
    
    /**
     * Strip color codes from message
     */
    private static String stripColors(String message) {
        return message.replaceAll("§[0-9a-fk-or]", "");
    }
    
    /**
     * Log levels with colors and prefixes
     */
    private enum LogLevel {
        INFO("§f", ""),
        WARNING("§e", "⚠ "),
        ERROR("§c", "❌ "),
        DEBUG("§7", "🐛 "),
        SUCCESS("§a", "✅ ");
        
        private final String color;
        private final String prefix;
        
        LogLevel(String color, String prefix) {
            this.color = color;
            this.prefix = prefix;
        }
        
        public String getColor() { return color; }
        public String getPrefix() { return prefix; }
    }
}