

package dev.tbm00.spigot.displayshopaddon64.command;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.displayshopaddon64.DisplayShopAddon64;
import dev.tbm00.spigot.displayshopaddon64.utils.*;
import xzot1k.plugins.ds.api.objects.Shop;

public class AdminCmd implements TabExecutor {
    private final String ADMIN_PERM = "displayshopaddon64.admim";

    public AdminCmd() {}

    /**
     * Handles the /testshopadmin command.
     * 
     * @param player the command sender
     * @param consoleCommand the command being executed
     * @param alias the alias used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!Utils.hasPermission(sender, ADMIN_PERM)) {
            Utils.sendMessage(sender, "&cNo permission!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) return handleHelpCmd(player);

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                return handleHelpCmd(player);
            case "transfer":
                return handleTransferCmd(player, args);
            default:
                return ShopUtils.handleAdminSearch(player, args);
        }
    }
    
    /**
     * Handles the sub command for the help menu.
     * 
     * @param player the command sender
     * @return true after displaying help menu
     */
    private boolean handleHelpCmd(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "--- " + ChatColor.LIGHT_PURPLE + "Admin Commands" + ChatColor.DARK_PURPLE + " ---\n"
            + ChatColor.WHITE + "/testshopadmin <item/player>" + ChatColor.GRAY + " Manage all <item/player> shops\n"
            + ChatColor.WHITE + "/testshopadmin transfer <playerTo> <playerFrom>" + ChatColor.GRAY + " Change shops' owner"
        );
        return true;
    }
    
    /**
     * Handles the sub command for transfering shops from player-to-player.
     * 
     * @param sender the command sender
     * @param args the arguments passed to the command
     * @return true if after processing command
     */
    private boolean handleTransferCmd(Player sender, String[] args) {
        ConcurrentHashMap<String, Shop> dsMap = DisplayShopAddon64.dsHook.getManager().getShopMap();

        if (args.length<3) {
            Utils.sendMessage(sender, ChatColor.RED + "Usage: /testshopadmin transfer <playerTo> <playerFrom>");
            return true;
        }

        UUID uuidA = UUID.fromString(DisplayShopAddon64.repHook.getRepManager().getPlayerUUID(args[1]));
        if (uuidA == null) {
            Utils.sendMessage(sender, ChatColor.RED + "Could not find target (from) player!");
            return true;
        } 
        UUID uuidB = UUID.fromString(DisplayShopAddon64.repHook.getRepManager().getPlayerUUID(args[2]));
        if (uuidB == null) {
            Utils.sendMessage(sender, ChatColor.RED + "Could not find target (to) player!");
            return true;
        } 

        int i = 0;
        for (Shop shop : dsMap.values()) {
            if (shop.getOwnerUniqueId()!=null && shop.getOwnerUniqueId().equals(uuidA)) {
                shop.setOwnerUniqueId(uuidB);
                ++i;
            }
        }

        Utils.sendMessage(sender, ChatColor.YELLOW + "Transferred " + i + " shops to " + args[2]);
        return true;
    }

    /**
     * Handles tab completion for the /testshopadmin command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            String[] subCmds = new String[]{"<item>","<player>","transfer"};
            for (String n : subCmds) {
                if (n!=null && n.startsWith(args[0])) 
                    list.add(n);
            }
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getName().startsWith(args[0])&&args[0].length()>0)
                    list.add(player.getName());
            });
            for (Material mat : Material.values()) {
                if (mat.name().toLowerCase().startsWith(args[0].toLowerCase())&&args[0].length()>1)
                    list.add(mat.name().toLowerCase());
            }
        } else if (args.length == 2) {
            if (args[0].equals("transfer")) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getName().startsWith(args[1]))
                        list.add(player.getName());
                });
            }
        } else if (args.length == 3) {
            if (args[0].equals("transfer")) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getName().startsWith(args[1]))
                        list.add(player.getName());
                });
            }
        }
        return list;
    }
}