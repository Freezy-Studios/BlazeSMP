package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new PlayerManager().setPlayerTeam(player);
    }
}
