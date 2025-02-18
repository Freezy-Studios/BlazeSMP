package me.freezy.plugins.papermc.blazesmp.manager;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.UUID;

public class PlayerManager {
    private final LinkedHashMap<Clan, String> clanChars = new LinkedHashMap<>();
    {
        Clans clans = BlazeSMP.getInstance().getClans();
        LinkedList<Clan> clansList = clans.getClans();
        clansList.forEach(clan -> {
            int clanPlace = clansList.indexOf(clan);
            char first = (char) ('a' + (clanPlace / (26 * 26)) % 26);
            char second = (char) ('a' + (clanPlace / 26) % 26);
            char third = (char) ('a' + clanPlace % 26);
            clanChars.put(clan, ""+first+second+third);
        });
    }
    public void setPlayerTeam(Player player) {
        Clans clans = BlazeSMP.getInstance().getClans();
        UUID playerUUID = player.getUniqueId();
        player.setScoreboard(player.getServer().getScoreboardManager().getMainScoreboard());
        Scoreboard scoreboard = player.getScoreboard();
        String teamName;
        if (clans.isInClan(playerUUID)) {
            teamName=clanChars.get(clans.getClanByMember(playerUUID));
            if (clans.isLeader(playerUUID)) {
                teamName+="a";
            } else if (clans.isVice(playerUUID)) {
                teamName+="b";
            } else {
                teamName+="c";
            }
        } else {
            teamName="zzzm";
        }
        teamName+=generateRandomString();
        scoreboard.getTeams().forEach(Team::unregister);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        Component prefix = player.isOp() ?
                MiniMessage.miniMessage().deserialize(
                        BlazeSMP.getInstance().getConfiguration().getString("op-prefix",
                                "<color:dark_gray>[</color><gradient:#ffa600:#ffb700><b>Team</b></gradient><color:dark_gray>]</color> "
                        )
                )
                :
                MiniMessage.miniMessage().deserialize(
                        BlazeSMP.getInstance().getConfiguration().getString("player-prefix",
                                "<color:dark_gray>[</color><gradient:#747e80:#828d8f><b>Player</b></gradient><color:dark_gray>]</color> "
                        )
                );
        if (clans.isInClan(playerUUID)) {
            if (clans.isLeader(playerUUID)) {
                prefix = prefix.append(MiniMessage.miniMessage().deserialize("<color:red>*</color>"));
            } else if (clans.isVice(playerUUID)) {
                prefix = prefix.append(MiniMessage.miniMessage().deserialize("<color:light_purple>*</color>"));
            }
        }
        team.prefix(prefix);
        Clan clan = clans.getClanByMember(playerUUID);
        Component suffix;
        if (clan != null) {
            suffix = (Component.text(" ").append(clan.getTag()));
        } else {
            suffix = (Component.empty());
        }
        team.suffix();
        team.addEntity(player);
        team.addEntry(player.getName());

        Component displayName = prefix.append(Component.text(player.getName())).append(suffix);
        player.playerListName(displayName);
        player.displayName(displayName);
    }

    private String generateRandomString() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }
}
