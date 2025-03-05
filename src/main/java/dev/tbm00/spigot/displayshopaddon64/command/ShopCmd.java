

package dev.tbm00.spigot.displayshopaddon64.command;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xzot1k.plugins.ds.api.objects.Shop;

import net.milkbowl.vault.economy.EconomyResponse;

import dev.tbm00.spigot.displayshopaddon64.DisplayShopAddon64;
import dev.tbm00.spigot.displayshopaddon64.ConfigHandler;
import dev.tbm00.spigot.displayshopaddon64.Utils;

public class ShopCmd implements TabExecutor {
    private final ConfigHandler configHandler;
    private static String PLAYER_PERM = "displayshopaddon64.player";

    public ShopCmd(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    /**
     * Handles the "/testshop" command.
     * 
     * @param player the command sender
     * @param consoleCommand the command being executed
     * @param label the label used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Utils.sendMessage(sender, "&cThis command cannot be run through the console!");
            return true;
        } else if (!Utils.hasPermission(sender, PLAYER_PERM)) {
            Utils.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0)
            return handleCategoryCmd(player, configHandler.getGUIDefaultCategory());

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                return handleHelpCmd(player);
            case "buy":
                return handleBuyCmd(player, args);
            case "advertise":
                return handleAdvertiseCmd(player, args);
            case "locate":
                return handleLocateCmd(player, args);
            case "deposit-all":
                return handleDepositCmd(player, args);
            case "withdraw-all":
                return handleWithdrawCmd(player, args);
            case "pog":
                return handleCategoryCmd(player, "shoppog");
            case "ores":
                return handleCategoryCmd(player, "shopores");
            case "tools":
                return handleCategoryCmd(player, "shoptools");
            case "blocks":
                return handleCategoryCmd(player, "shopblocks");
            case "drops":
                return handleCategoryCmd(player, "shopdrops");
            case "food":
                return handleCategoryCmd(player, "shopfood");
            case "farming":
                return handleCategoryCmd(player, "shopfarm");
            default:
                return handleSearchCmd(player, args);
        }
    }
    
    /**
     * Handles the sub command for the help menu.
     * 
     * @param player the command sender
     * @return true if command was processed successfully
     */
    private boolean handleHelpCmd(Player player) {
        player.sendMessage(ChatColor.DARK_GREEN + "--- " + ChatColor.GREEN + "Shop Commands" + ChatColor.DARK_GREEN + " ---\n"
            + ChatColor.WHITE + "/testshop" + ChatColor.GRAY + " Open shop category GUI\n"
            + ChatColor.WHITE + "/testshop [item name]" + ChatColor.GRAY + " Search for a specific item shop\n"
            + ChatColor.WHITE + "/testshop buy <#>" + ChatColor.GRAY + " Buy shop creation item(s)\n"
            + ChatColor.WHITE + "/testshop advertise" + ChatColor.GRAY + " Broadcast the shop you're looking at\n"
            + ChatColor.WHITE + "/testshop deposit-all <#>/max" + ChatColor.GRAY + " Deposit money into all your shops\n"
            + ChatColor.WHITE + "/testshop withdraw-all <#>/max" + ChatColor.GRAY + " Withdraw money from all your shops\n"
        );
        return true;
    }
    
    /**
     * Handles the sub command for buying shop creation items.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleBuyCmd(Player player, String[] args) {
        String count;
        if (args[1]==null || args[2].isBlank()) count = "1";
        else count = args[1];

        Utils.sudoCommand(player, "ds buy "+count);
        return true;
    }
    
    /**
     * Handles the sub command for advertising shops.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleAdvertiseCmd(Player player, String[] args) {
        Utils.sudoCommand(player, "ds advertise");
        return true;
    }
    
    /**
     * Handles the sub command for locating display shops.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleLocateCmd(Player player, String[] args) {
        ConcurrentHashMap<String, Shop> dsMap = DisplayShopAddon64.dsHook.getManager().getShopMap();
        UUID uuid = player.getUniqueId();
        int i = 0;
        for (Shop shop : dsMap.values()) {
            if (shop.getShopItem()==null) continue;
            if (shop.getOwnerUniqueId() != uuid) continue;
            ++i;
            Utils.sendMessage(player, "&7" + i + " &b" + shop.getShopItem().getType().toString().toLowerCase() + " &7@ "
                + shop.getBaseLocation().getWorldName() + ": " + shop.getBaseLocation().getX() + ", "
                + shop.getBaseLocation().getY() + ", " + shop.getBaseLocation().getZ());
        } if (i<1) Utils.sendMessage(player, "&cCouldn't find any of your DisplayShops!");
        return true;
    }

    /**
     * Handles the sub command for depositing money into shops.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleDepositCmd(Player player, String[] args) {
        if (args[1]==null) {
            Utils.sendMessage(player, "&f/testshop deposit-all <#>/max - &7Deposit money into all your shops");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = DisplayShopAddon64.dsHook.getManager().getShopMap();
        UUID uuid = player.getUniqueId();
        int shop_count = countPlayerShops(uuid, dsMap);
        if (shop_count<1) {
            Utils.sendMessage(player, "&cCouldn't find any of your DisplayShops!");
            return true;
        }

        // Determine how much to deposit into each shop
        String passedString = args[1];
        double pocket_balance = DisplayShopAddon64.ecoHook.getBalance(player), deposit_per;
        double max_possible_deposit_per = Math.floor(pocket_balance / shop_count);
        if (passedString.equalsIgnoreCase("max")) {
            deposit_per = max_possible_deposit_per;
        } else {
            Double potential_deposit_per;
            try {potential_deposit_per = Math.floor(Double.parseDouble(passedString));} 
            catch (Exception e) {
                Utils.sendMessage(player, "&cEntered amount must be numerical or 'max'!");
                return true;
            }
            if (potential_deposit_per < 1) {
                Utils.sendMessage(player, "&cEntered amount must be greater than 1!");
                return true;
            }

            if (potential_deposit_per>max_possible_deposit_per) {
                Utils.sendMessage(player, "&fYou can not afford to deposit $" + potential_deposit_per + " into each of your display shops. Using $" + max_possible_deposit_per + " instead (max based on your pocket balance & shop count).");
                deposit_per = max_possible_deposit_per;
            } else deposit_per = potential_deposit_per;
        }

        // Deposit into shops 1 by 1
        double amount_deposited = 0;
        int shops_affected = 0;
        for (Shop shop : dsMap.values()) {
            // confirm shop belongs to target
            if (shop.getShopItem()==null) continue;
            if (shop.getOwnerUniqueId() != uuid) continue;
            else {
                // caculate amount to set
                double stored_balance = shop.getStoredBalance(), deposit_amount;
                if (stored_balance >= configHandler.getDSMaxStoredBalance()) continue;
                if (stored_balance+deposit_per >= configHandler.getDSMaxStoredBalance())
                    deposit_amount = configHandler.getDSMaxStoredBalance()-stored_balance;
                else deposit_amount = deposit_per;

                shop.setStoredBalance(stored_balance+deposit_amount);
                amount_deposited += deposit_amount;
                ++shops_affected;
            }
        } removeMoney(player, amount_deposited);

        Utils.sendMessage(player, "&aDeposited a total of $" + amount_deposited + " into " + shops_affected + " of your shops!");
        return true;
    }

    /**
     * Handles the sub command for withdrawing money into shops.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleWithdrawCmd(Player player, String[] args) {
        if (args[1]==null) {
            Utils.sendMessage(player, "&f/testshop withdraw-all <#>/max - &7Withdraw money from all your shops");
            return true;
        }

        ConcurrentHashMap<String, Shop> dsMap = DisplayShopAddon64.dsHook.getManager().getShopMap();
        UUID uuid = player.getUniqueId();
        int shop_count = countPlayerShops(uuid, dsMap);
        if (shop_count<1) {
            Utils.sendMessage(player, "&cCouldn't find any of your DisplayShops!");
            return true;
        }

        // Determine how much to withdraw from each shop
        String passedString = args[1];
        boolean usingMax = false;
        Double withdraw_per = 0.0;
        if (passedString.equalsIgnoreCase("max")) usingMax = true;
        else {
            try {withdraw_per = Math.floor(Double.parseDouble(passedString));} 
            catch (Exception e) {
                Utils.sendMessage(player, "&cEntered amount must be numerical or 'max'!");
                return true;
            }
            if (withdraw_per < 1) {
                Utils.sendMessage(player, "&cEntered amount must be greater than 1!");
                return true;
            }
        }
        
        // Deposit into shops 1 by 1
        double amount_withdrew = 0;
        int shops_affected = 0;
        for (Shop shop : dsMap.values()) {
            // confirm shop belongs to target
            if (shop.getShopItem()==null) continue;
            if (shop.getOwnerUniqueId() != uuid) continue;
            else {
                // caculate amount to set
                double stored_balance = shop.getStoredBalance(), withdraw_amount;
                if (stored_balance <= 0) continue;
                if (usingMax==true)
                    withdraw_amount = stored_balance;
                else if (stored_balance < withdraw_per)
                    withdraw_amount = stored_balance;
                else withdraw_amount = withdraw_per;

                shop.setStoredBalance(stored_balance-withdraw_amount);
                amount_withdrew += withdraw_amount;
                ++shops_affected;
            }
        } addMoney(player, amount_withdrew);

        Utils.sendMessage(player, "&aWithdrew a total of $" + amount_withdrew + " from " + shops_affected + " of your shops!");
        return true;
    }

    /**
     * Counts the number of shops owned by a specific player.
     *
     * @param uuid  the unique identifier of the player whose shops are being counted
     * @param dsMap the map of all shops keyed by their unique IDs
     * @return the total number of shops that belong to the specified player
     */
    private int countPlayerShops(UUID uuid, ConcurrentHashMap<String, Shop> dsMap) {
        int i = 0;
        for (Shop shop : dsMap.values()) {
            if (shop.getShopItem()==null) continue;
            if (shop.getOwnerUniqueId() != uuid) continue;

            ++i;
        } return i;
    }

    /**
     * Attempts to remove a specified amount of money from the player's account.
     *
     * @param player the player from whose account the money will be withdrawn
     * @param amount the amount of money to remove from the account
     * @return true if the withdrawal transaction was successful, false otherwise
     */
    private boolean removeMoney(Player player, double amount) {
        EconomyResponse r = DisplayShopAddon64.ecoHook.withdrawPlayer(player, amount);
        if (r.transactionSuccess()) {
            return true;
        } else return false;
    }

    /**
     * Attempts to add a specified amount of money to the player's account.
     *
     * @param player the player whose account will receive the deposit
     * @param amount the amount of money to add to the account
     * @return true if the deposit transaction was successful, false otherwise
     */
    private boolean addMoney(Player player, double amount) {
        EconomyResponse r = DisplayShopAddon64.ecoHook.depositPlayer(player, amount);
        if (r.transactionSuccess()) {
            return true;
        } else return false;
    }
    
    /**
     * Handles the sub command for opening a specific category.
     * 
     * @param player the command sender
     * @param category the category passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleCategoryCmd(Player player, String category) {
        Utils.sudoCommand(player, "commandpanel "+category);
        return true;
    }
    
    /**
     * Handles the sub command for searching all shops.
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    private boolean handleSearchCmd(Player player, String[] args) {
        String search = "";
        for (String part : args) {
            search = search + " " + part;
        }
        Utils.sudoCommand(player, "swfilter"+search);
        return true;
    }

    /**
     * Handles tab completion for the "/testshop" command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            String[] subCmds = new String[]{"[item name]","help","buy","advertise","deposit-all","withdraw-all"};
            for (String n : subCmds) {
                if (n!=null && n.startsWith(args[0])) 
                    list.add(n);
            }
        } else if (args.length == 2) {
            if (args[0].equals("deposit-all") || args[0].equals("withdraw-all")) {
                list.add("<#>");
                list.add("max");
            } if (args[0].equals("buy")) {
                list.add("<#>");
            } 
        }
        return list;
    }
}