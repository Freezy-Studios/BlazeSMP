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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VanishCommand extends SimpleCommand {
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private static boolean isvanished = true;

    public VanishCommand() {
        super("vanish");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Du bist kein Spieler!");
            return false;
        }
        Player player = (Player) sender;
        Player online = (Player) Bukkit.getOnlinePlayers();

        if (isvanished) {
            player.showPlayer(BlazeSMP.getInstance(), online);
            vanishedPlayers.remove(player.getUniqueId());
            online.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("player.join")));
        } else {
            player.hidePlayer(BlazeSMP.getInstance(), online);
            vanishedPlayers.add(player.getUniqueId());
            online.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("player.left")));
        }

        isvanished = !isvanished;
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}