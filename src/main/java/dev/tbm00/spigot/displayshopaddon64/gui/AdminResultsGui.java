package dev.tbm00.spigot.displayshopaddon64.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;

import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.displayshopaddon64.DisplayShopAddon64;
import dev.tbm00.spigot.displayshopaddon64.utils.*;

public class AdminResultsGui {
    DisplayShopAddon64 javaPlugin;
    PaginatedGui gui;
    String query;
    String label;
    
    public AdminResultsGui(DisplayShopAddon64 javaPlugin, ConcurrentHashMap<String, Shop> dsMap, Player sender, String query) {
        this.javaPlugin = javaPlugin;
        this.query = query;

        String name = DisplayShopAddon64.repHook.getRepManager().getPlayerUsername(query);
        if (name!=null) label = name+" - ";
        else label = query+" - ";
        
        gui = new PaginatedGui(6, 45, query);
        
        fillShops(dsMap, sender, query);
        setupFooter(sender);
        
        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
        gui.disableAllInteractions();
        gui.open(sender);
    }

    /**
     * Fills the GUI with items from the shop map.
     * Each shop that has a valid shop item and pricing information is converted into a clickable GUI item.
     *
     * @param dsMap a concurrent hash map of shop identifiers to Shop objects
     * @param sender the player for whom the GUI is being built
     */
    private void fillShops(ConcurrentHashMap<String, Shop> dsMap, Player sender, String query) {
        for (Shop shop : dsMap.values()) {
            /*check if valid & active shop*/ 
                double buyPrice = shop.getBuyPrice(false), sellPrice = shop.getSellPrice(false);
                double balance = shop.getStoredBalance();
                int stock = shop.getStock();
                boolean empty = false;

            /*check if query matches*/ 
                boolean include = false;
                ItemStack item;
                if (shop.getShopItem()==null) {
                    item = new ItemStack(Material.BARRIER, 1);
                    empty = true;
                }
                else item = shop.getShopItem().clone();
                String mat = item.getType().toString().replace("_", " ");
                ItemMeta meta = item.getItemMeta();
                String name = meta.getDisplayName();
                String desc = shop.getDescription();
                if (mat != null && StringUtils.containsIgnoreCase(mat, query)) include = true;
                else if (name!=null && StringUtils.containsIgnoreCase(name, query)) include = true;
                else if (desc != null && StringUtils.containsIgnoreCase(desc, query)) include = true;
                else if (shop.getOwnerUniqueId()!=null) {
                    String owner = DisplayShopAddon64.repHook.getRepManager().getPlayerUsername(shop.getOwnerUniqueId().toString());
                    if (owner!=null && StringUtils.containsIgnoreCase(owner, query)) include = true;
                } if (!include) continue;
            
            /*define item button's lore, name, etc. and add to gui*/
                List<String> lore = new ArrayList<>();    
                String priceLine = "";
                UUID uuid = shop.getOwnerUniqueId();

                GuiUtils.addGuiAdminItemShop(gui, shop, item, meta, lore, balance, buyPrice, sellPrice, priceLine, stock, uuid, name, sender, empty);
        }
    }

    /**
     * Sets up the footer of the GUI with categories & all other buttons.
     */
    private void setupFooter(Player sender) {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // Your Shops
        GuiUtils.setGuiItemYourShops(gui, item, meta, lore, sender);

        // All Shops
        GuiUtils.setGuiItemAllShops(gui, item, meta, lore);

        // Category
        GuiUtils.setGuiItemCat(gui, item, meta, lore);

        // Empty
        gui.setItem(6, 4, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Previous Page
        if (gui.getPagesNum()>=2) GuiUtils.setGuiItemPageBack(gui, item, meta, lore, label);
        else gui.setItem(6, 5, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Next Page
        if (gui.getPagesNum()>=2)  GuiUtils.setGuiItemPageNext(gui, item, meta, lore, label);
        else gui.setItem(6, 6, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Sort
        gui.setItem(6, 7, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Search
        GuiUtils.setGuiAdminItemSearch(gui, item, meta, lore);
        
        // Main Menu
        GuiUtils.setGuiItemMainMenu(gui, item, meta, lore);
    }
}