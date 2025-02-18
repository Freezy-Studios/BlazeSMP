package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimCommand extends SimpleCommand {

    private final Clans clans;

    public ClaimCommand() {
        super("claim", List.of("unclaim"));
        this.clans = BlazeSMP.getInstance().getClans();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be a player to use this command!"));
            return true;
        }
        UUID playerUUID = player.getUniqueId();
        if (!clans.isInClan(playerUUID)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be in a clan to claim/unclaim chunks!"));
            return true;
        } else {
            if (label.equalsIgnoreCase("claim")) {
                Clan playerClan = clans.getClanByMember(playerUUID);
                LinkedHashMap<UUID, LinkedList<Chunk>> existingClaims=clans.getClanChunks(playerClan);
                if (existingClaims.containsKey(playerUUID)) {
                    LinkedList<Chunk> playerClaims = existingClaims.get(playerUUID);
                    int MAX_CLAIMS = 50;
                    if (playerClaims.size() >= MAX_CLAIMS) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You have reached the maximum amount of claims!"));
                    } else {
                        Chunk playerChunk = player.getLocation().getChunk();
                        playerClaims.add(playerChunk);
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Claimed chunk!"));
                        existingClaims.put(playerUUID, playerClaims);
                        clans.setClanChunks(playerClan, existingClaims);
                    }
                    return true;
                }
            } else if (label.equalsIgnoreCase("unclaim")) {
                Clan playerClan = clans.getClanByMember(playerUUID);
                LinkedHashMap<UUID, LinkedList<Chunk>> existingClaims=clans.getClanChunks(playerClan);
                if (existingClaims.containsKey(playerUUID)) {
                    LinkedList<Chunk> playerClaims = existingClaims.get(playerUUID);
                    Chunk playerChunk = player.getLocation().getChunk();
                    if (playerClaims.contains(playerChunk)) {
                        playerClaims.remove(playerChunk);
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Unclaimed chunk!"));
                        existingClaims.put(playerUUID, playerClaims);
                        clans.setClanChunks(playerClan, existingClaims);
                    } else {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not own this chunk!"));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
