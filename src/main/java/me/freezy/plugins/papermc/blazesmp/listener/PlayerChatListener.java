package me.freezy.plugins.papermc.blazesmp.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

public class PlayerChatListener implements Listener {
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        Component messageComponent = event.message();

        Component chatComponent = Component.empty()
                .append(player.playerListName())
                .append(Component.text(": "))
                .append(messageComponent);

        player.sendMessage(player.playerListName());

        event.renderer((source, sourceDisplayName, msg, viewer) -> chatComponent);
    }
}
