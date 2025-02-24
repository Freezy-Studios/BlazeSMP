package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
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

import java.util.HashSet;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private final HashSet<UUID> joinedPlayers = new HashSet<>();
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();
        new PlayerManager().setPlayerTeam(player);
        Team team = player.getScoreboard().getEntryTeam(player.getName());
        if (!joinedPlayers.contains(playerUUID)) {
            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("join.notify")));
            joinedPlayers.add(playerUUID);
        }

        // Verwende den zentral konfigurierten Join-Text aus der JSON-Datei
        assert team != null;
        event.joinMessage(MiniMessage.miniMessage().deserialize(L4M4.get("player.join"))
                .append(player.playerListName()));
    }
}
