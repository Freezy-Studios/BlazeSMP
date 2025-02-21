package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazeghcsmpclan.Main;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class EndPortalListener implements Listener {

    private final Main plugin;

    public EndPortalListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World world = event.getTo().getWorld();

        if (world != null && world.getEnvironment() == Environment.THE_END) {
            if (!plugin.isEndOpen()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Das End ist noch nicht geöffnet!");
            }
        }
    }
}