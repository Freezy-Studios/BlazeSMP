package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public class ClanCommand extends SimpleCommand {
    private final Clans clans;

    // Mapping: Clan -> Liste der Join-Anfragen (Spieler, die einer bestehenden Clan beitreten möchten)
    private final LinkedHashMap<Clan, LinkedList<UUID>> clanJoins = new LinkedHashMap<>();
    // Mapping: Clan -> Liste der Einladungen (Spieler, die vom Clan eingeladen wurden)
    private final LinkedHashMap<Clan, LinkedList<UUID>> clanInvites = new LinkedHashMap<>();

    public ClanCommand() {
        super("clan");
        BlazeSMP plugin = BlazeSMP.getInstance();
        clans = plugin.getClans();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage().deserialize("<color:red>You must be a player to execute this command!</color>"));
            return true;
        }
        UUID playerUUID = player.getUniqueId();

        // Keine Subcommands -> zeige Hilfe
        if (args.length == 0) {
            sendHelpMessage(player, playerUUID);
            return true;
        }

        // Verarbeitung der Unterbefehle
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {

            // ========== CREATE ==========
            case "create" -> {
                if (clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You are already in a clan!</color>"));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan create <name> <tag></color>"));
                    return true;
                }
                String clanName = args[1];
                String clanTag = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                Component tagComponent = miniMessage().deserialize(clanTag);

                Clan newClan = new Clan(clanName, tagComponent, playerUUID);
                // Clan hinzufügen und speichern
                clans.addClan(newClan);
                newClan.save();

                player.sendMessage(miniMessage().deserialize("<color:green>Clan created successfully!</color>"));
                return true;
            }
            case "chat" -> {
                if (!clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You are not in a clan!</color>"));
                    return true;
                }
                Clan clan = clans.getClanByMember(playerUUID);
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan chat <message></color>"));
                    return true;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                Component chatMessage = miniMessage().deserialize(
                        String.format("<color:#10abc7>[Clan] %s:</color> <color:#ff8800>%s</color>", player.getName(), message));
                for (UUID mem : clan.getMembers()) {
                    Player member = Bukkit.getPlayer(mem);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(chatMessage);
                    }
                }
                return true;
            }

            // ========== JOIN ==========
            case "join" -> {
                if (clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You are already in a clan!</color>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan join <clanName></color>"));
                    return true;
                }
                String targetClanName = args[1];
                Clan targetClan = clans.getClanByName(targetClanName);
                if (targetClan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Clan not found!</color>"));
                    return true;
                }

                clanJoins.computeIfAbsent(targetClan, k -> new LinkedList<>());
                LinkedList<UUID> joinRequests = clanJoins.get(targetClan);

                if (joinRequests.contains(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:yellow>You have already requested to join this clan.</color>"));
                    return true;
                }
                joinRequests.add(playerUUID);
                player.sendMessage(miniMessage().deserialize(
                        String.format("<color:green>Join request sent to clan %s!</color>", targetClan.getName())));

                // Benachrichtige den Clan-Leader (sofern online)
                Player leader = Bukkit.getPlayer(targetClan.getLeaderUUID());
                if (leader != null && leader.isOnline()) {
                    String acceptCommand = "/clan accept " + player.getName();
                    String denyCommand = "/clan deny " + player.getName();
                    Component notifyMsg = miniMessage().deserialize(
                            String.format("<color:yellow>New join request from %s.</color>\n", player.getName())
                                    + String.format("<click:run_command:'%s'><color:green>[Accept]</color></click> ", acceptCommand)
                                    + String.format("<click:run_command:'%s'><color:red>[Deny]</color></click>", denyCommand)
                    );
                    leader.sendMessage(notifyMsg);
                }
                return true;
            }

            // ========== INVITE ==========
            case "invite" -> {
                if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You are not authorized to invite players to a clan!</color>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan invite <playerName></color>"));
                    return true;
                }
                String inviteeName = args[1];
                Player invitee = Bukkit.getPlayer(inviteeName);
                if (invitee == null || !invitee.isOnline()) {
                    player.sendMessage(miniMessage().deserialize(String.format("<color:red>Player %s is not online!</color>", inviteeName)));
                    return true;
                }
                if (clans.isInClan(invitee.getUniqueId())) {
                    player.sendMessage(miniMessage().deserialize(String.format("<color:red>%s is already in a clan!</color>", inviteeName)));
                    return true;
                }
                Clan inviterClan = clans.getClanByMember(playerUUID);
                if (inviterClan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Error: Your clan could not be found.</color>"));
                    return true;
                }
                clanInvites.computeIfAbsent(inviterClan, k -> new LinkedList<>());
                LinkedList<UUID> inviteList = clanInvites.get(inviterClan);

                if (inviteList.contains(invitee.getUniqueId())) {
                    player.sendMessage(miniMessage().deserialize(String.format("<color:yellow>Player %s has already been invited!</color>", inviteeName)));
                    return true;
                }
                inviteList.add(invitee.getUniqueId());
                player.sendMessage(miniMessage().deserialize(String.format("<color:green>Invite sent to %s.</color>", inviteeName)));

                // Benachrichtige den Eingeladenen
                String acceptCmd = "/clan accept " + inviterClan.getName();
                String denyCmd = "/clan deny " + inviterClan.getName();
                Component inviteNotify = miniMessage().deserialize(
                        String.format("<color:yellow>Invite from clan %s.</color>\n", inviterClan.getName())
                                + String.format("<click:run_command:'%s'><color:green>[Accept]</color></click> ", acceptCmd)
                                + String.format("<click:run_command:'%s'><color:red>[Deny]</color></click>", denyCmd)
                );
                invitee.sendMessage(inviteNotify);
                return true;
            }

            // ========== ACCEPT ==========
            case "accept" -> {
                // 1) Spieler ist noch in keinem Clan -> Einladung annehmen
                if (!clans.isInClan(playerUUID)) {
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan accept <clanName></color>"));
                        return true;
                    }
                    String clanNameForInvite = args[1];
                    Clan invitedClan = null;
                    for (Map.Entry<Clan, LinkedList<UUID>> entry : clanInvites.entrySet()) {
                        if (entry.getValue().contains(playerUUID)
                                && entry.getKey().getName().equalsIgnoreCase(clanNameForInvite)) {
                            invitedClan = entry.getKey();
                            break;
                        }
                    }
                    if (invitedClan == null) {
                        player.sendMessage(miniMessage().deserialize(String.format("<color:red>No invite found from clan %s.</color>", clanNameForInvite)));
                        return true;
                    }
                    invitedClan.getMembers().add(playerUUID);
                    clanInvites.get(invitedClan).remove(playerUUID);

                    player.sendMessage(miniMessage().deserialize(
                            String.format("<color:green>You have joined the clan %s!</color>", invitedClan.getName())));

                    Player leader = Bukkit.getPlayer(invitedClan.getLeaderUUID());
                    if (leader != null && leader.isOnline()) {
                        leader.sendMessage(miniMessage().deserialize(
                                String.format("<color:green>%s has accepted the clan invite.</color>", player.getName())));
                    }
                    invitedClan.save();
                } else {
                    // 2) Spieler ist bereits in einem Clan -> Beitrittsanfrage annehmen (Leader/Vice)
                    if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                        player.sendMessage(miniMessage().deserialize("<color:red>You are not authorized to accept join requests.</color>"));
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan accept <playerName></color>"));
                        return true;
                    }
                    String joinRequesterName = args[1];
                    Clan currentClan = clans.getClanByMember(playerUUID);
                    if (currentClan == null) {
                        player.sendMessage(miniMessage().deserialize("<color:red>Error: Your clan could not be found.</color>"));
                        return true;
                    }
                    LinkedList<UUID> joinReqs = clanJoins.get(currentClan);
                    if (joinReqs == null || joinReqs.isEmpty()) {
                        player.sendMessage(miniMessage().deserialize("<color:red>No join requests available.</color>"));
                        return true;
                    }
                    UUID requesterUUID = getUuidByName(joinReqs, joinRequesterName);
                    if (requesterUUID == null) {
                        player.sendMessage(miniMessage().deserialize(String.format("<color:red>No join request found from %s.</color>", joinRequesterName)));
                        return true;
                    }
                    currentClan.getMembers().add(requesterUUID);
                    joinReqs.remove(requesterUUID);

                    player.sendMessage(miniMessage().deserialize(
                            String.format("<color:green>You have accepted %s's join request.</color>", joinRequesterName)));

                    Player requester = Bukkit.getPlayer(requesterUUID);
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(miniMessage().deserialize(
                                String.format("<color:green>Your join request for clan %s has been accepted.</color>", currentClan.getName())));
                    }
                    currentClan.save();
                }
                return true;
            }

            // ========== DENY ==========
            case "deny" -> {
                // 1) Spieler ist noch in keinem Clan -> Einladung ablehnen
                if (!clans.isInClan(playerUUID)) {
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan deny <clanName></color>"));
                        return true;
                    }
                    String clanNameForInvite = args[1];
                    Clan invitedClan = null;
                    for (Map.Entry<Clan, LinkedList<UUID>> entry : clanInvites.entrySet()) {
                        if (entry.getValue().contains(playerUUID)
                                && entry.getKey().getName().equalsIgnoreCase(clanNameForInvite)) {
                            invitedClan = entry.getKey();
                            break;
                        }
                    }
                    if (invitedClan == null) {
                        player.sendMessage(miniMessage().deserialize(String.format("<color:red>No invite found from clan %s.</color>", clanNameForInvite)));
                        return true;
                    }
                    clanInvites.get(invitedClan).remove(playerUUID);
                    player.sendMessage(miniMessage().deserialize(
                            String.format("<color:red>You have declined the clan invite from %s.</color>", invitedClan.getName())));

                    Player leader = Bukkit.getPlayer(invitedClan.getLeaderUUID());
                    if (leader != null && leader.isOnline()) {
                        leader.sendMessage(miniMessage().deserialize(
                                String.format("<color:red>%s has declined the clan invite.</color>", player.getName())));
                    }
                } else {
                    // 2) Spieler ist in einem Clan -> Beitrittsanfrage ablehnen (Leader/Vice)
                    if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                        player.sendMessage(miniMessage().deserialize("<color:red>You are not authorized to deny join requests.</color>"));
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan deny <playerName></color>"));
                        return true;
                    }
                    String joinRequesterName = args[1];
                    Clan currentClan = clans.getClanByMember(playerUUID);
                    if (currentClan == null) {
                        player.sendMessage(miniMessage().deserialize("<color:red>Error: Your clan could not be found.</color>"));
                        return true;
                    }
                    LinkedList<UUID> joinReqs = clanJoins.get(currentClan);
                    if (joinReqs == null || joinReqs.isEmpty()) {
                        player.sendMessage(miniMessage().deserialize("<color:red>No join requests available.</color>"));
                        return true;
                    }
                    UUID requesterUUID = getUuidByName(joinReqs, joinRequesterName);
                    if (requesterUUID == null) {
                        player.sendMessage(miniMessage().deserialize(String.format("<color:red>No join request found from %s.</color>", joinRequesterName)));
                        return true;
                    }
                    joinReqs.remove(requesterUUID);

                    player.sendMessage(miniMessage().deserialize(
                            String.format("<color:red>You have denied %s's join request.</color>", joinRequesterName)));

                    Player requester = Bukkit.getPlayer(requesterUUID);
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(miniMessage().deserialize(
                                String.format("<color:red>Your join request for clan %s has been denied.</color>", currentClan.getName())));
                    }
                }
                return true;
            }

            // ========== LIST ==========
            case "list" -> {
                // Zeige eine Liste aller existierenden Clans an
                sendClanList(player);
                return true;
            }

            // ========== INFO ==========
            case "info" -> {
                if (!clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You are not in a clan!</color>"));
                    return true;
                }
                Clan clan = clans.getClanByMember(playerUUID);
                if (clan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Could not find your clan.</color>"));
                    return true;
                }

                // Sammle die Daten
                UUID leaderUUID = clan.getLeaderUUID();
                UUID viceUUID = clan.getViceUUID();
                List<UUID> members = clan.getMembers();

                OfflinePlayer leaderOffline = Bukkit.getOfflinePlayer(leaderUUID);
                String leaderName = leaderOffline.getName() != null ? leaderOffline.getName() : leaderUUID.toString();

                String viceName = "<none>";
                if (viceUUID != null) {
                    OfflinePlayer viceOffline = Bukkit.getOfflinePlayer(viceUUID);
                    viceName = (viceOffline.getName() != null) ? viceOffline.getName() : viceUUID.toString();
                }

                // Wir bauen den Info-Text in mehreren Component-Teilen auf
                Component infoComponent = Component.empty();

                // Header
                Component l1 = miniMessage().deserialize("<color:#c70088>=== Clan info ===</color>\n");
                // ID
                Component l2 = miniMessage().deserialize(String.format(
                        "  <color:#c70088>-</color> <hover:show_text:'Unique identifier'><color:#10abc7>ID:</color></hover> <color:#ff8800>%s</color>\n",
                        clan.getUuid()
                ));
                // Name
                Component l3 = miniMessage().deserialize(String.format(
                        "  <color:#c70088>-</color> <hover:show_text:'Name'><color:#10abc7>Name:</color></hover> <color:#ff8800>%s</color>\n",
                        clan.getName()
                ));
                // Leader
                Component l4 = miniMessage().deserialize(String.format(
                        "  <color:#c70088>-</color> <hover:show_text:'Leader'><color:#10abc7>Leader:</color></hover> <color:#ff8800>%s</color>\n",
                        leaderName
                ));
                // Vice
                Component l5 = miniMessage().deserialize(String.format(
                        "  <color:#c70088>-</color> <hover:show_text:'Vice Leader'><color:#10abc7>Vice Leader:</color></hover> <color:#ff8800>%s</color>\n",
                        viceName
                ));
                // Member Count
                Component l6 = miniMessage().deserialize(String.format(
                        "  <color:#c70088>-</color> <hover:show_text:'Member count'><color:#10abc7>Members:</color></hover> <color:#ff8800>%d</color>\n",
                        members.size()
                ));
                // Tag
                Component l8 = miniMessage().deserialize(String.format(
                        "  <color:#c70088>-</color> <hover:show_text:'Clan tag'><color:#10abc7>Tag:</color></hover> <color:#ff8800>%s</color>\n",
                        miniMessage().serialize(clan.getTag())
                ));
                // Member-Liste
                Component l7 = miniMessage().deserialize("<color:#c70088>Members List:</color>\n");

                infoComponent = infoComponent
                        .append(l1).append(l2).append(l3)
                        .append(l4).append(l5).append(l6)
                        .append(l8).append(l7);

                // Detaillierte Auflistung der Mitglieder
                for (UUID mem : members) {
                    OfflinePlayer off = Bukkit.getOfflinePlayer(mem);
                    String name = off.getName() != null ? off.getName() : mem.toString();
                    Component memberLine = miniMessage().deserialize(String.format(
                            "  <color:#c70088>-</color> <hover:show_text:'Member'><color:#10abc7>Member:</color></hover> <color:#ff8800>%s</color>\n",
                            name
                    ));
                    infoComponent = infoComponent.append(memberLine);
                }

                // Abschlusslinie
                Component endLine = miniMessage().deserialize("<color:#c70088>=====================</color>");
                infoComponent = infoComponent.append(endLine);

                player.sendMessage(infoComponent);
                return true;
            }

            // ========== KICK ==========
            case "kick" -> {
                if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You are not authorized to kick players.</color>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan kick <playerName></color>"));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Could not find your clan.</color>"));
                    return true;
                }
                String targetName = args[1];
                OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);

                if (targetOffline.getName() == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>No such player found.</color>"));
                    return true;
                }
                UUID targetUUID = targetOffline.getUniqueId();

                // Check: Ist der Spieler im Clan?
                if (!currentClan.isMember(targetUUID)) {
                    player.sendMessage(miniMessage().deserialize(String.format("<color:red>%s is not in your clan!</color>", targetName)));
                    return true;
                }
                // Vice kann keinen Leader kicken
                if (clans.isVice(playerUUID) && currentClan.isLeader(targetUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You cannot kick the clan leader!</color>"));
                    return true;
                }

                // Vice entfernen, falls der Gekickte Vice war
                if (currentClan.isVice(targetUUID)) {
                    currentClan.setViceUUID(null);
                }
                currentClan.getMembers().remove(targetUUID);
                currentClan.save();

                player.sendMessage(miniMessage().deserialize(String.format("<color:green>You kicked %s from the clan.</color>", targetName)));
                if (targetOffline.isOnline()) {
                    Player targetOnline = (Player) targetOffline;
                    targetOnline.sendMessage(miniMessage().deserialize(String.format("<color:red>You have been kicked from the clan %s.</color>", currentClan.getName())));
                }
                return true;
            }

            // ========== TRANSFER ==========
            case "transfer" -> {
                if (!clans.isLeader(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Only the clan leader can transfer leadership.</color>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan transfer <playerName></color>"));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Could not find your clan.</color>"));
                    return true;
                }
                String newLeaderName = args[1];
                OfflinePlayer newLeaderOffline = Bukkit.getOfflinePlayer(newLeaderName);
                if (newLeaderOffline.getName() == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>No such player found.</color>"));
                    return true;
                }
                UUID newLeaderUUID = newLeaderOffline.getUniqueId();

                if (!currentClan.isMember(newLeaderUUID)) {
                    player.sendMessage(miniMessage().deserialize(String.format("<color:red>%s is not in your clan!</color>", newLeaderName)));
                    return true;
                }

                // Füge den alten Leader zur Members-Liste hinzu (falls nicht enthalten)
                if (!currentClan.getMembers().contains(playerUUID)) {
                    currentClan.getMembers().add(playerUUID);
                }
                // Setze neuen Leader
                currentClan.setLeaderUUID(newLeaderUUID);
                // Falls Vice, entfernen
                if (currentClan.isVice(newLeaderUUID)) {
                    currentClan.setViceUUID(null);
                }
                currentClan.save();

                player.sendMessage(miniMessage().deserialize(String.format("<color:green>You transferred leadership to %s.</color>", newLeaderName)));
                if (newLeaderOffline.isOnline()) {
                    ((Player) newLeaderOffline).sendMessage(miniMessage().deserialize(
                            String.format("<color:green>You are now the leader of the clan %s.</color>", currentClan.getName())));
                }
                return true;
            }

            // ========== PROMOTE ==========
            case "promote" -> {
                if (!clans.isLeader(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Only the clan leader can promote a member.</color>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan promote <playerName></color>"));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Could not find your clan.</color>"));
                    return true;
                }
                String promoteName = args[1];
                OfflinePlayer promoteOffline = Bukkit.getOfflinePlayer(promoteName);

                if (promoteOffline.getName() == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>No such player found.</color>"));
                    return true;
                }
                UUID promoteUUID = promoteOffline.getUniqueId();

                if (!currentClan.isMember(promoteUUID)) {
                    player.sendMessage(miniMessage().deserialize(String.format("<color:red>%s is not in your clan!</color>", promoteName)));
                    return true;
                }
                // Vice setzen
                currentClan.setViceUUID(promoteUUID);
                currentClan.save();

                player.sendMessage(miniMessage().deserialize(String.format("<color:green>You promoted %s to vice leader.</color>", promoteName)));
                if (promoteOffline.isOnline()) {
                    ((Player) promoteOffline).sendMessage(miniMessage().deserialize(
                            String.format("<color:green>You have been promoted to vice leader of clan %s.</color>", currentClan.getName())));
                }
                return true;
            }

            // ========== DEMOTE ==========
            case "demote" -> {
                if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You are not authorized to demote anyone.</color>"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan demote <playerName></color>"));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Could not find your clan.</color>"));
                    return true;
                }
                String demoteName = args[1];
                OfflinePlayer demoteOffline = Bukkit.getOfflinePlayer(demoteName);

                if (demoteOffline.getName() == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>No such player found.</color>"));
                    return true;
                }
                UUID demoteUUID = demoteOffline.getUniqueId();

                // Check, ob der Spieler Vice ist
                if (!currentClan.isVice(demoteUUID)) {
                    player.sendMessage(miniMessage().deserialize(String.format("<color:red>%s is not the vice leader!</color>", demoteName)));
                    return true;
                }
                // Vice kann nur sich selbst demoten (oder Leader kann Vice demoten)
                if (!clans.isLeader(playerUUID) && !playerUUID.equals(demoteUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You can only demote yourself!</color>"));
                    return true;
                }
                currentClan.setViceUUID(null);
                // Sicherstellen, dass der Spieler in der Memberliste bleibt
                if (!currentClan.getMembers().contains(demoteUUID)) {
                    currentClan.getMembers().add(demoteUUID);
                }
                currentClan.save();

                player.sendMessage(miniMessage().deserialize(String.format("<color:green>You demoted %s to a normal member.</color>", demoteName)));
                if (demoteOffline.isOnline()) {
                    ((Player) demoteOffline).sendMessage(miniMessage().deserialize(
                            String.format("<color:red>You have been demoted to a normal member of clan %s.</color>", currentClan.getName())));
                }
                return true;
            }

            // ========== DISBAND ==========
            case "disband" -> {
                if (!clans.isLeader(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Only the clan leader can disband the clan.</color>"));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Could not find your clan.</color>"));
                    return true;
                }

                // Benachrichtige alle Mitglieder
                for (UUID memberUUID : currentClan.getMembers()) {
                    Player memberOnline = Bukkit.getPlayer(memberUUID);
                    if (memberOnline != null && memberOnline.isOnline()) {
                        memberOnline.sendMessage(miniMessage().deserialize(
                                String.format("<color:red>Your clan %s has been disbanded by the leader.</color>", currentClan.getName())));
                    }
                }
                // Clan entfernen
                clans.removeClan(currentClan);
                player.sendMessage(miniMessage().deserialize("<color:green>You have disbanded your clan.</color>"));
                return true;
            }

            // ========== LEAVE ==========
            case "leave" -> {
                if (!clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>You are not in a clan!</color>"));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Could not find your clan.</color>"));
                    return true;
                }

                // Leader kann nicht einfach gehen, ohne disband oder transfer
                if (currentClan.isLeader(playerUUID)) {
                    if (!currentClan.getMembers().isEmpty()) {
                        player.sendMessage(miniMessage().deserialize("<color:red>You must transfer leadership or disband the clan before leaving.</color>"));
                    } else {
                        // Keine weiteren Mitglieder -> disband
                        clans.removeClan(currentClan);
                        player.sendMessage(miniMessage().deserialize(
                                "<color:green>You have disbanded your clan (no other members) and left.</color>"));
                    }
                    return true;
                }
                // Vice -> Vice-Position freigeben
                if (currentClan.isVice(playerUUID)) {
                    currentClan.setViceUUID(null);
                }
                currentClan.getMembers().remove(playerUUID);
                currentClan.save();

                player.sendMessage(miniMessage().deserialize(
                        String.format("<color:green>You have left the clan %s.</color>", currentClan.getName())));
                return true;
            }

            // ========== MODIFY (Name/Tag) ==========
            case "modify" -> {
                if (!clans.isLeader(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Only the clan leader can modify clan settings.</color>"));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Usage: /clan modify <name|tag> <newValue></color>"));
                    return true;
                }
                String whatToModify = args[1].toLowerCase();
                String newValue = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize("<color:red>Could not find your clan.</color>"));
                    return true;
                }

                switch (whatToModify) {
                    case "name" -> {
                        currentClan.setName(newValue);
                        currentClan.save();
                        player.sendMessage(miniMessage().deserialize(String.format("<color:green>Clan name changed to %s.</color>", newValue)));
                    }
                    case "tag" -> {
                        Component newTag = miniMessage().deserialize(newValue);
                        currentClan.setTag(newTag);
                        currentClan.save();
                        player.sendMessage(miniMessage().deserialize(String.format("<color:green>Clan tag changed to %s.</color>", newValue)));
                    }
                    default -> {
                        player.sendMessage(miniMessage().deserialize("<color:red>You can only modify 'name' or 'tag'.</color>"));
                    }
                }
                return true;
            }

            // ========== Fallback für Unbekanntes ==========
            default -> {
                player.sendMessage(miniMessage().deserialize("<color:red>Unknown subcommand. Use /clan for help.</color>"));
                return true;
            }
        }
    }

    /**
     * Zeigt eine Liste aller Clans auf dem Server an.
     */
    private void sendClanList(Player player) {
        // Hier nehmen wir an, dass 'clans.getClans()' alle existierenden Clans liefert.
        Collection<Clan> allClans = clans.getClans();
        if (allClans.isEmpty()) {
            player.sendMessage(miniMessage().deserialize("<color:red>No clans found!</color>"));
            return;
        }

        Component clanListComponent = Component.empty();
        clanListComponent = clanListComponent.append(
                miniMessage().deserialize("<color:#c70088>=== List of clans ===</color>\n"));

        for (Clan clan : allClans) {
            Component clanComponent = miniMessage().deserialize(
                    String.format("  <color:#c70088>-</color> <color:#ff8800>%s</color>\n", clan.getName()));
            clanListComponent = clanListComponent.append(clanComponent);
        }
        clanListComponent = clanListComponent.append(
                miniMessage().deserialize("<color:#c70088>=====================</color>"));

        player.sendMessage(clanListComponent);
    }

    /**
     * Hilfsfunktion: Zeigt eine übersichtliche Hilfe basierend auf der Rolle (Leader, Vice, Member, Kein-Mitglied).
     */
    private void sendHelpMessage(Player player, UUID playerUUID) {
        if (clans.isLeader(playerUUID)) {
            Component l1 = miniMessage().deserialize("<color:#c70088>=== Clan Commands ===</color>\n");
            Component l2 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Displays clan info'><click:run_command:'/clan info'><color:#10abc7>/clan info</color></click></hover>\n");
            Component l3 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Invite a player'><click:run_command:'/clan invite'><color:#10abc7>/clan invite</color></click></hover>\n");
            Component l4 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Kick a player'><click:run_command:'/clan kick'><color:#10abc7>/clan kick</color></click></hover>\n");
            Component l5 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Transfer leadership'><click:run_command:'/clan transfer'><color:#10abc7>/clan transfer</color></click></hover>\n");
            Component l6 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Promote a member'><click:run_command:'/clan promote'><color:#10abc7>/clan promote</color></click></hover>\n");
            Component l7 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Demote the vice leader'><click:run_command:'/clan demote'><color:#10abc7>/clan demote</color></click></hover>\n");
            Component l8 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Disband your clan'><click:run_command:'/clan disband'><color:#10abc7>/clan disband</color></click></hover>\n");
            Component l9 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Leave your clan'><click:run_command:'/clan leave'><color:#10abc7>/clan leave</color></click></hover>\n");
            Component l10 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Accept a join request'><click:run_command:'/clan accept'><color:#10abc7>/clan accept</color></click></hover>\n");
            Component l11 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Deny a join request'><click:run_command:'/clan deny'><color:#10abc7>/clan deny</color></click></hover>\n");
            Component l12 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Modify clan name or tag'><click:run_command:'/clan modify'><color:#10abc7>/clan modify</color></click></hover>\n");
            Component l13 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Show a list of all clans'><click:run_command:'/clan list'><color:#10abc7>/clan list</color></click></hover>\n");
            Component l14 = miniMessage().deserialize("<color:#c70088>=====================</color>");

            player.sendMessage(l1.append(l2).append(l3).append(l4).append(l5)
                    .append(l6).append(l7).append(l8).append(l9).append(l10).append(l11).append(l12).append(l13).append(l14));
        } else if (clans.isVice(playerUUID)) {
            Component l1 = miniMessage().deserialize("<color:#c70088>=== Clan Commands ===</color>\n");
            Component l2 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Displays clan info'><click:run_command:'/clan info'><color:#10abc7>/clan info</color></click></hover>\n");
            Component l3 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Invite a player'><click:run_command:'/clan invite'><color:#10abc7>/clan invite</color></click></hover>\n");
            Component l4 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Kick a player'><click:run_command:'/clan kick'><color:#10abc7>/clan kick</color></click></hover>\n");
            Component l5 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Demote the vice leader'><click:run_command:'/clan demote'><color:#10abc7>/clan demote</color></click></hover>\n");
            Component l6 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Leave your clan'><click:run_command:'/clan leave'><color:#10abc7>/clan leave</color></click></hover>\n");
            Component l7 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Accept a join request'><click:run_command:'/clan accept'><color:#10abc7>/clan accept</color></click></hover>\n");
            Component l8 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Deny a join request'><click:run_command:'/clan deny'><color:#10abc7>/clan deny</color></click></hover>\n");
            Component l9 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Show a list of all clans'><click:run_command:'/clan list'><color:#10abc7>/clan list</color></click></hover>\n");
            Component l10 = miniMessage().deserialize("<color:#c70088>=====================</color>");

            player.sendMessage(l1.append(l2).append(l3).append(l4).append(l5)
                    .append(l6).append(l7).append(l8).append(l9).append(l10));
        } else if (clans.isMember(playerUUID)) {
            Component l1 = miniMessage().deserialize("<color:#c70088>=== Clan Commands ===</color>\n");
            Component l2 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Displays clan info'><click:run_command:'/clan info'><color:#10abc7>/clan info</color></click></hover>\n");
            Component l3 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Leave your clan'><click:run_command:'/clan leave'><color:#10abc7>/clan leave</color></click></hover>\n");
            Component l4 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Show a list of all clans'><click:run_command:'/clan list'><color:#10abc7>/clan list</color></click></hover>\n");
            Component l5 = miniMessage().deserialize("<color:#c70088>=====================</color>");

            player.sendMessage(l1.append(l2).append(l3).append(l4).append(l5));
        } else {
            // Spieler ist in keinem Clan
            Component l1 = miniMessage().deserialize("<color:#c70088>=== Clan Commands ===</color>\n");
            Component l2 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Create a clan'><click:run_command:'/clan create'><color:#10abc7>/clan create</color></click></hover>\n");
            Component l3 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Join a clan'><click:run_command:'/clan join'><color:#10abc7>/clan join</color></click></hover>\n");
            Component l4 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Accept a clan invite'><click:run_command:'/clan accept'><color:#10abc7>/clan accept</color></click></hover>\n");
            Component l5 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Deny a clan invite'><click:run_command:'/clan deny'><color:#10abc7>/clan deny</color></click></hover>\n");
            Component l6 = miniMessage().deserialize("  <color:#c70088>-</color> <hover:show_text:'Show a list of all clans'><click:run_command:'/clan list'><color:#10abc7>/clan list</color></click></hover>\n");
            Component l7 = miniMessage().deserialize("<color:#c70088>=====================</color>");

            player.sendMessage(l1.append(l2).append(l3).append(l4).append(l5).append(l6).append(l7));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command cmd,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        UUID playerUUID = player.getUniqueId();

        // Erste Ebene der Subcommands
        if (args.length == 1) {
            if (clans.isLeader(playerUUID)) {
                return Stream.of("info", "chat", "invite", "kick", "transfer", "promote",
                                "demote", "disband", "leave", "accept", "deny",
                                "modify", "list")
                        .filter(s -> s.startsWith(args[0]))
                        .collect(Collectors.toList());
            } else if (clans.isVice(playerUUID)) {
                return Stream.of("info", "chat", "invite", "kick", "demote",
                                "leave", "accept", "deny", "list")
                        .filter(s -> s.startsWith(args[0]))
                        .collect(Collectors.toList());
            } else if (clans.isMember(playerUUID)) {
                return Stream.of("info", "chat", "leave", "list")
                        .filter(s -> s.startsWith(args[0]))
                        .collect(Collectors.toList());
            } else {
                return Stream.of("create", "join", "accept", "deny", "list")
                        .filter(s -> s.startsWith(args[0]))
                        .collect(Collectors.toList());
            }
        }

        // Zweite Ebene
        if (args.length == 2) {
            if (clans.isLeader(playerUUID)) {
                switch (args[0].toLowerCase()) {
                    case "invite" -> {
                        return Bukkit.getOnlinePlayers().stream()
                                .filter(p -> !clans.isMember(p.getUniqueId()))
                                .map(Player::getName)
                                .filter(name -> name.startsWith(args[1]))
                                .collect(Collectors.toList());
                    }
                    case "kick", "promote" -> {
                        Clan clan = clans.getClanByMember(playerUUID);
                        if (clan == null) return List.of();
                        return clan.getMembers().stream()
                                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                                .filter(Objects::nonNull)
                                .filter(name -> name.startsWith(args[1]))
                                .collect(Collectors.toList());
                    }
                    case "demote" -> {
                        Clan clan = clans.getClanByMember(playerUUID);
                        if (clan == null || clan.getViceUUID() == null) return List.of();
                        String viceName = Optional.ofNullable(
                                        Bukkit.getOfflinePlayer(clan.getViceUUID()).getName())
                                .orElse(clan.getViceUUID().toString());
                        return viceName.startsWith(args[1])
                                ? List.of(viceName)
                                : List.of();
                    }
                    case "accept", "deny" -> {
                        List<String> joins = getClanJoinRequests(args, playerUUID);
                        if (joins != null) return joins;
                    }
                    case "modify" -> {
                        return Stream.of("name", "tag")
                                .filter(s -> s.startsWith(args[1]))
                                .collect(Collectors.toList());
                    }
                    default -> {}
                }
            } else if (clans.isVice(playerUUID)) {
                switch (args[0].toLowerCase()) {
                    case "invite" -> {
                        return Bukkit.getOnlinePlayers().stream()
                                .filter(p -> !clans.isMember(p.getUniqueId()))
                                .map(Player::getName)
                                .filter(name -> name.startsWith(args[1]))
                                .collect(Collectors.toList());
                    }
                    case "kick" -> {
                        Clan clan = clans.getClanByMember(playerUUID);
                        if (clan == null) return List.of();
                        return clan.getMembers().stream()
                                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                                .filter(Objects::nonNull)
                                .filter(name -> name.startsWith(args[1]))
                                .collect(Collectors.toList());
                    }
                    case "demote" -> {
                        Clan clan = clans.getClanByMember(playerUUID);
                        if (clan == null || clan.getViceUUID() == null) return List.of();
                        String viceName = Optional.ofNullable(
                                        Bukkit.getOfflinePlayer(clan.getViceUUID()).getName())
                                .orElse(clan.getViceUUID().toString());
                        return viceName.startsWith(args[1])
                                ? List.of(viceName)
                                : List.of();
                    }
                    case "accept", "deny" -> {
                        List<String> joins = getClanJoinRequests(args, playerUUID);
                        if (joins != null) return joins;
                    }
                    default -> {}
                }
            } else {
                switch (args[0].toLowerCase()) {
                    case "accept", "deny" -> {
                        // Zeige alle Clan-Einladungen an
                        return clanInvites.entrySet().stream()
                                .filter(entry -> entry.getValue().contains(playerUUID))
                                .map(entry -> entry.getKey().getName())
                                .filter(name -> name.startsWith(args[1]))
                                .collect(Collectors.toList());
                    }
                    case "join" -> {
                        // Zeige alle Clans an
                        return clans.getClans().stream()
                                .map(Clan::getName)
                                .filter(s -> s.startsWith(args[1]))
                                .collect(Collectors.toList());
                    }
                    case "create" -> {
                        return Collections.singletonList("<name>");
                    }
                    default -> {}
                }
            }
        }

        // Dritte Ebene (z. B. /clan modify name <newName>)
        if (args.length == 3) {
            if (clans.isLeader(playerUUID)) {
                if (args[0].equalsIgnoreCase("modify")) {
                    if (args[1].equalsIgnoreCase("name")) {
                        return Collections.singletonList("<newName>");
                    } else if (args[1].equalsIgnoreCase("tag")) {
                        return Collections.singletonList("<newTag>");
                    }
                }
            } else if (args[0].equalsIgnoreCase("create")) {
                return Collections.singletonList("<tag>");
            }
        }
        return List.of();
    }

    /**
     * Liefert zu einem Namen die passende UUID aus einer UUID-Liste (z. B. Join-Anfragen).
     */
    @Nullable
    private UUID getUuidByName(List<UUID> uuidList, String playerName) {
        for (UUID uuid : uuidList) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
            if (off.getName() != null && off.getName().equalsIgnoreCase(playerName)) {
                return uuid;
            }
        }
        return null;
    }

    /**
     * Hilfsfunktion für Tab-Completion (Zeigt Spielernamen von Join-Anfragen).
     */
    @Nullable
    private List<String> getClanJoinRequests(@NotNull String[] args, UUID playerUUID) {
        Clan clan = clans.getClanByMember(playerUUID);
        if (clan == null) return null;

        LinkedList<UUID> joins = clanJoins.get(clan);
        if (joins == null) return null;

        return joins.stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull)
                .filter(name -> name.startsWith(args[1]))
                .collect(Collectors.toList());
    }
}
