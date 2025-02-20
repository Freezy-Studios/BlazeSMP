package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DiscordCommand extends SimpleCommand {

    private final String discordUrl;

    public DiscordCommand() {
        super("discord");
        this.discordUrl = BlazeSMP.getInstance().getConfig().getString("discord-url");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Component message = MiniMessage.miniMessage().deserialize("<click:open_url:'" + discordUrl + "'><gradient:#00ff00:#0000ff><b>Click here to join our Discord!</b></gradient></click>");
            player.sendMessage(message);
        } else {
            sender.sendMessage("This command can only be used by players.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}