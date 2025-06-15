package dev.hexlord.hexicript.config;

import dev.hexlord.hexicript.HexicriptPlugin;
import dev.hexlord.hexicript.utils.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Handles all plugin configuration
 */
public class PluginConfig {
    
    private final HexicriptPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    
    // Configuration values
    private boolean debugMode;
    private String scriptExtension;
    private List<String> allowedFileExtensions;
    private int maxScriptExecutionTime;
    private boolean enableMetrics;
    private boolean autoUpdate;
    private boolean enableAutoReload;
    private int autoReloadInterval;
    
    public PluginConfig(HexicriptPlugin plugin) {
        this.plugin = plugin;
        setupConfig();
    }
    
    /**
     * Set up the configuration file
     */
    private void setupConfig() {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                Logger.severe("Could not create plugin directory!");
                return;
            }
        }
        
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        // Create default config if it doesn't exist
        if (!configFile.exists()) {
            try {
                if (configFile.createNewFile()) {
                    Logger.info("Created default config file");
                }
            } catch (IOException e) {
                Logger.severe("Could not create config file: " + e.getMessage());
                return;
            }
        }
        
        reloadConfig();
    }
    
    /**
     * Reload the configuration from disk
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Set default values if they don't exist
        setDefaults();
        
        // Load values
        loadValues();
        
        try {
            // Save the config to update any missing values
            config.save(configFile);
        } catch (IOException e) {
            Logger.severe("Could not save config: " + e.getMessage());
        }
    }
    
    /**
     * Set default configuration values
     */
    private void setDefaults() {
        config.addDefault("debug", false);
        config.addDefault("script_extension", ".hxs");
        config.addDefault("allowed_file_extensions", List.of(".hxs", ".txt"));
        config.addDefault("max_script_execution_time", 5000); // 5 seconds
        config.addDefault("enable_metrics", true);
        config.addDefault("auto_update", true);
        config.addDefault("auto_reload.enabled", true);
        config.addDefault("auto_reload.interval", 300); // 5 minutes
        
        config.options().copyDefaults(true);
    }
    
    /**
     * Load configuration values into memory
     */
    private void loadValues() {
        debugMode = config.getBoolean("debug", false);
        scriptExtension = config.getString("script_extension", ".hxs");
        allowedFileExtensions = config.getStringList("allowed_file_extensions");
        maxScriptExecutionTime = config.getInt("max_script_execution_time", 5000);
        enableMetrics = config.getBoolean("enable_metrics", true);
        autoUpdate = config.getBoolean("auto_update", true);
        enableAutoReload = config.getBoolean("auto_reload.enabled", true);
        autoReloadInterval = config.getInt("auto_reload.interval", 300);
    }
    
    // Getters
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public String getScriptExtension() {
        return scriptExtension;
    }
    
    public List<String> getAllowedFileExtensions() {
        return allowedFileExtensions;
    }
    
    public int getMaxScriptExecutionTime() {
        return maxScriptExecutionTime;
    }
    
    public boolean isMetricsEnabled() {
        return enableMetrics;
    }
    
    public boolean isAutoUpdateEnabled() {
        return autoUpdate;
    }
    
    public boolean isAutoReloadEnabled() {
        return enableAutoReload;
    }
    
    public int getAutoReloadInterval() {
        return autoReloadInterval;
    }
    
    /**
     * Get the raw configuration
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
