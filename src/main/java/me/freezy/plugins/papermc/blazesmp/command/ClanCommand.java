package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public class ClanCommand extends SimpleCommand {
    private final BlazeSMP plugin;
    private final Clans clans;

    // Mapping: Clan -> Liste der Join-Anfragen (Spieler, die einer bestehenden Clan beitreten möchten)
    private final LinkedHashMap<Clan, LinkedList<UUID>> clanJoins = new LinkedHashMap<>();
    // Mapping: Clan -> Liste der Einladungen (Spieler, die vom Clan eingeladen wurden)
    private final LinkedHashMap<Clan, LinkedList<UUID>> clanInvites = new LinkedHashMap<>();

    public ClanCommand() {
        super("clan");
        plugin = BlazeSMP.getInstance();
        clans = plugin.getClans();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage().deserialize("<red>You must be a player to execute this command!</red>"));
            return true;
        }
        UUID playerUUID = player.getUniqueId();

        if (args.length == 0) {
            // Anzeige der Hilfemeldung – abhängig von der Rolle des Spielers
            if (clans.isLeader(playerUUID)) {
                Component l1 = miniMessage().deserialize("<color:#c70088>=== Clan Commands ===</color>\n");
                Component l2 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Displays clan info'><click:run_command:'/clan info'><color:#10abc7>/clan info</color></click></hover>\n");
                Component l3 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Invite a player to your clan'><click:run_command:'/clan invite'><color:#10abc7>/clan invite</color></click></hover>\n");
                Component l4 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Kick a player from your clan'><click:run_command:'/clan kick'><color:#10abc7>/clan kick</color></click></hover>\n");
                Component l5 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Transfer leadership'><click:run_command:'/clan transfer'><color:#10abc7>/clan transfer</color></click></hover>\n");
                Component l6 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Promote a member'><click:run_command:'/clan promote'><color:#10abc7>/clan promote</color></click></hover>\n");
                Component l7 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Demote a vice leader'><click:run_command:'/clan demote'><color:#10abc7>/clan demote</color></click></hover>\n");
                Component l8 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Disband your clan'><click:run_command:'/clan disband'><color:#10abc7>/clan disband</color></click></hover>\n");
                Component l9 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Leave your clan'><click:run_command:'/clan leave'><color:#10abc7>/clan leave</color></click></hover>\n");
                Component l10 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Accept a join request'><click:run_command:'/clan accept'><color:#10abc7>/clan accept</color></click></hover>\n");
                Component l11 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Deny a join request'><click:run_command:'/clan deny'><color:#10abc7>/clan deny</color></click></hover>\n");
                Component l12 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Modify your clan'><click:run_command:'/clan modify'><color:#10abc7>/clan modify</color></click></hover>\n");
                Component l13 = miniMessage().deserialize("<color:#c70088>=====================</color>");
                player.sendMessage(l1.append(l2).append(l3).append(l4).append(l5)
                        .append(l6).append(l7).append(l8).append(l9).append(l10).append(l11).append(l12).append(l13));
            } else if (clans.isVice(playerUUID)) {
                Component l1 = miniMessage().deserialize("<color:#c70088>=== Clan Commands ===</color>\n");
                Component l2 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Displays clan info'><click:run_command:'/clan info'><color:#10abc7>/clan info</color></click></hover>\n");
                Component l3 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Invite a player to your clan'><click:run_command:'/clan invite'><color:#10abc7>/clan invite</color></click></hover>\n");
                Component l4 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Kick a player from your clan'><click:run_command:'/clan kick'><color:#10abc7>/clan kick</color></click></hover>\n");
                Component l5 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Demote a vice leader'><click:run_command:'/clan demote'><color:#10abc7>/clan demote</color></click></hover>\n");
                Component l6 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Leave your clan'><click:run_command:'/clan leave'><color:#10abc7>/clan leave</color></click></hover>\n");
                Component l7 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Accept a join request'><click:run_command:'/clan accept'><color:#10abc7>/clan accept</color></click></hover>\n");
                Component l8 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Deny a join request'><click:run_command:'/clan deny'><color:#10abc7>/clan deny</color></click></hover>\n");
                Component l9 = miniMessage().deserialize("<color:#c70088>=====================</color>");
                player.sendMessage(l1.append(l2).append(l3).append(l4).append(l5)
                        .append(l6).append(l7).append(l8).append(l9));
            } else if (clans.isMember(playerUUID)) {
                Component l1 = miniMessage().deserialize("<color:#c70088>=== Clan Commands ===</color>\n");
                Component l2 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Displays clan info'><click:run_command:'/clan info'><color:#10abc7>/clan info</color></click></hover>\n");
                Component l3 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Leave your clan'><click:run_command:'/clan leave'><color:#10abc7>/clan leave</color></click></hover>\n");
                Component l4 = miniMessage().deserialize("<color:#c70088>=====================</color>");
                player.sendMessage(l1.append(l2).append(l3).append(l4));
            } else {
                Component l1 = miniMessage().deserialize("<color:#c70088>=== Clan Commands ===</color>\n");
                Component l2 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Create a clan'><click:run_command:'/clan create'><color:#10abc7>/clan create</color></click></hover>\n");
                Component l3 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Join a clan'><click:run_command:'/clan join'><color:#10abc7>/clan join</color></click></hover>\n");
                Component l4 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Accept a clan invite'><click:run_command:'/clan accept'><color:#10abc7>/clan accept</color></click></hover>\n");
                Component l5 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Deny a clan invite'><click:run_command:'/clan deny'><color:#10abc7>/clan deny</color></click></hover>\n");
                Component l6 = miniMessage().deserialize("<color:#c70088>=====================</color>");
                player.sendMessage(l1.append(l2).append(l3).append(l4).append(l5).append(l6));
            }
            return true;
        }

        // Verarbeitung der Unterbefehle
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create" -> {
                if (clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<red>You are already in a clan!</red>"));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(miniMessage().deserialize("<red>Usage: /clan create <name> <tag></red>"));
                    return true;
                }
                String clanName = args[1];
                String clanTag = args[2];
                Component tagComponent = miniMessage().deserialize(clanTag);
                Clan newClan = new Clan(clanName, tagComponent, playerUUID);
                clans.addClan(newClan);
                newClan.save();
                player.sendMessage(miniMessage().deserialize("<green>Clan created successfully!</green>"));
                return true;
            }
            case "join" -> {
                if (clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<red>You are already in a clan!</red>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize("<red>Usage: /clan join <clanName></red>"));
                    return true;
                }
                String targetClanName = args[1];
                Clan targetClan = clans.getClanByName(targetClanName);
                if (targetClan == null) {
                    player.sendMessage(miniMessage().deserialize("<red>Clan not found!</red>"));
                    return true;
                }
                // Füge eine Join-Anfrage hinzu
                clanJoins.computeIfAbsent(targetClan, k -> new LinkedList<>());
                LinkedList<UUID> joinRequests = clanJoins.get(targetClan);
                if (joinRequests.contains(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<yellow>You have already requested to join this clan.</yellow>"));
                    return true;
                }
                joinRequests.add(playerUUID);
                player.sendMessage(miniMessage().deserialize("<green>Join request sent to clan " + targetClan.getName() + "!</green>"));
                // Benachrichtige den Clan-Leader (sofern online) mit klickbaren Nachrichten
                Player leader = Bukkit.getPlayer(targetClan.getLeaderUUID());
                if (leader != null && leader.isOnline()) {
                    String acceptCommand = "/clan accept " + player.getName();
                    String denyCommand = "/clan deny " + player.getName();
                    Component notifyMsg = miniMessage().deserialize(
                            "<yellow>New join request from " + player.getName() + ".</yellow>\n" +
                                    "<click:run_command:'" + acceptCommand + "'><green>[Accept]</green></click> " +
                                    "<click:run_command:'" + denyCommand + "'><red>[Deny]</red></click>"
                    );
                    leader.sendMessage(notifyMsg);
                }
                return true;
            }
            case "invite" -> {
                if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<red>You are not authorized to invite players to a clan!</red>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize("<red>Usage: /clan invite <playerName></red>"));
                    return true;
                }
                String inviteeName = args[1];
                Player invitee = Bukkit.getPlayer(inviteeName);
                if (invitee == null || !invitee.isOnline()) {
                    player.sendMessage(miniMessage().deserialize("<red>Player " + inviteeName + " is not online!</red>"));
                    return true;
                }
                if (clans.isInClan(invitee.getUniqueId())) {
                    player.sendMessage(miniMessage().deserialize("<red>" + inviteeName + " is already in a clan!</red>"));
                    return true;
                }
                Clan inviterClan = clans.getClanByMember(playerUUID);
                if (inviterClan == null) {
                    player.sendMessage(miniMessage().deserialize("<red>Error: Your clan could not be found.</red>"));
                    return true;
                }
                clanInvites.computeIfAbsent(inviterClan, k -> new LinkedList<>());
                LinkedList<UUID> inviteList = clanInvites.get(inviterClan);
                if (inviteList.contains(invitee.getUniqueId())) {
                    player.sendMessage(miniMessage().deserialize("<yellow>Player " + inviteeName + " has already been invited!</yellow>"));
                    return true;
                }
                inviteList.add(invitee.getUniqueId());
                player.sendMessage(miniMessage().deserialize("<green>Invite sent to " + inviteeName + ".</green>"));
                // Benachrichtige den eingeladenen Spieler mit klickbaren Nachrichten
                String acceptCmd = "/clan accept " + inviterClan.getName();
                String denyCmd = "/clan deny " + inviterClan.getName();
                Component inviteNotify = miniMessage().deserialize(
                        "<yellow>Invite from clan " + inviterClan.getName() + ".</yellow>\n" +
                                "<click:run_command:'" + acceptCmd + "'><green>[Accept]</green></click> " +
                                "<click:run_command:'" + denyCmd + "'><red>[Deny]</red></click>"
                );
                invitee.sendMessage(inviteNotify);
                return true;
            }
            case "accept" -> {
                // Unterscheidung: Ist der Spieler noch in keinem Clan? -> Einladung annehmen.
                if (!clans.isInClan(playerUUID)) {
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize("<red>Usage: /clan accept <clanName></red>"));
                        return true;
                    }
                    String clanNameForInvite = args[1];
                    Clan invitedClan = null;
                    for (var entry : clanInvites.entrySet()) {
                        if (entry.getValue().contains(playerUUID) &&
                                entry.getKey().getName().equalsIgnoreCase(clanNameForInvite)) {
                            invitedClan = entry.getKey();
                            break;
                        }
                    }
                    if (invitedClan == null) {
                        player.sendMessage(miniMessage().deserialize("<red>No invite found from clan " + clanNameForInvite + ".</red>"));
                        return true;
                    }
                    invitedClan.getMembers().add(playerUUID);
                    clanInvites.get(invitedClan).remove(playerUUID);
                    player.sendMessage(miniMessage().deserialize("<green>You have joined the clan " + invitedClan.getName() + "!</green>"));
                    Player leader = Bukkit.getPlayer(invitedClan.getLeaderUUID());
                    if (leader != null && leader.isOnline()) {
                        leader.sendMessage(miniMessage().deserialize("<green>" + player.getName() + " has accepted the clan invite.</green>"));
                    }
                    invitedClan.save();
                } else {
                    // Akzeptiere eine Beitrittsanfrage – nur für Leader oder Vice
                    if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                        player.sendMessage(miniMessage().deserialize("<red>You are not authorized to accept join requests.</red>"));
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize("<red>Usage: /clan accept <playerName></red>"));
                        return true;
                    }
                    String joinRequesterName = args[1];
                    Clan currentClan = clans.getClanByMember(playerUUID);
                    if (currentClan == null) {
                        player.sendMessage(miniMessage().deserialize("<red>Error: Your clan could not be found.</red>"));
                        return true;
                    }
                    LinkedList<UUID> joinReqs = clanJoins.get(currentClan);
                    if (joinReqs == null || joinReqs.isEmpty()) {
                        player.sendMessage(miniMessage().deserialize("<red>No join requests available.</red>"));
                        return true;
                    }
                    UUID requesterUUID = null;
                    for (UUID uuid : joinReqs) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null && p.getName().equalsIgnoreCase(joinRequesterName)) {
                            requesterUUID = uuid;
                            break;
                        }
                    }
                    if (requesterUUID == null) {
                        player.sendMessage(miniMessage().deserialize("<red>No join request found from " + joinRequesterName + ".</red>"));
                        return true;
                    }
                    currentClan.getMembers().add(requesterUUID);
                    joinReqs.remove(requesterUUID);
                    player.sendMessage(miniMessage().deserialize("<green>You have accepted " + joinRequesterName + "'s join request.</green>"));
                    Player requester = Bukkit.getPlayer(requesterUUID);
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(miniMessage().deserialize("<green>Your join request for clan " + currentClan.getName() + " has been accepted.</green>"));
                    }
                    currentClan.save();
                }
                return true;
            }
            case "deny" -> {
                if (!clans.isInClan(playerUUID)) {
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize("<red>Usage: /clan deny <clanName></red>"));
                        return true;
                    }
                    String clanNameForInvite = args[1];
                    Clan invitedClan = null;
                    for (var entry : clanInvites.entrySet()) {
                        if (entry.getValue().contains(playerUUID) &&
                                entry.getKey().getName().equalsIgnoreCase(clanNameForInvite)) {
                            invitedClan = entry.getKey();
                            break;
                        }
                    }
                    if (invitedClan == null) {
                        player.sendMessage(miniMessage().deserialize("<red>No invite found from clan " + clanNameForInvite + ".</red>"));
                        return true;
                    }
                    clanInvites.get(invitedClan).remove(playerUUID);
                    player.sendMessage(miniMessage().deserialize("<red>You have declined the clan invite from " + invitedClan.getName() + ".</red>"));
                    Player leader = Bukkit.getPlayer(invitedClan.getLeaderUUID());
                    if (leader != null && leader.isOnline()) {
                        leader.sendMessage(miniMessage().deserialize("<red>" + player.getName() + " has declined the clan invite.</red>"));
                    }
                } else {
                    if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                        player.sendMessage(miniMessage().deserialize("<red>You are not authorized to deny join requests.</red>"));
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize("<red>Usage: /clan deny <playerName></red>"));
                        return true;
                    }
                    String joinRequesterName = args[1];
                    Clan currentClan = clans.getClanByMember(playerUUID);
                    if (currentClan == null) {
                        player.sendMessage(miniMessage().deserialize("<red>Error: Your clan could not be found.</red>"));
                        return true;
                    }
                    LinkedList<UUID> joinReqs = clanJoins.get(currentClan);
                    if (joinReqs == null || joinReqs.isEmpty()) {
                        player.sendMessage(miniMessage().deserialize("<red>No join requests available.</red>"));
                        return true;
                    }
                    UUID requesterUUID = null;
                    for (UUID uuid : joinReqs) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null && p.getName().equalsIgnoreCase(joinRequesterName)) {
                            requesterUUID = uuid;
                            break;
                        }
                    }
                    if (requesterUUID == null) {
                        player.sendMessage(miniMessage().deserialize("<red>No join request found from " + joinRequesterName + ".</red>"));
                        return true;
                    }
                    joinReqs.remove(requesterUUID);
                    player.sendMessage(miniMessage().deserialize("<red>You have denied " + joinRequesterName + "'s join request.</red>"));
                    Player requester = Bukkit.getPlayer(requesterUUID);
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(miniMessage().deserialize("<red>Your join request for clan " + currentClan.getName() + " has been denied.</red>"));
                    }
                }
                return true;
            }
            default -> {
                player.sendMessage(miniMessage().deserialize("<red>Unknown subcommand. Use /clan for help.</red>"));
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        if (args.length == 1) {
            if (clans.isLeader(playerUUID)) {
                return Stream.of("info", "invite", "kick", "transfer", "promote", "demote", "disband", "leave", "accept", "deny", "modify")
                        .filter(s -> s.startsWith(args[0])).toList();
            } else if (clans.isVice(playerUUID)) {
                return Stream.of("info", "invite", "kick", "demote", "leave", "accept", "deny")
                        .filter(s -> s.startsWith(args[0])).toList();
            } else if (clans.isMember(playerUUID)) {
                return Stream.of("info", "leave")
                        .filter(s -> s.startsWith(args[0])).toList();
            } else {
                return Stream.of("create", "join", "accept", "deny")
                        .filter(s -> s.startsWith(args[0])).toList();
            }
        } else if (args.length == 2) {
            if (clans.isLeader(playerUUID)) {
                if (args[0].equalsIgnoreCase("invite")) {
                    return Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !clans.isMember(p.getUniqueId()))
                            .map(Player::getName)
                            .filter(name -> name.startsWith(args[1])).collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("kick")) {
                    return clans.getClanByMember(playerUUID).getMembers().stream()
                            .map(UUID::toString)
                            .filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("promote")) {
                    return clans.getClanByMember(playerUUID).getMembers().stream()
                            .map(UUID::toString)
                            .filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("demote")) {
                    return Collections.singletonList(plugin.getServer().getOfflinePlayer(clans.getClanByMember(playerUUID).getViceUUID()).getName());
                } else if (args[0].equalsIgnoreCase("accept")) {
                    List<String> joins = getClanJoinRequests(args, playerUUID);
                    if (joins != null) return joins;
                } else if (args[0].equalsIgnoreCase("deny")) {
                    List<String> joins = getClanJoinRequests(args, playerUUID);
                    if (joins != null) return joins;
                } else if (args[0].equalsIgnoreCase("modify")) {
                    return Stream.of("name", "tag").filter(s -> s.startsWith(args[1])).toList();
                }
            } else if (clans.isVice(playerUUID)) {
                if (args[0].equalsIgnoreCase("invite")) {
                    return Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !clans.isMember(p.getUniqueId()))
                            .map(Player::getName)
                            .filter(name -> name.startsWith(args[1])).collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("kick")) {
                    return clans.getClanByMember(playerUUID).getMembers().stream()
                            .map(UUID::toString)
                            .filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
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
                            .filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
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
                    .filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
        }
        return null;
    }
}
