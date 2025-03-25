package dev.tbm00.spigot.displayshopaddon64.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

public class ListResultsPlayerGui {
    DisplayShopAddon64 javaPlugin;
    PaginatedGui gui;
    String targetName;
    String label;
    Player sender;
    List<Map.Entry<String, Shop>> dsMap;
    int currentSortIndex = 0;
    int queryType = 0; // the type of query 0="shop", 1="buy", 2="sell"
    
    public ListResultsPlayerGui(DisplayShopAddon64 javaPlugin, ConcurrentHashMap<String, Shop> dsMap, Player sender, String targetUUID, String targetName, int queryType, int sortIndex) {
        this.javaPlugin = javaPlugin;
        this.targetName = targetName;
        this.sender = sender;
        this.dsMap = new ArrayList<>(dsMap.entrySet());
        this.queryType = queryType;
        currentSortIndex = sortIndex;
        label = targetName+" - ";
        gui = new PaginatedGui(6, 45, targetName);
        
        preProcessShops(targetUUID);
        ShopUtils.sortShops(this.dsMap, currentSortIndex);
        fillShops(targetUUID);
        setupFooter(targetUUID);
        
        gui.updateTitle(label + gui.getCurrentPageNum() + "/" + gui.getPagesNum());
        gui.disableAllInteractions();
        gui.open(sender);
    }

    private void preProcessShops(String targetUUID) {
        Iterator<Map.Entry<String, Shop>> iter = dsMap.iterator();
        while(iter.hasNext()) {
            Map.Entry<String, Shop> entry = iter.next();
            Shop shop = entry.getValue();

            if (shop.getShopItem() == null) { // if no shop item
                iter.remove();
                continue;
            }

            boolean remove = false;

            /*check if valid & active shop*/ 
                double buyPrice = shop.getBuyPrice(false), sellPrice = shop.getSellPrice(false);
                if (shop.getOwnerUniqueId()==null || !shop.getOwnerUniqueId().toString().equals(targetUUID))
                                                remove = true; // if owner is not a match
                //else if (buyPrice<0 && sellPrice<0) remove = true; // if buy-from & sell-to are both disabled
                //else if (queryType==1 && buyPrice<0) remove = true; // if searching for buy shops and buy-from is disabled
                //else if (queryType==2 && sellPrice<0) remove = true; // if searching for sell shops and sell-to is disabled
                
            if (remove) iter.remove();
        }
    }

    /**
     * Fills the GUI with items from the shop map.
     * Each shop that has a valid shop item and pricing information is converted into a clickable GUI item.
     */
    private void fillShops(String targetUUID) {
        Iterator<Map.Entry<String, Shop>> iter = dsMap.iterator();
        while(iter.hasNext()) {
            Map.Entry<String, Shop> entry = iter.next();
            Shop shop = entry.getValue();

            ItemStack item = shop.getShopItem().clone();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            int stock = shop.getStock();
            double buyPrice = shop.getBuyPrice(false), sellPrice = shop.getSellPrice(false), balance = shop.getStoredBalance();
            String priceLine = "", name=null;
            UUID uuid = shop.getOwnerUniqueId();

            GuiUtils.addGuiItemShop(gui, shop, item, meta, lore, balance, buyPrice, sellPrice, priceLine, stock, uuid, name, sender);
        }
    }

    /**
     * Sets up the footer of the GUI with all, page next, page back, and search buttons.
     */
    private void setupFooter(String targetUUID) {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // Your Shops
        if (targetUUID.equals(sender.getUniqueId().toString())) {
            item.setType(Material.PLAYER_HEAD);
            SkullMeta headmeta = (SkullMeta) item.getItemMeta();
            lore.add("&8-----------------------");
            lore.add("&eCurrently viewing your shops");
            headmeta.setOwningPlayer(sender);
            headmeta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
            headmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&dYour Shops"));
            item.setItemMeta(headmeta);
            gui.setItem(6, 1, ItemBuilder.from(item).asGuiItem(event -> {event.setCancelled(true);}));
            lore.clear();
        } else GuiUtils.setGuiItemYourShops(gui, item, meta, lore, sender);

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
        GuiUtils.setGuiItemSortShopsPlayer(gui, item, meta, lore, targetUUID, targetUUID, queryType, currentSortIndex);

        // Search
        GuiUtils.setGuiItemSearch(gui, item, meta, lore);
        
        // Main Menu
        GuiUtils.setGuiItemMainMenu(gui, item, meta, lore);
    }
}