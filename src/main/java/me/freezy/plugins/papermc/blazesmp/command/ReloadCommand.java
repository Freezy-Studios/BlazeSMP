package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReloadCommand extends SimpleCommand {
    public ReloadCommand() {
        super("reloadconf");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.not_a_player")));
            return true;
        }
        BlazeSMP.getInstance().getClans().saveAllClans();
        BlazeSMP.getInstance().saveConfig();
        BlazeSMP.getInstance().reloadConfig();
        BlazeSMP.getInstance().getProtectedBlocks().save();
        BlazeSMP.getInstance().getHomes().save();
        L4M4.init();
        player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("config.reloaded")));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
