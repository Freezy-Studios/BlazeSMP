package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.manager.Homes;
import me.freezy.plugins.papermc.blazesmp.tasks.PlayerTeleportHomeTimer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HomeCommand extends SimpleCommand {
    public HomeCommand() {
        super("home", List.of("sethome", "delhome"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be a player to use this command!</red>"));
            return true;
        }
        Homes homes = BlazeSMP.getInstance().getHomes();

        if (label.equalsIgnoreCase("sethome")) {
            homes.setHome(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Home set!</green>"));
        } else if (label.equalsIgnoreCase("delhome")) {
            homes.removeHome(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Home removed!</red>"));
        } else {
            if (homes.hasHome(player)) {
                // Informiere den Spieler, dass der Teleport-Vorgang gestartet wird
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Teleporting to home!</yellow>"));
                new PlayerTeleportHomeTimer(player).runTaskTimer(BlazeSMP.getInstance(), 0, 1);
            } else {
                homes.setHome(player);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Home set!</green>"));
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
