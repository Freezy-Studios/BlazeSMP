package me.freezy.plugins.papermc.blazesmp.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChatListener implements Listener {
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        Component messageComponent = MiniMessage.miniMessage().deserialize(PlainTextComponentSerializer.plainText().serialize(event.message()));

        Component chatComponent = Component.empty()
                .append(player.playerListName())
                .append(Component.text(": "))
                .append(messageComponent);

        event.renderer((source, sourceDisplayName, msg, viewer) -> chatComponent);
    }
}
