package dev.hexlord.hexicript;

import dev.hexlord.hexicript.commands.HexicriptCommand;
import dev.hexlord.hexicript.commands.HexicriptReloadCommand;
import dev.hexlord.hexicript.commands.HexicriptRunCommand;
import dev.hexlord.hexicript.config.ConfigManager;
import dev.hexlord.hexicript.core.ScriptEngine;
import dev.hexlord.hexicript.core.ScriptManager;
import dev.hexlord.hexicript.events.HexicriptEventManager;
import dev.hexlord.hexicript.integrations.IntegrationManager;
import dev.hexlord.hexicript.metrics.MetricsManager;
import dev.hexlord.hexicript.storage.DataManager;
import dev.hexlord.hexicript.utils.Logger;
import dev.hexlord.hexicript.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Main plugin class for hexicript - The Ultimate Minecraft Scripting Language
 * 
 * @author hexlorddev
 * @version 2.0.0
 */
public class HexicriptPlugin extends JavaPlugin {
    
    // Plugin instance
    private static HexicriptPlugin instance;
    
    // Core managers
    private ConfigManager configManager;
    private ScriptEngine scriptEngine;
    private ScriptManager scriptManager;
    private HexicriptEventManager eventManager;
    private DataManager dataManager;
    private MetricsManager metricsManager;
    private IntegrationManager integrationManager;
    
    // Plugin state
    private boolean isEnabled = false;
    private long startupTime;
    private int scriptsLoaded = 0;
    
    /**
     * Get the plugin instance
     */
    public static HexicriptPlugin getInstance() {
        return instance;
    }
    
    @Override
    public void onLoad() {
        instance = this;
        startupTime = System.currentTimeMillis();
        
        // Create plugin directories
        createDirectories();
        
        Logger.info("hexicript v" + getDescription().getVersion() + " is loading...");
        Logger.info("Created by " + getDescription().getAuthor());
    }
    
