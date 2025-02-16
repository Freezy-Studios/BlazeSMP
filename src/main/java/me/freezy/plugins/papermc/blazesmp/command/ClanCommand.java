package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClanCommand extends SimpleCommand {
    private final BlazeSMP plugin;
    private final Clans clans;

    private final LinkedHashMap<Clan, LinkedList<UUID>> clanInvites = new LinkedHashMap<>();
    private final LinkedHashMap<Clan, LinkedList<UUID>> clanJoins = new LinkedHashMap<>();

    public ClanCommand() {
        super("clan");
        plugin = BlazeSMP.getInstance();
        clans = plugin.getClans();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String list, @NotNull String[] args) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String list, @NotNull String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        if (args.length == 1) {
            if (clans.isLeader(playerUUID)) {
                return Stream.of("info", "invite", "kick", "transfer", "promote", "demote", "disband", "leave", "accept", "deny", "modify")
                        .filter(s -> s.startsWith(args[0]))
                        .toList();
            } else if (clans.isVice(playerUUID)) {
                return Stream.of("info", "invite", "kick", "demote", "leave", "accept", "deny")
                        .filter(s -> s.startsWith(args[0]))
                        .toList();
            } else if (clans.isMember(playerUUID)) {
                return Stream.of("info","leave")
                        .filter(s -> s.startsWith(args[0]))
                        .toList();
            } else {
                return Stream.of("create", "join", "accept", "deny")
                        .filter(s -> s.startsWith(args[0]))
                        .toList();
            }
        } else if (args.length == 2) {
            if (clans.isLeader(playerUUID)) {
                if (args[0].equalsIgnoreCase("invite")) {
                    return Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !clans.isMember(p.getUniqueId()))
                            .map(Player::getName)
                            .filter(name -> name.startsWith(args[1]))
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("kick")) {
                    return clans.getClanByMember(playerUUID).getMembers().stream()
                            .map(UUID::toString)
                            .filter(s -> s.startsWith(args[1]))
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("promote")) {
                    return clans.getClanByMember(playerUUID).getMembers().stream()
                            .map(UUID::toString)
                            .filter(s -> s.startsWith(args[1]))
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("demote")) {
                    return Collections.singletonList(plugin.getServer().getOfflinePlayer(clans.getClanByMember(playerUUID).getViceUUID()).getName());
                } else if (args[0].equalsIgnoreCase("accept")) {
                    List<String> joins = getClanJoinRequests(args, playerUUID);
                    if (joins != null) return joins;
                } else if (args[0].equalsIgnoreCase("deny")) {
                    List<String> joins = getClanJoinRequests(args, playerUUID);
                    if (joins != null) return joins;
                } else if (args[0].equalsIgnoreCase("modify")) {
                    return Stream.of("name", "tag")
                            .filter(s -> s.startsWith(args[1]))
                            .toList();
                }
            } else if (clans.isVice(playerUUID)) {
                if (args[0].equalsIgnoreCase("invite")) {
                    return Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !clans.isMember(p.getUniqueId()))
                            .map(Player::getName)
                            .filter(name -> name.startsWith(args[1]))
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("kick")) {
                    return clans.getClanByMember(playerUUID).getMembers().stream()
                            .map(UUID::toString)
                            .filter(s -> s.startsWith(args[1]))
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("accept")) {
                    List<String> joins = getClanJoinRequests(args, playerUUID);
                    if (joins != null) return joins;
                } else if (args[0].equalsIgnoreCase("deny")) {
                    List<String> joins = getClanJoinRequests(args, playerUUID);
                    if (joins != null) return joins;
                } else if (args[0].equalsIgnoreCase("demote")) {
                    return Collections.singletonList(plugin.getServer().getOfflinePlayer(clans.getClanByMember(playerUUID).getViceUUID()).getName());
                }
            } else {
                if (args[0].equalsIgnoreCase("accept")) {
                    return clanInvites.entrySet().stream()
                            .filter(entry -> entry.getValue().contains(playerUUID))
                            .map(entry -> entry.getKey().getName())
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("deny")) {
                    return clanInvites.entrySet().stream()
                            .filter(entry -> entry.getValue().contains(playerUUID))
                            .map(entry -> entry.getKey().getName())
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("join")) {
                    return clans.getClans().stream()
                            .map(Clan::getName)
                            .filter(s -> s.startsWith(args[1]))
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("create")) {
                    return Collections.singletonList("<name>");
                }
            }
        } else if (args.length == 3) {
            if (clans.isLeader(playerUUID)) {
                if (args[1].equalsIgnoreCase("name")) {
                    return Collections.singletonList("<name>");
                } else if (args[1].equalsIgnoreCase("tag")) {
                    return Collections.singletonList("<tag>");
                }
            } else {
                if (args[0].equalsIgnoreCase("create")) {
                    return Collections.singletonList("<tag>");
                }
            }
        }
        return List.of();
    }

    @Nullable
    private List<String> getClanJoinRequests(@NotNull String[] args, UUID playerUUID) {
        LinkedList<UUID> joins = clanJoins.get(clans.getClanByMember(playerUUID));
        if (joins != null) {
            return joins.stream()
                    .map(uuid -> plugin.getServer().getOfflinePlayer(uuid).getName())
                    .filter(Objects::nonNull)
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
