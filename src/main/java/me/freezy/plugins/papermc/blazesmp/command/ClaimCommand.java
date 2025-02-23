package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.listener.ChunkInventoryListener;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ClaimCommand extends SimpleCommand {

    private final Clans clans;

    public ClaimCommand() {
        super("claim", List.of("unclaim"));
        this.clans = BlazeSMP.getInstance().getClans();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.not_a_player")));
            return true;
        }
        UUID playerUUID = player.getUniqueId();
        if (!clans.isInClan(playerUUID)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.not_in_clan")));
            return true;
        } else {
            if (label.equalsIgnoreCase("claim")) {
                if (args.length != 0 && args[0].equalsIgnoreCase("see")) {
                    ChunkInventoryListener.openInv(player);
                    return true;
                }
                Clan playerClan = clans.getClanByMember(playerUUID);
                LinkedHashMap<UUID, LinkedList<Chunk>> existingClaims = clans.getClanChunks(playerClan);
                if (!existingClaims.containsKey(playerUUID)) {
                    existingClaims.put(playerUUID, new LinkedList<>());
                }
                LinkedList<Chunk> playerClaims = existingClaims.get(playerUUID);
                int MAX_CLAIMS = 50;
                if (playerClaims.size() >= MAX_CLAIMS) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.max_claims_reached")));
                    System.out.println("Claim denied: max claims reached");
                } else {
                    Chunk playerChunk = player.getLocation().getChunk();
                    if (clans.isChunkClaimed(playerChunk)) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.chunk_already_claimed")));
                        System.out.println("Claim denied: chunk already claimed");
                    } else {
                        // claim too close to spawn 8 chunks
                        // claim too close to spawn 152 blocks in all directions
                        if (Math.abs(player.getX()) < 152.0 && Math.abs(player.getZ()) < 152.0) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.chunk_too_close_to_spawn")));
                            System.out.println("Claim denied: chunk too close to spawn");
                            return true;
                        }
                        playerClaims.add(playerChunk);
                        player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("success.chunk_claimed")));
                        existingClaims.put(playerUUID, playerClaims);
                        clans.setClanChunks(playerClan, existingClaims);
                        playerClan.save();
                        clans.saveAllClans();
                        System.out.println("Chunk claimed successfully");
                    }
                }
                return true;
            } else if (label.equalsIgnoreCase("unclaim")) {
                Clan playerClan = clans.getClanByMember(playerUUID);
                LinkedHashMap<UUID, LinkedList<Chunk>> existingClaims = clans.getClanChunks(playerClan);
                if (existingClaims.containsKey(playerUUID)) {
                    LinkedList<Chunk> playerClaims = existingClaims.get(playerUUID);
                    Chunk playerChunk = player.getLocation().getChunk();
                    if (playerClaims.contains(playerChunk)) {
                        playerClaims.remove(playerChunk);
                        player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("success.chunk_unclaimed")));
                        existingClaims.put(playerUUID, playerClaims);
                        clans.setClanChunks(playerClan, existingClaims);
                        playerClan.save();
                        clans.saveAllClans();
                        System.out.println("Chunk unclaimed successfully");
                    } else {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.chunk_not_owned")));
                        System.out.println("Unclaim denied: chunk not owned");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("see");
        }
        return List.of();
    }
}