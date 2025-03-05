package dev.tbm00.spigot.displayshopaddon64;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigHandler {
    private final DisplayShopAddon64 javaPlugin;
    private String chatPrefix;
    private boolean featureEnabled = false;
    private int dsMaxStoredBalance;
    private String guiDefaultCategory;

    /**
     * Constructs a ConfigHandler instance.
     * Loads configuration values for the plugin.
     *
     * @param javaPlugin the main plugin instance
     */
    public ConfigHandler(DisplayShopAddon64 javaPlugin) {
        this.javaPlugin = javaPlugin;
        try {
            loadLanguageSection();
            loadFeatureSection();
        } catch (Exception e) {
            javaPlugin.getLogger().warning("Caught exception loading config: " + e.getMessage());
        }
    }

    /**
     * Loads the "lang" section of the configuration.
     */
    private void loadLanguageSection() {
        ConfigurationSection section = javaPlugin.getConfig().getConfigurationSection("lang");
        if (section!=null)
            chatPrefix = section.contains("prefix") ? section.getString("prefix") : null;
    }

    /**
     * Loads the "feature" section of the configuration.
     */
    private void loadFeatureSection() {
        ConfigurationSection section = javaPlugin.getConfig().getConfigurationSection("feature");
        if (section!=null) {
            featureEnabled = section.contains("enabled") ? section.getBoolean("enabled") : false;
            dsMaxStoredBalance = section.contains("dsMaxStoredBalance") ? section.getInt("dsMaxStoredBalance") : 20000000;
            guiDefaultCategory = section.contains("guiDefaultCategory") ? section.getString("guiDefaultCategory") : "shoppog";
        }
    }

    public String getChatPrefix() {
        return chatPrefix;
    }

    public boolean isFeatureEnabled() {
        return featureEnabled;
    }

    public int getDSMaxStoredBalance() {
        return dsMaxStoredBalance;
    }

    public String getGUIDefaultCategory() {
        return guiDefaultCategory;
    }
}