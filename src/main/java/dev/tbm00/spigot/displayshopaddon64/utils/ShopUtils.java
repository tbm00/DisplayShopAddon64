package dev.tbm00.spigot.displayshopaddon64.utils;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;

import xzot1k.plugins.ds.api.objects.Shop;
import xzot1k.plugins.ds.api.objects.DataPack;

import dev.tbm00.spigot.displayshopaddon64.DisplayShopAddon64;
import dev.tbm00.spigot.displayshopaddon64.gui.*;

public class ShopUtils {
    private static DisplayShopAddon64 javaPlugin;
    public static final List<String> pendingTeleports = new CopyOnWriteArrayList<>();

    public static void init(DisplayShopAddon64 javaPlugin) {
        ShopUtils.javaPlugin = javaPlugin;
    }

    /**
     * Handles the sub command for opening a specific category.
     * 
     * @param player the command sender
     * @param category the category passed to the command
     * @return true if command was processed successfully
     */
    public static boolean handleCategoryCmd(Player player, String category) {
        Utils.sudoCommand(player, "commandpanel "+category);
        return true;
    }

    /**
     * Handles the sub command for opening the shop gui.
     * 
     * @param player the command sender
     * @return true after creating gui instance
     */
    public static boolean handleGuiCmd(Player player) {
        ConcurrentHashMap<String, Shop> dsMap = DisplayShopAddon64.dsHook.getManager().getShopMap();
        new ShopGui(javaPlugin, dsMap, player);
        return true;
    }

    /**
     * Handles the sub command for opening the search gui.
     * 
     * @param player the command sender
     * @return true after creating gui instance
     */
    public static boolean handleAnvilGuiCmd(Player player) {
        new AnvilGui(javaPlugin, player);
        return true;
    }

    /**
     * Handles searching the shops by String/player
     * 
     * @param player the command sender
     * @param args the arguments passed to the command
     * @return true if command was processed successfully
     */
    public static boolean handleSearch(Player sender, String[] args) {
        ConcurrentHashMap<String, Shop> dsMap = DisplayShopAddon64.dsHook.getManager().getShopMap();
        // search shops for target player
        while (args[0].startsWith(" ")) {
            args[0] = args[0].substring(1);
        }
        String targetName = args[0];
        String targetUUID = DisplayShopAddon64.repHook.getRepManager().getPlayerUUID(targetName);
        if (targetUUID!=null) {
            new PlayerSearchResultsGui(javaPlugin, dsMap, sender, targetUUID, targetName);
            return true;
        }

        // search shops for target item String
        String search = null;
        int i=0;
        for (String arg : args) {
            if (i==0) {
                search = arg;
                ++i;
            } else search = search + " " + arg;
        }

        if (search==null) return false;
        search = search.replace("_", " ");
        
        new StringSearchResultsGui(javaPlugin, dsMap, sender, search);
        return true;
    }

    /**
     * Handles the event when search button is clicked.
     * 
     * @param event the inventory click event
     */
    public static void handleSearchClick(InventoryClickEvent event) {
        event.setCancelled(true);
        new AnvilGui(javaPlugin, (Player) event.getWhoClicked());
    }

    /**
     * Handles the event when a category selector is clicked.
     * 
     * @param event the inventory click event
     * @param command the command to execute for changing the category
     */
    public static void handleCategoryClick(InventoryClickEvent event, String command) {
        event.setCancelled(true);
        Utils.sudoCommand(event.getWhoClicked(), command);
    }

    /**
     * Handles the event when a shop item in the GUI is clicked.
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    public static void handleAllClick(InventoryClickEvent event, Player sender) {
        event.setCancelled(true);
        ConcurrentHashMap<String, Shop> dsMap = DisplayShopAddon64.dsHook.getManager().getShopMap();
        new ShopGui(javaPlugin, dsMap, sender);
    }

    /**
     * Handles the event when a page button is clicked.
     * 
     * @param event the inventory click event
     * @param next true to go to the next page; false to go to the previous page
     */
    public static void handlePageClick(InventoryClickEvent event, PaginatedGui gui, boolean next, String label) {
        event.setCancelled(true);
        if (next) gui.next();
        else gui.previous();
        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
    }

