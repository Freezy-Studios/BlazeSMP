package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.manager.PlayerManager;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new PlayerManager().setPlayerTeam(player);
        Team team = player.getScoreboard().getEntryTeam(player.getName());

        // Verwende den zentral konfigurierten Join-Text aus der JSON-Datei
        assert team != null;
        event.joinMessage(MiniMessage.miniMessage().deserialize(L4M4.get("player.join"))
                .append(player.playerListName()));
    }
}
