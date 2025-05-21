package dev.tbm00.spigot.displayshopaddon64;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import xzot1k.plugins.ds.DisplayShops;
import dev.tbm00.spigot.rep64.Rep64;

import dev.tbm00.spigot.displayshopaddon64.utils.*;
import dev.tbm00.spigot.displayshopaddon64.command.*;
import dev.tbm00.spigot.displayshopaddon64.listener.PlayerMovement;
import dev.tbm00.spigot.displayshopaddon64.task.DescChangeTask;

public class DisplayShopAddon64 extends JavaPlugin {
    private ConfigHandler configHandler;
    public static DisplayShops dsHook;
    public static Economy ecoHook;
    public static Rep64 repHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        final PluginDescriptionFile pdf = this.getDescription();

        if (getConfig().contains("enabled") && getConfig().getBoolean("enabled")) {
            configHandler = new ConfigHandler(this);

            Utils.init(this, configHandler);
            ShopUtils.init(this, configHandler);
            GuiUtils.init(this, configHandler);
            
            Utils.log(ChatColor.LIGHT_PURPLE,
                    ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-",
                    pdf.getName() + " v" + pdf.getVersion() + " created by tbm00",
                    ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
            );

            setupHooks();

            if (configHandler.isFeatureEnabled()) {
                // Register Listener
                getServer().getPluginManager().registerEvents(new PlayerMovement(), this);

                // Register Commands
                if (isPluginAvailable("ShopGUIPlus")) {
                    cloneSGPCommand("originalsgplus");
                    overrideSGPCommands();
                } else {
                    getCommand("shop").setExecutor(new ShopCmd(this, configHandler));
                    getCommand("sell").setExecutor(new SellCmd(this, configHandler));
                }
                
                getCommand("buy").setExecutor(new BuyCmd(configHandler));
                getCommand("sellgui").setExecutor(new SellGuiCmd(this));
                getCommand("shopadmin").setExecutor(new AdminCmd());

                // Register DS description task
                if (configHandler.isDsDescChanged()) {
                    new DescChangeTask();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void cloneSGPCommand(String newName) {
        Plugin sgp = getServer().getPluginManager().getPlugin("ShopGUIPlus");
        if (sgp == null) return;
        
        try {
            // 1) grab the SimpleCommandMap
            SimpleCommandMap commandMap = (SimpleCommandMap)
                getServer().getClass().getMethod("getCommandMap").invoke(getServer());
            Field knownField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownField.setAccessible(true);
            Map<String, Command> known = (Map<String, Command>) knownField.get(commandMap);

            // 2) find the real PluginCommand whose getName() == "shop"
            PluginCommand original = null;
            for (Command cmd : known.values()) {
                if (cmd instanceof PluginCommand pc
                    && pc.getPlugin().equals(sgp)
                    && pc.getName().equalsIgnoreCase("shop"))
                {
                    original = pc;
                    break;
                }
            }
            if (original == null) {
                getLogger().warning("Could not locate ShopGUIPlus's original /shop command");
                return;
            }

            // 3) reflectively construct a fresh PluginCommand
            Constructor<PluginCommand> ctor =
                PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            ctor.setAccessible(true);
            PluginCommand clone = ctor.newInstance(newName, sgp);

            // 4) copy over data
            clone.setDescription(original.getDescription());
            clone.setUsage(original.getUsage());
            clone.setPermission(original.getPermission());
            clone.setAliases(new ArrayList<>());
            clone.setExecutor(original.getExecutor());
            if (original.getTabCompleter() != null) {
                clone.setTabCompleter(original.getTabCompleter());
            }

            // 5) register it under SGP's namespace
            commandMap.register(sgp.getName(), clone);
            getLogger().info("Cloned ShopGUIPlus's /shop ➔ /" + newName);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to clone ShopGUIPlus's original /shop command", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void overrideSGPCommands() {
        Plugin sgp = getServer().getPluginManager().getPlugin("ShopGUIPlus");
        if (sgp == null) return;

        try {
            // grab commandMap & knownCommands
            SimpleCommandMap commandMap = (SimpleCommandMap)
                getServer().getClass().getMethod("getCommandMap").invoke(getServer());
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            Map<String, Command> known = (Map<String, Command>) f.get(commandMap);

            for (Command cmd : known.values()) {
                if (!(cmd instanceof PluginCommand)) continue;
                PluginCommand pc = (PluginCommand) cmd;
                if (!pc.getPlugin().equals(sgp)) continue;

                // override /shop
                if (pc.getName().equalsIgnoreCase("shop")) {
                    //getLogger().info(pc.getName()+": "+pc.getLabel()+" "+pc.getAliases().subList(0, pc.getAliases().size()-1).toString());
                    ShopCmd shopCmd = new ShopCmd(this, configHandler);
                    pc.setExecutor(shopCmd);
                    pc.setTabCompleter(shopCmd);
                    getLogger().info("Overrode ShopGUIPlus's /shop");
                    continue;
                }

                // override /sell
                if (pc.getName().equalsIgnoreCase("sell")) {
                    //getLogger().info(pc.getName()+": "+pc.getLabel()+" "+pc.getAliases().subList(0, pc.getAliases().size()-1).toString());
                    SellCmd sellCmd = new SellCmd(this, configHandler);
                    pc.setExecutor(sellCmd);
                    pc.setTabCompleter(sellCmd);
                    getLogger().info("Overrode ShopGUIPlus's /sell");
                    continue;
                }

                //getLogger().info("Leftover: "+pc.getName()+": "+pc.getLabel()+" "+pc.getAliases().toArray().toString());
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not override ShopGUIPlus commands", e);
        }
    }

    /**
     * Sets up the required hooks for plugin integration.
     * Disables the plugin if any required hook fails.
     */
    private void setupHooks() {
        if (!setupDisplayShops()) {
            getLogger().severe("DisplayShops hook failed -- disabling plugin!");
            disablePlugin();
            return;
        }

        if (!setupVault()) {
            getLogger().severe("Vault hook failed -- disabling plugin!");
            disablePlugin();
            return;
        }

        if (!setupRep64()) {
            getLogger().severe("Rep64 hook failed -- disabling plugin!");
            disablePlugin();
            return;
        }
    }

    /**
     * Attempts to hook into the DisplayShops plugin.
     *
     * @return true if the hook was successful, false otherwise.
     */
    private boolean setupDisplayShops() {
        if (!isPluginAvailable("DisplayShops")) return false;

        DisplayShopAddon64.dsHook = (DisplayShops) getServer().getPluginManager().getPlugin("DisplayShops");
        
        Utils.log(ChatColor.GREEN, "DisplayShops hooked.");
        return true;
    }

    /**
     * Attempts to hook into the Vault plugin.
     *
     * @return true if the hook was successful, false otherwise.
     */
    private boolean setupVault() {
        if (!isPluginAvailable("Vault")) return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        ecoHook = rsp.getProvider();
        if (ecoHook == null) return false;

        Utils.log(ChatColor.GREEN, "Vault hooked.");
        return true;
    }

    /**
     * Attempts to hook into the Rep64 plugin.
     *
     * @return true if the hook was successful, false otherwise.
     */
    private boolean setupRep64() {
        if (!isPluginAvailable("Rep64")) return false;

        Plugin rep64 = Bukkit.getPluginManager().getPlugin("Rep64");
        if (rep64.isEnabled() && rep64 instanceof Rep64)
            repHook = (Rep64) rep64;
        else return false;

        Utils.log(ChatColor.GREEN, "Rep64 hooked.");
        return true;
    }

    /**
     * Checks if the specified plugin is available and enabled on the server.
     *
     * @param pluginName the name of the plugin to check
     * @return true if the plugin is available and enabled, false otherwise.
     */
    private boolean isPluginAvailable(String pluginName) {
		final Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
		return plugin != null && plugin.isEnabled();
	}

    /**
     * Disables the plugin.
     */
    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        getLogger().info("DisplayShopAddon64 disabled..! ");
    }
}