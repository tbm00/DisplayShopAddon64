package dev.tbm00.spigot.displayshopaddon64.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.tbm00.spigot.displayshopaddon64.ConfigHandler;

public class PlayerConnection implements Listener {
    private final ConfigHandler configHandler;

    public PlayerConnection(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    /**
     * Handles the player join event.
     *
     * @param event the PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {}

    /**
     * Handles the player quit event.
     *
     * @param event the PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {}
}