    /**
     * Handles the event when a shop item in the GUI is clicked.
     * 
     * @param event the inventory click event
     * @param sender the player who clicked the shop item
     * @param shop the shop associated with the clicked item
     */
    public static void handleShopClick(InventoryClickEvent event, Player sender, Shop shop) {
        event.setCancelled(true);
        
        if (event.isShiftClick() && sender.getUniqueId().equals(shop.getOwnerUniqueId())) {
            DataPack dataPack = DisplayShopAddon64.dsHook.getManager().getDataPackMap().get(sender.getUniqueId());
            dataPack.setSelectedShop(shop);

            DisplayShopAddon64.dsHook.getMenu("edit").build(sender, (String[]) null);
        } else teleportPlayerToShop(sender, shop);
    }

    /**
     * Teleports the player to the given shop's location.
     * 
     * @param sender the player to teleport
     * @param shop the shop whose location the player will be teleported to
     */
    public static void teleportPlayerToShop(Player sender, Shop shop) {
        double x=shop.getBaseLocation().getX(), y=shop.getBaseLocation().getY(), z=shop.getBaseLocation().getZ();
        String world=shop.getBaseLocation().getWorldName();

        Utils.teleportPlayer(sender, world, x, y+1, z);
    }

    /**
     * Counts the number of shops owned by a specific player.
     *
     * @param uuid  the unique identifier of the player whose shops are being counted
     * @param dsMap the map of all shops keyed by their unique IDs
     * @return the total number of shops that belong to the specified player
     */
    public static int countPlayerShops(ConcurrentHashMap<String, Shop> dsMap, UUID uuid) {
        int i = 0;
        for (Shop shop : dsMap.values()) {
            if (shop.getShopItem()==null) continue;
            if (shop.getOwnerUniqueId()==null || !shop.getOwnerUniqueId().equals(uuid)) continue;

            ++i;
        } return i;
    }