    @Override
    public void onEnable() {
        try {
            // Initialize core components
            initializeCore();
            
            // Load configuration
            loadConfiguration();
            
            // Initialize managers
            initializeManagers();
            
            // Register commands
            registerCommands();
            
            // Load scripts
            loadScripts();
            
            // Register event listeners
            registerEventListeners();
            
            // Initialize integrations
            initializeIntegrations();
            
            // Start metrics collection
            startMetrics();
            
            // Check for updates
            checkForUpdates();
            
            // Mark as enabled
            isEnabled = true;
            
            // Startup complete message
            long loadTime = System.currentTimeMillis() - startupTime;
            Logger.info("");
            Logger.info("§6╔══════════════════════════════════════════════════════════════╗");
            Logger.info("§6║                      §c🔥 hexicript 🔥                       §6║");
            Logger.info("§6║              §eThe Ultimate Minecraft Scripting Language    §6║");
            Logger.info("§6║                                                              §6║");
            Logger.info("§6║  §aVersion: §f" + String.format("%-10s", getDescription().getVersion()) + 
                       "§aLoaded Scripts: §f" + String.format("%-10s", scriptsLoaded) + "      §6║");
            Logger.info("§6║  §aLoad Time: §f" + String.format("%-8s", loadTime + "ms") + 
                       "§aIntegrations: §f" + String.format("%-10s", getEnabledIntegrations()) + "    §6║");
            Logger.info("§6║                                                              §6║");
            Logger.info("§6║                 §dMaking Minecraft scripting legendary!     §6║");
            Logger.info("§6╚══════════════════════════════════════════════════════════════╝");
            Logger.info("");
            
        } catch (Exception e) {
            Logger.error("Failed to enable hexicript: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        isEnabled = false;
        
        Logger.info("Disabling hexicript...");
        
        // Stop metrics collection
        if (metricsManager != null) {
            metricsManager.shutdown();
        }
        
        // Save all data
        if (dataManager != null) {
            dataManager.saveAll();
        }
        
        // Stop script engine
        if (scriptEngine != null) {
            scriptEngine.shutdown();
        }
        
        // Unregister all listeners
        if (eventManager != null) {
            eventManager.shutdown();
        }
        
        // Close integrations
        if (integrationManager != null) {
            integrationManager.shutdown();
        }
        
        Logger.info("§6🔥 hexicript disabled. Thanks for using the ultimate scripting language! 🔥");
    }
    
    /**
     * Create necessary plugin directories
     */
    private void createDirectories() {
        File[] directories = {
            new File(getDataFolder(), "scripts"),
            new File(getDataFolder(), "aliases"),
            new File(getDataFolder(), "functions"),
            new File(getDataFolder(), "modules"),
            new File(getDataFolder(), "logs"),
            new File(getDataFolder(), "backups"),
            new File(getDataFolder(), "cache"),
            new File(getDataFolder(), "data")
        };
        
        for (File dir : directories) {
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }
    
    /**
     * Initialize core components
     */
    private void initializeCore() {
        Logger.info("Initializing core components...");
        
        // Initialize configuration manager
        configManager = new ConfigManager(this);
        
        // Initialize script engine
        scriptEngine = new ScriptEngine(this);
        
        // Initialize data manager
        dataManager = new DataManager(this);
        
        Logger.info("Core components initialized.");
    }
    
    /**
     * Load configuration files
     */
    private void loadConfiguration() {
        Logger.info("Loading configuration...");
        
        configManager.loadConfig();
        configManager.loadAliases();
        configManager.loadFunctions();
        
        Logger.info("Configuration loaded successfully.");
    }
    
    /**
     * Initialize all core managers with proper error handling
     */
    private void initializeManagers() {
        try {
            Logger.info("Initializing core managers...");
            
            // Configuration manager must be initialized first
            configManager = new ConfigManager(this);
            configManager.loadConfig();
            
            // Initialize metrics before other managers
            metricsManager = new MetricsManager(this);
            metricsManager.start();
            
            // Initialize core components
            variableManager = new VariableManager(this);
            scriptEngine = new ScriptEngine(this);
            scriptManager = new ScriptManager(this);
            eventManager = new HexicriptEventManager(this);
            dataManager = new DataManager(this);
            integrationManager = new IntegrationManager(this);
            
            Logger.info("All managers initialized successfully");
            
        } catch (Exception e) {
            Logger.severe("Failed to initialize managers: " + e.getMessage());
            e.printStackTrace();
            // Disable plugin if critical managers fail to initialize
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    /**
     * Register all plugin commands with proper permission checking
     */
    private void registerCommands() {
        try {
            Logger.info("Registering commands...");
            
            // Main hexicript command with subcommands
            HexicriptCommand hexicriptCommand = new HexicriptCommand(this);
            getCommand("hexicript").setExecutor(hexicriptCommand);
            getCommand("hexicript").setTabCompleter(hexicriptCommand);
            
            // Reload command
            HexicriptReloadCommand reloadCommand = new HexicriptReloadCommand(this);
            getCommand("hexicriptreload").setExecutor(reloadCommand);
            getCommand("hexicriptreload").setPermission("hexicript.admin.reload");
            getCommand("hexicriptreload").setPermissionMessage(
                ChatColor.RED + "You don't have permission to reload hexicript!");
            
            // Run command
            HexicriptRunCommand runCommand = new HexicriptRunCommand(this);
            getCommand("hexicriptrun").setExecutor(runCommand);
            getCommand("hexicriptrun").setTabCompleter(runCommand);
            getCommand("hexicriptrun").setPermission("hexicript.command.run");
            getCommand("hexicriptrun").setPermissionMessage(
                ChatColor.RED + "You don't have permission to run hexicript scripts!");
            
            Logger.info("Commands registered successfully");
            
        } catch (Exception e) {
            Logger.severe("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load all scripts from the scripts directory with validation
     */
    private void loadScripts() {
        try {
            Logger.info("Loading scripts...");
            
            // Ensure scripts directory exists
            File scriptsDir = new File(getDataFolder(), "scripts");
            if (!scriptsDir.exists()) {
                if (scriptsDir.mkdirs()) {
                    Logger.info("Created scripts directory at: " + scriptsDir.getAbsolutePath());
                    scriptsLoaded = 0;
                    return;
                } else {
                    Logger.severe("Failed to create scripts directory!");
                    return;
                }
            }
            
            // Load scripts
            int loaded = scriptManager.loadScripts();
            int failed = scriptManager.getFailedScripts();
            
            // Log results
            if (failed > 0) {
                Logger.warning(String.format(
                    "Loaded %d scripts, %d failed to load. Check the console for errors.", 
                    loaded, failed));
            } else {
                Logger.info(String.format("Successfully loaded %d scripts", loaded));
            }
            
            scriptsLoaded = loaded;
            
            // Notify about new updates if available
            checkForUpdates();
            
        } catch (Exception e) {
            Logger.severe("An error occurred while loading scripts: " + e.getMessage());
            e.printStackTrace();
            scriptsLoaded = 0;
        }
    }
    /**
     * Register all event listeners with proper error handling
     */
    private void registerEventListeners() {
        try {
            Logger.info("Registering event listeners...");
            
            // Register core event listeners
            eventManager.registerListeners();
            
            // Register script-specific event handlers
            if (scriptManager != null) {
                scriptManager.registerEventHandlers();
            }
            
            // Register integration event listeners
            if (integrationManager != null) {
                integrationManager.registerEventListeners();
            }
            
            Logger.info("Event listeners registered successfully");
            
        } catch (Exception e) {
            Logger.severe("Failed to register event listeners: " + e.getMessage());
            e.printStackTrace();
            
            // Try to continue even if event registration fails
            Logger.warning("Continuing with limited functionality due to event registration errors");
        }
    }
    
    /**
     * Initialize all plugin integrations with proper error handling
     */
    private void initializeIntegrations() {
        try {
            Logger.info("Initializing integrations...");
            
            // Initialize core integrations
            int successCount = integrationManager.initializeIntegrations();
            int totalIntegrations = integrationManager.getAvailableIntegrationsCount();
            
            // Log integration status
            if (successCount == totalIntegrations) {
                Logger.info(String.format("Successfully initialized %d/%d integrations", 
                    successCount, totalIntegrations));
            } else {
                int failed = totalIntegrations - successCount;
                Logger.warning(String.format(
                    "Initialized %d/%d integrations, %d failed. Some features may be limited.",
                    successCount, totalIntegrations, failed));
                
                // Log which integrations failed
                integrationManager.getFailedIntegrations().forEach(integration -> 
                    Logger.warning(" - Failed to initialize integration: " + integration)
                );
            }
            
            // Register any integration-specific commands
            integrationManager.registerIntegrationCommands();
            
        } catch (Exception e) {
            Logger.severe("Error initializing integrations: " + e.getMessage());
            e.printStackTrace();
            Logger.warning("Continuing with limited functionality due to integration errors");
        }
    }
    
    /**
     * Start metrics collection
     */
    private void startMetrics() {
        if (configManager.getConfig().getBoolean("performance.monitoring.enable_metrics", true)) {
            Logger.info("Starting metrics collection...");
            
            metricsManager.startCollection();
            
            Logger.info("Metrics collection started.");
        }
    }
    
    /**
     * Check for plugin updates
     */
    private void checkForUpdates() {
        if (configManager.getConfig().getBoolean("network.updates.check_for_updates", true)) {
            Logger.info("Checking for updates...");
            
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                UpdateChecker checker = new UpdateChecker(this);
                checker.checkForUpdates().thenAccept(hasUpdate -> {
                    if (hasUpdate) {
                        Logger.info("§e⚠ A new version of hexicript is available!");
                        Logger.info("§eDownload it from: https://github.com/hexlorddev/hexicript/releases");
                    } else {
                        Logger.info("hexicript is up to date.");
                    }
                });
            });
        }
    }
    
    /**
     * Get the number of enabled integrations
     */
    private int getEnabledIntegrations() {
        if (integrationManager == null) {
            return 0;
        }
        return integrationManager.getEnabledIntegrations().size();
    }
    
    /**
     * Reload the plugin
     */
    public void reload(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Reloading hexicript...");
        
        long reloadStart = System.currentTimeMillis();
        
        try {
            // Reload configuration
            configManager.reloadConfig();
            
            // Reload scripts
            scriptManager.reloadAllScripts().thenAccept(count -> {
                scriptsLoaded = count;
                
                long reloadTime = System.currentTimeMillis() - reloadStart;
                
                sender.sendMessage(ChatColor.GREEN + "✅ hexicript reloaded successfully!");
                sender.sendMessage(ChatColor.GRAY + "Reload time: " + reloadTime + "ms");
                sender.sendMessage(ChatColor.GRAY + "Scripts loaded: " + count);
                
                Logger.info("hexicript reloaded by " + sender.getName() + " in " + reloadTime + "ms");
            }).exceptionally(throwable -> {
                sender.sendMessage(ChatColor.RED + "❌ Failed to reload scripts: " + throwable.getMessage());
                Logger.error("Failed to reload scripts: " + throwable.getMessage());
                return null;
            });
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "❌ Failed to reload hexicript: " + e.getMessage());
            Logger.error("Failed to reload hexicript: " + e.getMessage());
        }
    }
    
    // Getters for managers
    public ConfigManager getConfigManager() { return configManager; }
    public ScriptEngine getScriptEngine() { return scriptEngine; }
    public ScriptManager getScriptManager() { return scriptManager; }
    public HexicriptEventManager getEventManager() { return eventManager; }
    public DataManager getDataManager() { return dataManager; }
    public MetricsManager getMetricsManager() { return metricsManager; }
    public IntegrationManager getIntegrationManager() { return integrationManager; }
    
    // Plugin state getters
    public boolean isPluginEnabled() { return isEnabled; }
    public long getStartupTime() { return startupTime; }
    public int getScriptsLoaded() { return scriptsLoaded; }
}