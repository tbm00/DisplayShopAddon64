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
import org.bukkit.ChatColor;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;

import xzot1k.plugins.ds.api.objects.Shop;

import dev.tbm00.spigot.displayshopaddon64.DisplayShopAddon64;
import dev.tbm00.spigot.displayshopaddon64.utils.*;

public class StringSearchResultsGui {
    DisplayShopAddon64 javaPlugin;
    PaginatedGui gui;
    String query;
    String label;
    
    public StringSearchResultsGui(DisplayShopAddon64 javaPlugin, ConcurrentHashMap<String, Shop> dsMap, Player sender, String query) {
        this.javaPlugin = javaPlugin;
        this.query = query;
        label = query+" - ";
        gui = new PaginatedGui(6, 45, query);
        
        setupFooter();
        fillShops(dsMap, sender, query);
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
    private void fillShops(ConcurrentHashMap<String, Shop> dsMap, Player sender, String query) {
        for (Shop shop : dsMap.values()) {
            /*check if valid & active shop*/ 
                if (shop.getShopItem()==null) continue; // if no shop item
                double buyPrice = shop.getBuyPrice(false), sellPrice = shop.getSellPrice(false);
                if (buyPrice<0 && sellPrice<0) continue; // if buy & sell are both disabled
                double balance = shop.getStoredBalance();
                if (buyPrice<0 && balance<1) continue; // if buy disabled & no money to sell
                int stock = shop.getStock();
                if (sellPrice<0 && stock==0) continue; // // if sell disabled & no stock to buy
                if (stock==0 && balance==0) continue; // // if no stock & no balance to buy

            /*check if query matches*/ 
                boolean include = false; 
                ItemStack item = shop.getShopItem().clone();
                String mat = item.getType().toString().replace("_", " ");
                    if (StringUtils.containsIgnoreCase(mat, query)) include = true;
                ItemMeta meta = item.getItemMeta();
                String name = meta.getDisplayName();
                    if (name!=null && include==false && StringUtils.containsIgnoreCase(name, query)) include = true;
                String desc = shop.getDescription();
                    if (include==false && StringUtils.containsIgnoreCase(desc, query)) include = true;
                if (shop.getOwnerUniqueId()!=null) {
                    String owner = DisplayShopAddon64.repHook.getRepManager().getPlayerUsername(shop.getOwnerUniqueId().toString());
                    if (include==false && StringUtils.containsIgnoreCase(owner, query)) include = true;
                } if (!include) continue;
            
            /*define item button's lore, name, etc. and add to gui*/
                List<String> lore = new ArrayList<>();    
                String priceLine = "";
                UUID uuid = shop.getOwnerUniqueId();

                ShopUtils.addGuiItemShop(gui, shop, item, meta, lore, balance, buyPrice, sellPrice, priceLine, stock, uuid, name, sender);
        }
    }

    /**
     * Sets up the footer of the GUI with categories & all other buttons.
     */
    private void setupFooter() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // Category: Pog
        ShopUtils.setGuiItemCatPog(gui, item, meta, lore);

        // Category: Blocks
        ShopUtils.setGuiItemCatBlocks(gui, item, meta, lore);

        // Category: Food
        ShopUtils.setGuiItemCatFood(gui, item, meta, lore);

        // Category: Drops
        ShopUtils.setGuiItemCatDrops(gui, item, meta, lore);

        // Category: Ores
        ShopUtils.setGuiItemCatOres(gui, item, meta, lore);

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