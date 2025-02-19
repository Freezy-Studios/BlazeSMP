package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerClaimListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        Chunk chunk = location.getChunk();
        Clans clans = BlazeSMP.getInstance().getClans();
        Clan clan = clans.getClanByChunk(chunk);
        if (clan != null) {
            if (clans.isInClan(event.getPlayer().getUniqueId(), clan)) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();
        Chunk chunk = location.getChunk();
        Clans clans = BlazeSMP.getInstance().getClans();
        Clan clan = clans.getClanByChunk(chunk);
        if (clan != null) {
            if (clans.isInClan(event.getPlayer().getUniqueId(), clan)) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Location location = event.getPlayer().getLocation();
        Chunk chunk = location.getChunk();
        Clans clans = BlazeSMP.getInstance().getClans();
        Clan clan = clans.getClanByChunk(chunk);
        if (clan != null) {
            if (clans.isInClan(event.getPlayer().getUniqueId(), clan)) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Location location = event.getPlayer().getLocation();
        Chunk chunk = location.getChunk();
        Clans clans = BlazeSMP.getInstance().getClans();
        Clan clan = clans.getClanByChunk(chunk);
        if (clan != null) {
            if (clans.isInClan(event.getPlayer().getUniqueId(), clan)) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Location location = event.getPlayer().getLocation();
        Chunk chunk = location.getChunk();
        Clans clans = BlazeSMP.getInstance().getClans();
        Clan clan = clans.getClanByChunk(chunk);
        if (clan != null) {
            if (clans.isInClan(event.getPlayer().getUniqueId(), clan)) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEnterClanClaim(PlayerMoveEvent event) {
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        if (fromChunk.equals(toChunk)) {
            return;
        }

        Player player = event.getPlayer();
        Clans clans = BlazeSMP.getInstance().getClans();

        Clan oldClan = clans.getClanByChunk(fromChunk);
        Clan newClan = clans.getClanByChunk(toChunk);

        // Falls der Spieler den Claim wechselt (verlassen)
        if (oldClan != null && newClan != null && !oldClan.equals(newClan)) {
            String msg = String.format(
                    L4M4.get("claim.entered"),
                    newClan.getName()
            );
            player.sendActionBar(MiniMessage.miniMessage().deserialize(msg));
        }

        // Falls der Spieler in einen neuen Claim eintritt
        if (newClan != null && !newClan.equals(oldClan)) {
            String ownerName = Bukkit.getOfflinePlayer(newClan.getChunkOwnerMap().get(toChunk)).getName();
            String msg = String.format(
                    L4M4.get("claim.territory"),
                    newClan.getName(),
                    ownerName
            );
            player.sendActionBar(MiniMessage.miniMessage().deserialize(msg));
        }
    }
}
