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
import org.bukkit.inventory.meta.SkullMeta;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;

import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.displayshopaddon64.DisplayShopAddon64;
import dev.tbm00.spigot.displayshopaddon64.utils.*;

public class PlayerResultsGui {
    DisplayShopAddon64 javaPlugin;
    PaginatedGui gui;
    String targetName;
    String label;
    
    public PlayerResultsGui(DisplayShopAddon64 javaPlugin, ConcurrentHashMap<String, Shop> dsMap, Player sender, String targetUUID, String targetName, int queryType) {
        this.javaPlugin = javaPlugin;
        this.targetName = targetName;
        label = targetName+" - ";
        gui = new PaginatedGui(6, 45, targetName);
        
        fillShops(dsMap, sender, targetUUID, queryType);
        setupFooter(sender, targetUUID);
        
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
     * @param queryType the type of query 0="shop", 1="buy", 2="sell"
     */
    private void fillShops(ConcurrentHashMap<String, Shop> dsMap, Player sender, String targetUUID, int queryType) {
        for (Shop shop : dsMap.values()) {
            /*check if valid & active shop*/ 
                if (shop.getShopItem()==null) continue; // if no shop item
                if (shop.getOwnerUniqueId()==null || !shop.getOwnerUniqueId().toString().equals(targetUUID))
                                                continue; // if owner is not a match
                double buyPrice = shop.getBuyPrice(false), sellPrice = shop.getSellPrice(false);
                if (buyPrice<0 && sellPrice<0) continue; // if buy & sell are both disabled
                //if (queryType==1 && buyPrice<0) continue; // if searching for buy shops and buy is disabled
                //if (queryType==2 && sellPrice<0) continue; // if searching for buy shops and sell is disabled
                
            /*define item button's lore, name, etc. and add to gui*/
                ItemStack item = shop.getShopItem().clone();
                ItemMeta meta = item.getItemMeta();
                List<String> lore = new ArrayList<>();
                int stock = shop.getStock();
                double balance = shop.getStoredBalance();
                String priceLine = "", name=null;
                UUID uuid = shop.getOwnerUniqueId();

                GuiUtils.addGuiItemShop(gui, shop, item, meta, lore, balance, buyPrice, sellPrice, priceLine, stock, uuid, name, sender);
        }
    }

    /**
     * Sets up the footer of the GUI with all, page next, page back, and search buttons.
     */
    private void setupFooter(Player sender, String targetUUID) {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // Category: Pog
        GuiUtils.setGuiItemCatPog(gui, item, meta, lore);

        // Category: Blocks
        GuiUtils.setGuiItemCatBlocks(gui, item, meta, lore);

        // Category: Food
        GuiUtils.setGuiItemCatFood(gui, item, meta, lore);

        // Category: Drops
        GuiUtils.setGuiItemCatDrops(gui, item, meta, lore);

        // NonCategory: Your Shops
        if (targetUUID.equals(sender.getUniqueId().toString())) {
            item.setType(Material.PLAYER_HEAD);
            SkullMeta headmeta = (SkullMeta) item.getItemMeta();
            lore.add("&8-----------------------");
            lore.add("&eCurrently viewing your shops");
            headmeta.setOwningPlayer(sender);
            headmeta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
            headmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dYour Shops"));
            item.setItemMeta(headmeta);
            gui.setItem(6, 5, ItemBuilder.from(item).asGuiItem(event -> {event.setCancelled(true);}));
            lore.clear();
        } else GuiUtils.setGuiItemYourShops(gui, item, meta, lore, sender);

        // NonCategory: All Shops
        GuiUtils.setGuiItemAllShops(gui, item, meta, lore);

        // Previous Page
        if (gui.getPagesNum()>=2) GuiUtils.setGuiItemPageBack(gui, item, meta, lore, label);
        else gui.setItem(6, 7, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Next Page
        if (gui.getPagesNum()>=2)  GuiUtils.setGuiItemPageNext(gui, item, meta, lore, label);
        else gui.setItem(6, 8, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").asGuiItem(event -> event.setCancelled(true)));

        // Search
        GuiUtils.setGuiItemSearch(gui, item, meta, lore);
    }
}