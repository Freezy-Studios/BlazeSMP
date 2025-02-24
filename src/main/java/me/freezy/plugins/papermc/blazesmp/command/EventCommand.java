package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class EventCommand extends SimpleCommand {

    private final BlazeSMP plugin;

    public EventCommand(BlazeSMP plugin) {
        super("event");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("open")) {
            if (!(sender.getName().equals("BlazeGHC") || sender.getName().equals("EmrageGHC"))) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.no_permission")));
                return true;
            }

            if (plugin.isEndOpen()) {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "The End is already open!");
                return true;
            }

            if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
                for (int i = 10; i > 0; i--) {
                    int finalI = i;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        String titleMessage = "<red><bold>" + finalI + "</bold></red>";
                        String subtitleMessage = "<gradient:#ff0000:#ff00ff><bold>The End is opening soon!</bold></gradient>";
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.showTitle(Title.title(
                                    MiniMessage.miniMessage().deserialize(titleMessage),
                                    MiniMessage.miniMessage().deserialize(subtitleMessage)
                            ));
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                        });
                    }, (10 - i) * 20L);
                }

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.setEndOpen(true);
                    plugin.getConfig().set("isEndOpen", true);
                    plugin.saveConfig();
                    Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "The End is now open! Good luck!");
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                        player.showTitle(Title.title(
                                MiniMessage.miniMessage().deserialize("<gradient:#ff0000:#ff00ff><bold>Open!</bold></gradient>"),
                                MiniMessage.miniMessage().deserialize("<gradient:#00ff00:#00ffff><bold>Good luck!</bold></gradient>"),
                                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
                        ));
                    });
                }, 10 * 20L);
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}