package dev.tbm00.spigot.displayshopaddon64;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

                //reregisterCommand2("shop", "sgplus");
                //unregisterCommand3("sell");
                ///unregisterCommand3("shop");

                // Register Commands
                cloneOriginalShopCommand("originalsgplus");
                overrideSGPExecutors();
                getCommand("buy").setExecutor(new BuyCmd(configHandler));
                getCommand("sellgui").setExecutor(new SellGuiCmd(this));
                getCommand("shopadmin").setExecutor(new AdminCmd());

                //getCommand("shop").setExecutor(new ShopCmd(this, configHandler));
                //getCommand("sell").setExecutor(new SellCmd(this, configHandler));

                if (configHandler.isDsDescChanged()) {
                    new DescChangeTask();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void cloneOriginalShopCommand(String newName) {
        Plugin sgp = getServer().getPluginManager().getPlugin("ShopGUIPlus");
        if (sgp == null) {
            getLogger().warning("ShopGUIPlus not found; cannot clone /shop");
            return;
        }

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
                getLogger().warning("Could not locate ShopGUIPlus's real /shop command");
                return;
            }

            // 3) reflectively construct a fresh PluginCommand
            Constructor<PluginCommand> ctor =
                PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            ctor.setAccessible(true);
            PluginCommand clone = ctor.newInstance(newName, sgp);

            // 4) copy over all the bits you need
            clone.setDescription(original.getDescription());
            clone.setUsage(original.getUsage());
            clone.setPermission(original.getPermission());
            clone.setAliases(new ArrayList<>());
            clone.setExecutor(original.getExecutor());
            if (original.getTabCompleter() != null) {
                clone.setTabCompleter(original.getTabCompleter());
            }

            // 5) register it under SGP's namespace so it won't shadow
            commandMap.register(sgp.getName(), clone);
            getLogger().info("Cloned ShopGUIPlus's /shop ➔ /" + newName);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to clone ShopGUIPlus /shop command", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void overrideSGPExecutors() {
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
                    getLogger().info(pc.getName()+": "+pc.getLabel()+" "+pc.getAliases().subList(0, pc.getAliases().size()-1).toString());
                    ShopCmd shopCmd = new ShopCmd(this, configHandler);
                    pc.setExecutor(shopCmd);
                    pc.setTabCompleter(shopCmd);
                    continue;
                }

                // override /sell
                if (pc.getName().equalsIgnoreCase("sell")) {
                    getLogger().info(pc.getName()+": "+pc.getLabel()+" "+pc.getAliases().subList(0, pc.getAliases().size()-1).toString());
                    SellCmd sellCmd = new SellCmd(this, configHandler);
                    pc.setExecutor(sellCmd);
                    pc.setTabCompleter(sellCmd);
                    continue;
                }

                getLogger().info("Leftover: "+pc.getName()+": "+pc.getLabel()+" "+pc.getAliases().toArray().toString());
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not override ShopGUIPlus commands", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void reregisterCommand1(String original, String newName) {
        Plugin shopGuiPlusPl = getServer().getPluginManager().getPlugin("ShopGUIPlus");
        if (shopGuiPlusPl == null) {
            getLogger().warning("ShopGUIPlus not found; cannot reregister '" + original + "'");
            return;
        }

        try {
            // 1) Grab the CommandMap
            SimpleCommandMap commandMap = (SimpleCommandMap) getServer().getClass().getMethod("getCommandMap").invoke(getServer());

            // 2) Grab its private knownCommands map
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            Map<String, Command> known = (Map<String, Command>) f.get(commandMap);

            // 3) Find all entries for /original → collect their keys & stash the PluginCommand
            PluginCommand shopCmd = null;
            List<String> cmdKeysToRemove = new ArrayList<>();
            for (Map.Entry<String, Command> e : known.entrySet()) {
                String cmdKey = e.getKey();
                Command cmd = e.getValue();
                if ((cmdKey.equalsIgnoreCase(original)||cmd.getLabel().equalsIgnoreCase(original)||cmd.getName().equalsIgnoreCase(original))
                    && cmd instanceof PluginCommand
                    && ((PluginCommand) cmd).getPlugin().equals(shopGuiPlusPl))
                {
                    getLogger().warning("Found " + e.getKey() + " + " + cmd.getName() + " + " + cmd.getLabel());
                    shopCmd = (PluginCommand) cmd;
                    cmdKeysToRemove.add(cmdKey);
                }
            }

            if (shopCmd == null) {
                getLogger().warning("Could not find ShopGUIPlus command '" + original + "'");
                return;
            }

            // 4) Remove all those old mappings by key
            for (String cmdKey : cmdKeysToRemove) {
                known.remove(cmdKey);
                getLogger().warning("cmdKey to remove: " + cmdKey);
            }

            // 5) Rename the command object
            Field nameField = Command.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(shopCmd, newName);

            // 6) Reassign aliases
            shopCmd.getAliases().clear();

            // 7) Register it back under its new name
            commandMap.register(shopGuiPlusPl.getName(), shopCmd);

            for (Command cmd : commandMap.getCommands()) {
                if ((cmd instanceof PluginCommand) && !((PluginCommand)cmd).getPlugin().equals(this) && cmd.getName().equalsIgnoreCase(original)) {
                    getLogger().warning("Unregister successfully: " + cmd.unregister(commandMap) + cmd.getName() + " + " + cmd.getLabel());
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error reristering ShopGUIPlus command", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void reregisterCommand2(String original, String newName) {
        Plugin shopGuiPlusPl = getServer().getPluginManager().getPlugin("ShopGUIPlus");
        if (shopGuiPlusPl == null) {
            getLogger().warning("ShopGUIPlus not found; cannot reregister '" + original + "'");
            return;
        }

        try {
            // 1) Grab the CommandMap
            SimpleCommandMap commandMap = (SimpleCommandMap) getServer().getClass().getMethod("getCommandMap").invoke(getServer());

            // 2) Grab its private knownCommands map
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            Map<String, Command> known = (Map<String, Command>) f.get(commandMap);

            // 3) Find all entries for /original → collect their keys & stash the PluginCommand
            PluginCommand shopCmd = null;
            for (Map.Entry<String, Command> e : known.entrySet()) {
                String cmdKey = e.getKey();
                Command cmd = e.getValue();
                if ((cmdKey.equalsIgnoreCase(original))
                    && cmd instanceof PluginCommand
                    && ((PluginCommand) cmd).getPlugin().equals(shopGuiPlusPl))
                {
                    getLogger().warning("Found " + e.getKey() + " + " + cmd.getName() + " + " + cmd.getLabel());
                    shopCmd = (PluginCommand) cmd;
                }
            }
            if (shopCmd == null) {
                getLogger().warning("Could not find ShopGUIPlus command '" + original + "'");
                return;
            }

            // 4) Rename the command object
            Field nameField = Command.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(shopCmd, newName);

            // 5) Reassign aliases
            shopCmd.getAliases().clear();

            // 6) Register it back under its new name
            commandMap.register(shopGuiPlusPl.getName(), shopCmd);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error reregistering ShopGUIPlus command", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void unregisterCommand1(String name) {
        try {
            // grab the CommandMap
            SimpleCommandMap commandMap = (SimpleCommandMap) getServer().getClass().getMethod("getCommandMap").invoke(getServer());

            // grab the knownCommands map
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            Map<String, Command> known = (Map<String, Command>) f.get(commandMap);

            // remove all entries by that name
            known.entrySet().removeIf(entry -> {
                Command cmd = entry.getValue();
                // only remove if it's ShopGUIPlus's command under that alias
                return entry.getKey().equalsIgnoreCase(name)
                    && cmd instanceof PluginCommand
                    && ((PluginCommand) cmd).getPlugin().getName().equals("ShopGUIPlus");
            });
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not unregister command", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void unregisterCommand2(String name) {
        Plugin shopGuiPlusPl = getServer().getPluginManager().getPlugin("ShopGUIPlus");
        if (shopGuiPlusPl == null) {
            getLogger().warning("ShopGUIPlus not found; cannot unregister '" + name + "'");
            return;
        }

        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) getServer()
                .getClass().getMethod("getCommandMap").invoke(getServer());
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            Map<String, Command> known = (Map<String, Command>) f.get(commandMap);

            for (Command cmd : new ArrayList<>(known.values())) {
                if (cmd instanceof PluginCommand
                    && ((PluginCommand) cmd).getPlugin().equals(shopGuiPlusPl)
                    && cmd.getName().equalsIgnoreCase(name))
                {
                    getLogger().info("Unregistering ShopGUIPlus command /" + cmd.getName());
                    cmd.unregister(commandMap);
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not unregister ShopGUIPlus command", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void unregisterCommand3(String name) {
        Plugin shopGuiPlusPl = getServer().getPluginManager().getPlugin("ShopGUIPlus");
        if (shopGuiPlusPl == null) {
            getLogger().warning("ShopGUIPlus not found; cannot unregister '" + name + "'");
            return;
        }

        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) getServer()
                .getClass().getMethod("getCommandMap").invoke(getServer());
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            Map<String, Command> known = (Map<String, Command>) f.get(commandMap);

            // collect every PluginCommand from SGP whose primary name matches
            List<PluginCommand> toNuke = new ArrayList<>();
            for (Command cmd : new HashSet<>(known.values())) {
                if (cmd instanceof PluginCommand pc
                && pc.getPlugin().equals(shopGuiPlusPl)
                && (pc.getName().equalsIgnoreCase(name)||pc.getLabel().equalsIgnoreCase(name)||pc.getAliases().contains(name))) {
                    toNuke.add(pc);
                }
            }

            // now remove *every* key (label or alias) mapping to those instances
            for (PluginCommand pc : toNuke) {
                getLogger().info(name + ": Totally removed ShopGUIPlus command mappings for /" + pc.getName() + " + " +pc.getLabel()+ " + " +pc.getAliases().toArray().toString());
                known.entrySet().removeIf(entry -> entry.getValue() == pc);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, name + ": Could not unregister ShopGUIPlus command", e);
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