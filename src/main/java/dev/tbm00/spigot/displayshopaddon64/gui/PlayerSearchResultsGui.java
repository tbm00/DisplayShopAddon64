package dev.tbm00.spigot.displayshopaddon64.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;

import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.displayshopaddon64.DisplayShopAddon64;
import dev.tbm00.spigot.displayshopaddon64.utils.*;

public class PlayerSearchResultsGui {
    DisplayShopAddon64 javaPlugin;
    PaginatedGui gui;
    String targetName;
    String label;
    
    public PlayerSearchResultsGui(DisplayShopAddon64 javaPlugin, ConcurrentHashMap<String, Shop> dsMap, Player sender, String targetUUID, String targetName) {
        this.javaPlugin = javaPlugin;
        this.targetName = targetName;
        label = targetName+" - ";
        gui = new PaginatedGui(6, 45, targetName);
        
        setupHalfFooter();
        fillShops(dsMap, sender, targetUUID);
        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
        gui.open(sender);
    }

    /**
     * Fills the GUI with items from the shop map.
     * Each shop that has a valid shop item and pricing information is converted into a clickable GUI item.
     *
     * @param dsMap a concurrent hash map of shop identifiers to Shop objects
     * @param sender the player for whom the GUI is being built
     */
    private void fillShops(ConcurrentHashMap<String, Shop> dsMap, Player sender, String targetUUID) {
        for (Shop shop : dsMap.values()) {
            /*check if valid & active shop*/ 
                if (shop.getShopItem()==null) continue; // if no shop item
                if (shop.getOwnerUniqueId()==null || !shop.getOwnerUniqueId().toString().equals(targetUUID))
                                                continue; // if owner is not a match
                double buyPrice = shop.getBuyPrice(false), sellPrice = shop.getSellPrice(false);
                if (buyPrice<0 && sellPrice<0) continue; // if buy & sell are both disabled
                
            /*define item button's lore, name, etc. and add to gui*/
                ItemStack item = shop.getShopItem().clone();
                ItemMeta meta = item.getItemMeta();
                List<String> lore = new ArrayList<>();
                int stock = shop.getStock();
                double balance = shop.getStoredBalance();
                String priceLine = "", name=null;
                UUID uuid = shop.getOwnerUniqueId();

                ShopUtils.addGuiItemShop(gui, shop, item, meta, lore, balance, buyPrice, sellPrice, priceLine, stock, uuid, name, sender);
        }
    }

    /**
     * Sets up the footer of the GUI with all, page next, page back, and search buttons.
     */
    private void setupHalfFooter() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        gui.setItem(6, 1, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 2, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 3, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 4, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));
        gui.setItem(6, 5, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Category: All
        lore.add("&8-----------------------");
        lore.add("&6Click to view all shops");
        meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dAll Shops"));
        item.setItemMeta(meta);
        item.setType(Material.CHEST);
        gui.setItem(6, 6, ItemBuilder.from(item).asGuiItem(event -> ShopUtils.handleAllClick(event, (Player) event.getWhoClicked())));
        lore.clear();

        // Previous Page
        ShopUtils.setGuiItemPageBack(gui, item, meta, lore, label);

        // Next Page
        ShopUtils.setGuiItemPageNext(gui, item, meta, lore, label);

        // Search
        ShopUtils.setGuiItemSearch(gui, item, meta, lore);
    }
}