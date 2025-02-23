package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RestartCommand extends SimpleCommand {
    public RestartCommand() {
        super("restart");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.no_permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.no_reason")));
            return true;
        }

        String reason = String.join(" ", args);
        Component kickMessage = MiniMessage.miniMessage().deserialize(reason);

        Bukkit.getOnlinePlayers().forEach(player -> player.kick(kickMessage));

        Bukkit.getScheduler().runTaskLater(BlazeSMP.getInstance(), Bukkit::shutdown, 60L); // 3 seconds later (60 ticks)

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}