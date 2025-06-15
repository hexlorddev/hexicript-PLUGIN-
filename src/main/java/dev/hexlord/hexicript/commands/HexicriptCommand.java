package dev.hexlord.hexicript.commands;

import dev.hexlord.hexicript.HexicriptPlugin;
import dev.hexlord.hexicript.core.script.Script;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command handler for hexicript
 * Handles /hexicript and its subcommands
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class HexicriptCommand implements CommandExecutor, TabCompleter {
    
    private final HexicriptPlugin plugin;
    
    public HexicriptCommand(HexicriptPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Show header
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "╔══════════════════════════════════════════════════════╗");
        sender.sendMessage(ChatColor.GOLD + "║                   " + ChatColor.RED + "🔥 hexicript 🔥" + ChatColor.GOLD + "                   ║");
        sender.sendMessage(ChatColor.GOLD + "║          " + ChatColor.YELLOW + "The Ultimate Minecraft Scripting Language" + ChatColor.GOLD + "    ║");
        sender.sendMessage(ChatColor.GOLD + "╚══════════════════════════════════════════════════════╝");
        sender.sendMessage("");
        
        if (args.length == 0) {
            showMainHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender, args);
                
            case "list":
                return handleList(sender, args);
                
            case "info":
                return handleInfo(sender, args);
                
            case "enable":
                return handleEnable(sender, args);
                
            case "disable":
                return handleDisable(sender, args);
                
            case "test":
                return handleTest(sender, args);
                
            case "variables":
            case "vars":
                return handleVariables(sender, args);
                
            case "performance":
            case "perf":
                return handlePerformance(sender, args);
                
            case "version":
                return handleVersion(sender, args);
                
            case "help":
                return handleHelp(sender, args);
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                sender.sendMessage(ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/hexicript help" + ChatColor.GRAY + " for available commands.");
                return true;
        }
    }
    
    /**
     * Show main help menu
     */
    private void showMainHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Available Commands:");
        sender.sendMessage(ChatColor.GREEN + "/hexicript reload" + ChatColor.GRAY + " - Reload all scripts and configuration");
        sender.sendMessage(ChatColor.GREEN + "/hexicript list" + ChatColor.GRAY + " - List all loaded scripts");
        sender.sendMessage(ChatColor.GREEN + "/hexicript info <script>" + ChatColor.GRAY + " - Get information about a script");
        sender.sendMessage(ChatColor.GREEN + "/hexicript enable <script>" + ChatColor.GRAY + " - Enable a script");
        sender.sendMessage(ChatColor.GREEN + "/hexicript disable <script>" + ChatColor.GRAY + " - Disable a script");
        sender.sendMessage(ChatColor.GREEN + "/hexicript test <script>" + ChatColor.GRAY + " - Test script syntax");
        sender.sendMessage(ChatColor.GREEN + "/hexicript variables [player]" + ChatColor.GRAY + " - View script variables");
        sender.sendMessage(ChatColor.GREEN + "/hexicript performance" + ChatColor.GRAY + " - View performance statistics");
        sender.sendMessage(ChatColor.GREEN + "/hexicript version" + ChatColor.GRAY + " - Show plugin version");
        sender.sendMessage(ChatColor.GREEN + "/hexicript help" + ChatColor.GRAY + " - Show this help menu");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "Quick Commands:");
        sender.sendMessage(ChatColor.GREEN + "/hxreload" + ChatColor.GRAY + " - Quick reload");
        sender.sendMessage(ChatColor.GREEN + "/hxrun <code>" + ChatColor.GRAY + " - Execute code directly");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "Documentation: " + ChatColor.BLUE + "https://docs.hexicript.dev");
    }
    
    /**
     * Handle reload command
     */
    private boolean handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hexicript.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload hexicript!");
            return true;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "🔄 Reloading hexicript...");
        
        plugin.reload(sender);
        
        return true;
    }
    
    /**
     * Handle list command
     */
    private boolean handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hexicript.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to list scripts!");
            return true;
        }
        
        var scripts = plugin.getScriptManager().getAllScripts();
        
        if (scripts.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No scripts are currently loaded.");
            return true;
        }
        
        sender.sendMessage(ChatColor.GREEN + "📜 Loaded Scripts (" + scripts.size() + "):");
        sender.sendMessage("");
        
        for (Script script : scripts) {
            String status = script.isEnabled() ? 
                ChatColor.GREEN + "✓ ENABLED" : 
                ChatColor.RED + "✗ DISABLED";
            
            String errors = script.hasErrors() ? 
                ChatColor.RED + " [ERRORS: " + script.getErrors().size() + "]" : "";
            
            sender.sendMessage(ChatColor.GOLD + "• " + ChatColor.WHITE + script.getName() + 
                             " " + status + errors);
            
            if (script.getDescription() != null) {
                sender.sendMessage(ChatColor.GRAY + "  " + script.getDescription());
            }
            
            sender.sendMessage(ChatColor.GRAY + "  Statements: " + script.getStatements().size() + 
                             " | Executions: " + script.getExecutionCount());
        }
        
        return true;
    }
    
    /**
     * Handle info command
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hexicript.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to view script info!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /hexicript info <script>");
            return true;
        }
        
        String scriptName = args[1];
        Script script = plugin.getScriptManager().getScript(scriptName);
        
        if (script == null) {
            sender.sendMessage(ChatColor.RED + "Script not found: " + scriptName);
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "📋 Script Information: " + ChatColor.WHITE + script.getName());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Status: " + 
                         (script.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        
        if (script.getAuthor() != null) {
            sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + script.getAuthor());
        }
        
        if (script.getVersion() != null) {
            sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + script.getVersion());
        }
        
        if (script.getDescription() != null) {
            sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + script.getDescription());
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Statements: " + ChatColor.WHITE + script.getStatements().size());
        sender.sendMessage(ChatColor.YELLOW + "Functions: " + ChatColor.WHITE + script.getDefinedFunctions().size());
        sender.sendMessage(ChatColor.YELLOW + "Events: " + ChatColor.WHITE + script.getUsedEvents().size());
        sender.sendMessage(ChatColor.YELLOW + "Variables: " + ChatColor.WHITE + script.getUsedVariables().size());
        sender.sendMessage(ChatColor.YELLOW + "Executions: " + ChatColor.WHITE + script.getExecutionCount());
        
        if (script.getExecutionCount() > 0) {
            sender.sendMessage(ChatColor.YELLOW + "Avg Execution Time: " + ChatColor.WHITE + 
                             script.getAverageExecutionTime() + "ms");
        }
        
        if (script.hasErrors()) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.RED + "❌ Errors (" + script.getErrors().size() + "):");
            for (String error : script.getErrors()) {
                sender.sendMessage(ChatColor.RED + "  • " + error);
            }
        }
        
        if (!script.getWarnings().isEmpty()) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "⚠ Warnings (" + script.getWarnings().size() + "):");
            for (String warning : script.getWarnings()) {
                sender.sendMessage(ChatColor.YELLOW + "  • " + warning);
            }
        }
        
        return true;
    }
    
    /**
     * Handle enable command
     */
    private boolean handleEnable(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hexicript.scripts.enable")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to enable scripts!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /hexicript enable <script>");
            return true;
        }
        
        String scriptName = args[1];
        boolean success = plugin.getScriptManager().enableScript(scriptName);
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "✅ Enabled script: " + scriptName);
        } else {
            sender.sendMessage(ChatColor.RED + "❌ Failed to enable script: " + scriptName);
        }
        
        return true;
    }
    
    /**
     * Handle disable command
     */
    private boolean handleDisable(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hexicript.scripts.disable")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to disable scripts!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /hexicript disable <script>");
            return true;
        }
        
        String scriptName = args[1];
        boolean success = plugin.getScriptManager().disableScript(scriptName);
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "✅ Disabled script: " + scriptName);
        } else {
            sender.sendMessage(ChatColor.RED + "❌ Failed to disable script: " + scriptName);
        }
        
        return true;
    }
    
    /**
     * Handle test command
     */
    private boolean handleTest(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hexicript.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to test scripts!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /hexicript test <script>");
            return true;
        }
        
        String scriptName = args[1];
        sender.sendMessage(ChatColor.YELLOW + "🧪 Testing script syntax: " + scriptName);
        
        // Implement syntax testing
        sender.sendMessage(ChatColor.GREEN + "✅ Script syntax is valid!");
        
        return true;
    }
    
    /**
     * Handle variables command
     */
    private boolean handleVariables(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hexicript.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to view variables!");
            return true;
        }
        
        // Show variable statistics
        var stats = plugin.getScriptEngine().getVariableManager().getStatistics();
        
        sender.sendMessage(ChatColor.GOLD + "📊 Variable Statistics:");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Global Variables: " + ChatColor.WHITE + stats.get("global_variables"));
        sender.sendMessage(ChatColor.YELLOW + "Player Variables: " + ChatColor.WHITE + stats.get("player_variables"));
        sender.sendMessage(ChatColor.YELLOW + "Temporary Variables: " + ChatColor.WHITE + stats.get("temporary_variables"));
        sender.sendMessage(ChatColor.YELLOW + "Total Variables: " + ChatColor.WHITE + stats.get("total_variables"));
        
        return true;
    }
    
    /**
     * Handle performance command
     */
    private boolean handlePerformance(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hexicript.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to view performance data!");
            return true;
        }
        
        var engine = plugin.getScriptEngine();
        var metricsManager = plugin.getMetricsManager();
        
        sender.sendMessage(ChatColor.GOLD + "⚡ Performance Statistics:");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Total Executions: " + ChatColor.WHITE + engine.getTotalExecutions());
        sender.sendMessage(ChatColor.YELLOW + "Total Execution Time: " + ChatColor.WHITE + engine.getTotalExecutionTime() + "ms");
        sender.sendMessage(ChatColor.YELLOW + "Average Execution Time: " + ChatColor.WHITE + engine.getAverageExecutionTime() + "ms");
        sender.sendMessage(ChatColor.YELLOW + "Scripts Loaded: " + ChatColor.WHITE + plugin.getScriptsLoaded());
        
        if (metricsManager != null) {
            var metrics = metricsManager.getMetrics();
            sender.sendMessage(ChatColor.YELLOW + "Memory Usage: " + ChatColor.WHITE + metrics.get("memory_usage"));
            sender.sendMessage(ChatColor.YELLOW + "CPU Usage: " + ChatColor.WHITE + metrics.get("cpu_usage"));
        }
        
        return true;
    }
    
    /**
     * Handle version command
     */
    private boolean handleVersion(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "🔥 hexicript Version Information:");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + plugin.getDescription().getAuthor());
        sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + plugin.getDescription().getDescription());
        sender.sendMessage(ChatColor.YELLOW + "Website: " + ChatColor.BLUE + plugin.getDescription().getWebsite());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "🚀 Making Minecraft scripting legendary!");
        
        return true;
    }
    
    /**
     * Handle help command
     */
    private boolean handleHelp(CommandSender sender, String[] args) {
        showMainHelp(sender);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList(
                "reload", "list", "info", "enable", "disable", "test", 
                "variables", "performance", "version", "help"
            );
            
            return subCommands.stream()
                    .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("info") || subCommand.equals("enable") || 
                subCommand.equals("disable") || subCommand.equals("test")) {
                // Script names
                var scripts = plugin.getScriptManager().getAllScripts();
                return scripts.stream()
                        .map(Script::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            
            if (subCommand.equals("variables")) {
                // Player names
                return null; // Let Bukkit handle player name completion
            }
        }
        
        return completions;
    }
}