    /**
     * Sets the shop GUI's footer's category button: pog.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemCatPog(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to change category to: pog");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dPog Shops"));
        item.setItemMeta(meta);
        item.setType(Material.NETHERITE_PICKAXE);
        gui.setItem(6, 1, ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handleCategoryClick(event, "commandpanel shoppog")));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's category button: blocks.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemCatBlocks(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to change category to: blocks");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dBlock Shops"));
        item.setItemMeta(meta);
        item.setType(Material.GRASS_BLOCK);
        gui.setItem(6, 2, ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handleCategoryClick(event, "commandpanel shopblocks")));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's category button: food.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemCatFood(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to change category to: food");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dFood Shops"));
        item.setItemMeta(meta);
        item.setType(Material.COOKED_BEEF);
        gui.setItem(6, 3, ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handleCategoryClick(event, "commandpanel shopfood")));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's category button: mob drops.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemCatDrops(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to change category to: mob drops");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dMob Drop Shops"));
        item.setItemMeta(meta);
        item.setType(Material.GUNPOWDER);
        gui.setItem(6, 4, ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handleCategoryClick(event, "commandpanel shopdrops")));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's category button: ores.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemCatOres(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to change category to: ores");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dOre Shops"));
        item.setItemMeta(meta);
        item.setType(Material.DIAMOND);
        gui.setItem(6, 5, ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handleCategoryClick(event, "commandpanel shopores")));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's previous page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param label holder for gui's title
     */
    public static void setGuiItemPageBack(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, String label) {
        lore.add("&8-----------------------");
        lore.add("&6Click to go to the previous page");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fPrevious Page"));
        item.setItemMeta(meta);
        item.setType(Material.STONE_BUTTON);
        gui.setItem(6, 7, ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handlePageClick(event, gui, false, label)));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's next page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     * @param label holder for gui's title
     */
    public static void setGuiItemPageNext(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore, String label) {
        lore.add("&8-----------------------");
        lore.add("&6Click to go to the next page");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fNext Page"));
        item.setItemMeta(meta);
        item.setType(Material.STONE_BUTTON);
        gui.setItem(6, 8, ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handlePageClick(event, gui, true, label)));
        lore.clear();
    }

    /**
     * Sets the shop GUI's footer's search page button format.
     *
     * @param gui the gui that will be sent to the player
     * @param item holder for current item
     * @param meta holder for current item's meta
     * @param lore holder for current item's lore
     */
    public static void setGuiItemSearch(PaginatedGui gui, ItemStack item, ItemMeta meta, List<String> lore) {
        lore.add("&8-----------------------");
        lore.add("&6Click to search for a specific item");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dSearch Shops"));
        item.setItemMeta(meta);
        item.setType(Material.NAME_TAG);
        gui.setItem(6, 9, ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handleSearchClick(event)));
        lore.clear();
    }

    /**
     * Formats and adds an item to the shop GUI.
     *
     * @param gui the paginated GUI to which the item will be added
     * @param shop the shop associated with the item
     * @param item the item to be displayed in the GUI
     * @param meta the metadata of the item
     * @param lore the list of lore descriptions to be displayed
     * @param balance the shop's current balance
     * @param buyPrice the item's buy price
     * @param sellPrice the item's sell price
     * @param priceLine the formatted price string
     * @param stock the current stock of the item
     * @param uuid the unique identifier of the shop owner
     * @param name the formatted display name of the item
     * @param sender the player viewing the shop
     */
    public static void addGuiItemShop(PaginatedGui gui, Shop shop, ItemStack item, ItemMeta meta, List<String> lore, double balance, double buyPrice, double sellPrice, String priceLine, int stock, UUID uuid, String name, Player sender) {
        meta.setLore(null);
        lore.add("&8-----------------------");
        lore.add("&c" + shop.getDescription());
        if (buyPrice>=0) priceLine = "&7B: &a$" + Utils.formatInt(buyPrice) + " ";
        if (sellPrice>=0) priceLine += "&7S: &c$" + Utils.formatInt(sellPrice);
        lore.add(priceLine);
        if (stock<0) lore.add("&7Stock: &e∞");
            else lore.add("&7Stock: &e" + stock);
        if (stock<0) lore.add("&7Balance: &e$&e∞");
            else lore.add("&7Balance: &e$" + Utils.formatInt(balance));
        if (uuid!=null) lore.add("&7Owner: &f" + DisplayShopAddon64.repHook.getRepManager().getPlayerUsername(uuid.toString()));
        lore.add("&7"+shop.getBaseLocation().getWorldName()+": &f"+(int)shop.getBaseLocation().getX()+"&7, &f"
                    +(int)shop.getBaseLocation().getY()+"&7, &f"+(int)shop.getBaseLocation().getZ());
        lore.add("&8-----------------------");
        lore.add("&6Click to TP to this shop");
        if (uuid!=null && sender.getUniqueId().equals(uuid))
            lore.add("&eShift-click to edit this shop");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        if (meta.getDisplayName()==null || meta.getDisplayName().isBlank())
            name = Utils.formatMaterial(item.getType()) + " &7x &f" + shop.getShopItemAmount();
        else name = meta.getDisplayName() + " &7x &f" + shop.getShopItemAmount();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_DYE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        for (Enchantment enchant : new HashSet<>(meta.getEnchants().keySet()))
            meta.removeEnchant(enchant);

        item.setItemMeta(meta);
        item.setAmount(shop.getShopItemAmount());

        gui.addItem(ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handleShopClick(event, sender, shop)));
    }
}