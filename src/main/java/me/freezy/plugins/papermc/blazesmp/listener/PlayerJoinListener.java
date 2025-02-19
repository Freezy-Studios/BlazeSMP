package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.manager.PlayerManager;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new PlayerManager().setPlayerTeam(player);

        // Verwende den zentral konfigurierten Join-Text aus der JSON-Datei
        event.joinMessage(MiniMessage.miniMessage().deserialize(L4M4.get("player.join"))
                .append(player.teamDisplayName()));
    }
}
