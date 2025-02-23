package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.manager.PlayerManager;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;


import java.util.UUID;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();
        new PlayerManager().setPlayerTeam(player);
        Team team = player.getScoreboard().getEntryTeam(player.getName());

        assert team != null;
        event.quitMessage(MiniMessage.miniMessage().deserialize(L4M4.get("player.left"))
                .append(player.playerListName()));
    }
}
