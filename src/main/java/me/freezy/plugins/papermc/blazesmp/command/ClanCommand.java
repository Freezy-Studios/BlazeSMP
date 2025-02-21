package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
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

public class ClanCommand extends SimpleCommand {

    private final Clans clans;

    // Mapping: Clan -> Liste der Join-Anfragen (Spieler, die einem Clan beitreten möchten)
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
            sender.sendMessage(miniMessage().deserialize(L4M4.get("error.not_a_player")));
            return true;
        }
        UUID playerUUID = player.getUniqueId();

        // Keine Subcommands → Hilfe anzeigen
        if (args.length == 0) {
            sendHelpMessage(player, playerUUID);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            // ========== CREATE ==========
            case "create" -> {
                if (clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.already_in_clan")));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_create")));
                    return true;
                }
                String clanName = args[1];
                String clanTag = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                Component tagComponent = miniMessage().deserialize(clanTag);

                Clan newClan = new Clan(clanName, tagComponent, playerUUID);
                clans.addClan(newClan);
                newClan.save();
                player.sendMessage(miniMessage().deserialize(L4M4.get("success.clan_created")));
                return true;
            }

            // ========== CHAT ==========
            case "chat" -> {
                if (!clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.not_in_clan_chat")));
                    return true;
                }
                Clan clan = clans.getClanByMember(playerUUID);
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_chat")));
                    return true;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                // Verwende den externen Chat-Format-String
                Component chatMessage = miniMessage().deserialize(
                        String.format(L4M4.get("chat.format"), player.getName(), message)
                ).decoration(TextDecoration.ITALIC, true);
                player.sendMessage(chatMessage);
                // Sende Nachricht an alle Clanmitglieder (außer Sender)
                for (UUID mem : clan.getMembers()) {
                    if (mem.equals(playerUUID)) continue;
                    Player member = Bukkit.getPlayer(mem);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(chatMessage);
                    }
                }
                // Zusätzlich Leader und Vice benachrichtigen
                Player leader = Bukkit.getPlayer(clan.getLeaderUUID());
                if (leader != null && leader.isOnline() && !leader.getUniqueId().equals(playerUUID)) {
                    leader.sendMessage(chatMessage);
                }
                if (clan.getViceUUID() != null) {
                    Player vice = Bukkit.getPlayer(clan.getViceUUID());
                    if (vice != null && vice.isOnline() && !vice.getUniqueId().equals(playerUUID)) {
                        vice.sendMessage(chatMessage);
                    }
                }
                return true;
            }

            // ========== JOIN ==========
            case "join" -> {
                if (clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.already_in_clan")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_join")));
                    return true;
                }
                String targetClanName = args[1];
                Clan targetClan = clans.getClanByName(targetClanName);
                if (targetClan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                    return true;
                }
                if (targetClan.getMembers().size() >= 10) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_full")));
                    return true;
                }
                clanJoins.computeIfAbsent(targetClan, k -> new LinkedList<>());
                LinkedList<UUID> joinRequests = clanJoins.get(targetClan);
                if (joinRequests.contains(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.already_requested")));
                    return true;
                }
                joinRequests.add(playerUUID);
                player.sendMessage(miniMessage().deserialize(
                        String.format(L4M4.get("success.join_request_sent"), targetClan.getName())
                ));
                Player leader = Bukkit.getPlayer(targetClan.getLeaderUUID());
                if (leader != null && leader.isOnline()) {
                    String acceptCommand = "/clan accept " + player.getName();
                    String denyCommand = "/clan deny " + player.getName();
                    String notifyText = String.format(L4M4.get("notification.invite"), targetClan.getName());
                    Component notifyMsg = miniMessage().deserialize(notifyText)
                            .append(Component.text(" "))
                            .append(miniMessage().deserialize(
                                    "<click:run_command:'" + acceptCommand + "'>" + L4M4.get("button.accept") + "</click>"
                            ))
                            .append(Component.text(" "))
                            .append(miniMessage().deserialize(
                                    "<click:run_command:'" + denyCommand + "'>" + L4M4.get("button.deny") + "</click>"
                            ));
                    leader.sendMessage(notifyMsg);
                }
                return true;
            }

            // ========== INVITE ==========
            case "invite" -> {
                if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.not_authorized_invite")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_invite")));
                    return true;
                }
                String inviteeName = args[1];
                Player invitee = Bukkit.getPlayer(inviteeName);
                if (invitee == null || !invitee.isOnline()) {
                    player.sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("error.player_not_online"), inviteeName)
                    ));
                    return true;
                }
                if (clans.isInClan(invitee.getUniqueId())) {
                    player.sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("error.player_already_in_clan"), inviteeName)
                    ));
                    return true;
                }
                Clan inviterClan = clans.getClanByMember(playerUUID);
                if (inviterClan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                    return true;
                }
                if (inviterClan.getMembers().size() >= 10) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_full")));
                    return true;
                }
                clanInvites.computeIfAbsent(inviterClan, k -> new LinkedList<>());
                LinkedList<UUID> inviteList = clanInvites.get(inviterClan);
                if (inviteList.contains(invitee.getUniqueId())) {
                    player.sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("error.already_requested"), inviteeName)
                    ));
                    return true;
                }
                inviteList.add(invitee.getUniqueId());
                player.sendMessage(miniMessage().deserialize(
                        String.format(L4M4.get("success.invite_sent"), inviteeName)
                ));
                String acceptCmd = "/clan accept " + inviterClan.getName();
                String denyCmd = "/clan deny " + inviterClan.getName();
                String inviteNotifyText = String.format(L4M4.get("notification.invite"), inviterClan.getName());
                Component inviteNotify = miniMessage().deserialize(inviteNotifyText)
                        .append(Component.text("\n"))
                        .append(miniMessage().deserialize(
                                "<click:run_command:'" + acceptCmd + "'>" + L4M4.get("button.accept") + "</click>"
                        ))
                        .append(Component.text(" "))
                        .append(miniMessage().deserialize(
                                "<click:run_command:'" + denyCmd + "'>" + L4M4.get("button.deny") + "</click>"
                        ));
                invitee.sendMessage(inviteNotify);
                return true;
            }

            // ========== ACCEPT ==========
            case "accept" -> {
                if (!clans.isInClan(playerUUID)) {
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_accept")));
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
                        player.sendMessage(miniMessage().deserialize(
                                String.format(L4M4.get("error.invite_not_found"), clanNameForInvite)
                        ));
                        return true;
                    }
                    invitedClan.getMembers().add(playerUUID);
                    clanInvites.get(invitedClan).remove(playerUUID);
                    player.sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("success.join_clan"), invitedClan.getName())
                    ));
                    Player leader = Bukkit.getPlayer(invitedClan.getLeaderUUID());
                    if (leader != null && leader.isOnline()) {
                        leader.sendMessage(miniMessage().deserialize(
                                String.format(L4M4.get("success.invite_accepted_notify"), player.getName())
                        ));
                    }
                    invitedClan.save();
                } else {
                    if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("error.not_authorized_accept")));
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_accept_request")));
                        return true;
                    }
                    String joinRequesterName = args[1];
                    Clan currentClan = clans.getClanByMember(playerUUID);
                    if (currentClan == null) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                        return true;
                    }
                    LinkedList<UUID> joinReqs = clanJoins.get(currentClan);
                    if (joinReqs == null || joinReqs.isEmpty()) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("error.no_join_requests")));
                        return true;
                    }
                    UUID requesterUUID = getUuidByName(joinReqs, joinRequesterName);
                    if (requesterUUID == null) {
                        player.sendMessage(miniMessage().deserialize(
                                String.format(L4M4.get("error.join_request_not_found"), joinRequesterName)
                        ));
                        return true;
                    }
                    currentClan.getMembers().add(requesterUUID);
                    joinReqs.remove(requesterUUID);
                    player.sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("success.join_request_accepted"), joinRequesterName)
                    ));
                    Player requester = Bukkit.getPlayer(requesterUUID);
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(miniMessage().deserialize(
                                String.format(L4M4.get("success.join_request_accepted_notify"), currentClan.getName())
                        ));
                    }
                    currentClan.save();
                }
                return true;
            }

            // ========== DENY ==========
            case "deny" -> {
                if (!clans.isInClan(playerUUID)) {
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_deny_invite")));
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
                        player.sendMessage(miniMessage().deserialize(
                                String.format(L4M4.get("error.invite_not_found"), clanNameForInvite)
                        ));
                        return true;
                    }
                    clanInvites.get(invitedClan).remove(playerUUID);
                    player.sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("error.invite_declined"), invitedClan.getName())
                    ));
                    Player leader = Bukkit.getPlayer(invitedClan.getLeaderUUID());
                    if (leader != null && leader.isOnline()) {
                        leader.sendMessage(miniMessage().deserialize(
                                String.format(L4M4.get("error.invite_declined_notify"), player.getName())
                        ));
                    }
                } else {
                    if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("error.not_authorized_deny")));
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_deny_request")));
                        return true;
                    }
                    String joinRequesterName = args[1];
                    Clan currentClan = clans.getClanByMember(playerUUID);
                    if (currentClan == null) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                        return true;
                    }
                    LinkedList<UUID> joinReqs = clanJoins.get(currentClan);
                    if (joinReqs == null || joinReqs.isEmpty()) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("error.no_join_requests")));
                        return true;
                    }
                    UUID requesterUUID = getUuidByName(joinReqs, joinRequesterName);
                    if (requesterUUID == null) {
                        player.sendMessage(miniMessage().deserialize(
                                String.format(L4M4.get("error.join_request_not_found"), joinRequesterName)
                        ));
                        return true;
                    }
                    joinReqs.remove(requesterUUID);
                    player.sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("error.join_request_denied"), joinRequesterName)
                    ));
                    Player requester = Bukkit.getPlayer(requesterUUID);
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(miniMessage().deserialize(
                                String.format(L4M4.get("error.join_request_denied_notify"), currentClan.getName())
                        ));
                    }
                }
                return true;
            }

            // ========== LIST ==========
            case "list" -> {
                Collection<Clan> allClans = clans.getClans();
                if (allClans.isEmpty()) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.no_clans")));
                    return true;
                }
                Component clanListComponent = miniMessage().deserialize("<color:#c70088>=== List of clans ===</color>\n");
                for (Clan clan : allClans) {
                    clanListComponent = clanListComponent.append(
                            miniMessage().deserialize(String.format("  <color:#c70088>-</color> <color:#ff8800>%s</color>\n", clan.getName()))
                    );
                }
                clanListComponent = clanListComponent.append(miniMessage().deserialize("<color:#c70088>=====================</color>"));
                player.sendMessage(clanListComponent);
                return true;
            }

            // ========== INFO ==========
            case "info" -> {
                if (!clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.not_in_clan_info")));
                    return true;
                }
                Clan clan = clans.getClanByMember(playerUUID);
                if (clan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found_info")));
                    return true;
                }

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

                // Baue den Info-Text mithilfe der externen Message-Templates
                Component infoComponent = Component.empty()
                        .append(miniMessage().deserialize(L4M4.get("info.header")))
                        .append(miniMessage().deserialize(String.format(L4M4.get("info.uuid"), clan.getUuid())))
                        .append(miniMessage().deserialize(String.format(L4M4.get("info.name"), clan.getName())))
                        .append(miniMessage().deserialize(String.format(L4M4.get("info.leader"), leaderName)))
                        .append(miniMessage().deserialize(String.format(L4M4.get("info.vice"), viceName)))
                        .append(miniMessage().deserialize(String.format(L4M4.get("info.members_count"), members.size())))
                        .append(miniMessage().deserialize(String.format(L4M4.get("info.tag"), miniMessage().serialize(clan.getTag()))))
                        .append(miniMessage().deserialize(L4M4.get("info.members_list_header")));

                for (UUID mem : members) {
                    OfflinePlayer off = Bukkit.getOfflinePlayer(mem);
                    String name = off.getName() != null ? off.getName() : mem.toString();
                    Component memberLine = miniMessage().deserialize(String.format(L4M4.get("info.member_line"), name));
                    infoComponent = infoComponent.append(memberLine);
                }
                infoComponent = infoComponent.append(miniMessage().deserialize(L4M4.get("info.footer")));
                player.sendMessage(infoComponent);
                return true;
            }

            // ========== KICK ==========
            case "kick" -> {
                if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.not_authorized_kick")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_kick")));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                    return true;
                }
                String targetName = args[1];
                OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
                if (targetOffline.getName() == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.player_not_found")));
                    return true;
                }
                UUID targetUUID = targetOffline.getUniqueId();
                if (!currentClan.isMember(targetUUID)) {
                    player.sendMessage(miniMessage().deserialize(String.format(L4M4.get("error.player_not_in_clan"), targetName)));
                    return true;
                }
                if (clans.isVice(playerUUID) && currentClan.isLeader(targetUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.cannot_kick_leader")));
                    return true;
                }
                if (currentClan.isVice(targetUUID)) {
                    currentClan.setViceUUID(null);
                }
                currentClan.getMembers().remove(targetUUID);
                currentClan.save();
                player.sendMessage(miniMessage().deserialize(String.format(L4M4.get("success.player_kicked"), targetName)));
                if (targetOffline.isOnline()) {
                    Player targetOnline = (Player) targetOffline;
                    targetOnline.sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("error.player_kicked_notify"), currentClan.getName())
                    ));
                }
                return true;
            }

            // ========== TRANSFER ==========
            case "transfer" -> {
                if (!clans.isLeader(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.only_leader_transfer")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_transfer")));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                    return true;
                }
                String newLeaderName = args[1];
                OfflinePlayer newLeaderOffline = Bukkit.getOfflinePlayer(newLeaderName);
                if (newLeaderOffline.getName() == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.player_not_found")));
                    return true;
                }
                UUID newLeaderUUID = newLeaderOffline.getUniqueId();
                if (!currentClan.isMember(newLeaderUUID)) {
                    player.sendMessage(miniMessage().deserialize(String.format(L4M4.get("error.player_not_in_clan"), newLeaderName)));
                    return true;
                }

                currentClan.setLeaderUUID(newLeaderUUID);
                if (currentClan.isVice(newLeaderUUID)) {
                    currentClan.setViceUUID(playerUUID);
                }
                currentClan.save();
                player.sendMessage(miniMessage().deserialize(String.format(L4M4.get("success.leadership_transferred"), newLeaderName)));
                if (newLeaderOffline.isOnline()) {
                    ((Player) newLeaderOffline).sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("success.new_leader_notify"), currentClan.getName())
                    ));
                }
                return true;
            }

            // ========== PROMOTE ==========
            case "promote" -> {
                if (!clans.isLeader(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.only_leader_promote")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_promote")));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                    return true;
                }
                String promoteName = args[1];
                OfflinePlayer promoteOffline = Bukkit.getOfflinePlayer(promoteName);
                if (promoteOffline.getName() == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.player_not_found")));
                    return true;
                }
                UUID promoteUUID = promoteOffline.getUniqueId();
                if (!currentClan.isMember(promoteUUID)) {
                    player.sendMessage(miniMessage().deserialize(String.format(L4M4.get("error.player_not_in_clan"), promoteName)));
                    return true;
                }
                if (currentClan.isVice(promoteUUID)) {
                    player.sendMessage(miniMessage().deserialize(String.format(L4M4.get("error.already_vice"), promoteName)));
                    return true;
                }
                currentClan.getMembers().remove(promoteUUID);
                currentClan.setViceUUID(promoteUUID);
                currentClan.save();
                player.sendMessage(miniMessage().deserialize(String.format(L4M4.get("success.promoted_to_vice"), promoteName)));
                if (promoteOffline.isOnline()) {
                    ((Player) promoteOffline).sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("success.promoted_notify"), currentClan.getName())
                    ));
                }
                return true;
            }

            // ========== DEMOTE ==========
            case "demote" -> {
                if (!clans.isLeader(playerUUID) && !clans.isVice(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.not_authorized_demote")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_demote")));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                    return true;
                }
                String demoteName = args[1];
                OfflinePlayer demoteOffline = Bukkit.getOfflinePlayer(demoteName);
                if (demoteOffline.getName() == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.player_not_found")));
                    return true;
                }
                UUID demoteUUID = demoteOffline.getUniqueId();
                if (!currentClan.isVice(demoteUUID)) {
                    player.sendMessage(miniMessage().deserialize(String.format(L4M4.get("error.not_vice"), demoteName)));
                    return true;
                }
                if (!clans.isLeader(playerUUID) && !playerUUID.equals(demoteUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.only_self_demote")));
                    return true;
                }
                currentClan.setViceUUID(null);
                if (!currentClan.getMembers().contains(demoteUUID)) {
                    currentClan.getMembers().add(demoteUUID);
                }
                currentClan.save();
                player.sendMessage(miniMessage().deserialize(String.format(L4M4.get("success.demoted"), demoteName)));
                if (demoteOffline.isOnline()) {
                    ((Player) demoteOffline).sendMessage(miniMessage().deserialize(
                            String.format(L4M4.get("error.demoted_notify"), currentClan.getName())
                    ));
                }
                return true;
            }

            // ========== DISBAND ==========
            case "disband" -> {
                if (!clans.isLeader(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.only_leader_disband")));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                    return true;
                }
                for (UUID memberUUID : currentClan.getMembers()) {
                    Player memberOnline = Bukkit.getPlayer(memberUUID);
                    if (memberOnline != null && memberOnline.isOnline()) {
                        memberOnline.sendMessage(miniMessage().deserialize(
                                String.format(L4M4.get("error.clan_disbanded_notify"), currentClan.getName())
                        ));
                    }
                }
                clans.removeClan(currentClan);
                player.sendMessage(miniMessage().deserialize(L4M4.get("success.clan_disbanded_leave")));
                return true;
            }

            // ========== LEAVE ==========
            case "leave" -> {
                if (!clans.isInClan(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.not_in_clan_info")));
                    return true;
                }
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                    return true;
                }
                if (currentClan.isLeader(playerUUID)) {
                    if (!currentClan.getMembers().isEmpty()) {
                        player.sendMessage(miniMessage().deserialize(L4M4.get("error.leader_cannot_leave")));
                    } else {
                        clans.removeClan(currentClan);
                        player.sendMessage(miniMessage().deserialize(L4M4.get("success.clan_disbanded_leave")));
                    }
                    return true;
                }
                if (currentClan.isVice(playerUUID)) {
                    currentClan.setViceUUID(null);
                }
                currentClan.getMembers().remove(playerUUID);
                currentClan.save();
                player.sendMessage(miniMessage().deserialize(
                        String.format(L4M4.get("success.left_clan"), currentClan.getName())
                ));
                return true;
            }

            // ========== MODIFY ==========
            case "modify" -> {
                if (!clans.isLeader(playerUUID)) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.only_leader_modify")));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("usage.clan_modify")));
                    return true;
                }
                String whatToModify = args[1].toLowerCase();
                String newValue = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                Clan currentClan = clans.getClanByMember(playerUUID);
                if (currentClan == null) {
                    player.sendMessage(miniMessage().deserialize(L4M4.get("error.clan_not_found")));
                    return true;
                }
                switch (whatToModify) {
                    case "name" -> {
                        currentClan.setName(newValue);
                        currentClan.save();
                        player.sendMessage(miniMessage().deserialize(
                                String.format("<green>Clan name changed to %s.</green>", newValue)
                        ));
                    }
                    case "tag" -> {
                        Component newTag = miniMessage().deserialize(newValue);
                        currentClan.setTag(newTag);
                        currentClan.save();
                        player.sendMessage(miniMessage().deserialize(
                                String.format("<green>Clan tag changed to %s.</green>", newValue)
                        ));
                    }
                    default -> player.sendMessage(miniMessage().deserialize(L4M4.get("error.modify_invalid")));
                }
                return true;
            }

            // ========== Fallback ==========
            default -> {
                player.sendMessage(miniMessage().deserialize(L4M4.get("error.unknown_subcommand")));
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command cmd,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        UUID playerUUID = player.getUniqueId();

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
                        String viceName = Optional.ofNullable(Bukkit.getOfflinePlayer(clan.getViceUUID()).getName())
                                .orElse(clan.getViceUUID().toString());
                        return viceName.startsWith(args[1]) ? List.of(viceName) : List.of();
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
                        String viceName = Optional.ofNullable(Bukkit.getOfflinePlayer(clan.getViceUUID()).getName())
                                .orElse(clan.getViceUUID().toString());
                        return viceName.startsWith(args[1]) ? List.of(viceName) : List.of();
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
                        return clanInvites.entrySet().stream()
                                .filter(entry -> entry.getValue().contains(playerUUID))
                                .map(entry -> entry.getKey().getName())
                                .filter(name -> name.startsWith(args[1]))
                                .collect(Collectors.toList());
                    }
                    case "join" -> {
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

        if (args.length == 3) {
            if (clans.isLeader(playerUUID) && args[0].equalsIgnoreCase("modify")) {
                if (args[1].equalsIgnoreCase("name")) {
                    return Collections.singletonList("<newName>");
                } else if (args[1].equalsIgnoreCase("tag")) {
                    return Collections.singletonList("<newTag>");
                }
            } else if (args[0].equalsIgnoreCase("create")) {
                return Collections.singletonList("<tag>");
            }
        }
        return List.of();
    }

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

    private void sendHelpMessage(Player player, UUID playerUUID) {
        if (clans.isLeader(playerUUID)) {
            player.sendMessage(miniMessage().deserialize(L4M4.get("help.leader")));
        } else if (clans.isVice(playerUUID)) {
            player.sendMessage(miniMessage().deserialize(L4M4.get("help.vice")));
        } else if (clans.isMember(playerUUID)) {
            player.sendMessage(miniMessage().deserialize(L4M4.get("help.member")));
        } else {
            player.sendMessage(miniMessage().deserialize(L4M4.get("help.none")));
        }
    }
}
