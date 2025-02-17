package me.freezy.plugins.papermc.blazesmp.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

public class PlayerChatListener implements Listener {
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Team team = player.getScoreboard().getEntryTeam(player.getName());

        Component prefix = Component.empty();
        Component suffix = Component.empty();

        if (team != null) {
            team.prefix();
            prefix = team.prefix();
            team.suffix();
            suffix = team.suffix();
        }

        Component messageComponent = event.message();

        Component chatComponent = Component.empty()
                .append(prefix)
                .append(Component.text(player.getName()))
                .append(suffix)
                .append(Component.text(": "))
                .append(messageComponent);

        event.renderer((source, sourceDisplayName, msg, viewer) -> chatComponent);
    }
}
