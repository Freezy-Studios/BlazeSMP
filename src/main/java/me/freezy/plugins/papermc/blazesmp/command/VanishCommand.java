package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VanishCommand extends SimpleCommand {
    private final Map<UUID, Boolean> vanishedPlayers = new HashMap();




    public VanishCommand() {
        super("vanish");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(L4M4.get("error.not_a_player"));
            return false;
        }

        if (player.isOp()) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (vanishedPlayers.getOrDefault(player.getUniqueId(), false)) {
                    player.showPlayer(BlazeSMP.getInstance(), online);
                    online.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("player.join")).append(player.playerListName()));
                } else {
                    player.hidePlayer(BlazeSMP.getInstance(), online);
                    online.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("player.left")).append(player.playerListName()));
                }
            }
            if (vanishedPlayers.getOrDefault(player.getUniqueId(), false)) {
                vanishedPlayers.put(player.getUniqueId(), false);
            } else {
                vanishedPlayers.put(player.getUniqueId(), true);
            }
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.no_permission")));
            return false;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}