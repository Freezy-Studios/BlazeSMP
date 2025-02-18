package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
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

        if (oldClan != null && (!oldClan.equals(newClan))) {
            player.sendActionBar(
                    MiniMessage.miniMessage().deserialize(
                            "<red>You left the claim of <white>" + oldClan.getName() + "</white>!</red>"
                    )
            );
        }

        if (newClan != null && (!newClan.equals(oldClan))) {
            player.sendActionBar(
                    MiniMessage.miniMessage().deserialize(
                            "<red>Terretorry of <white>" + newClan.getName() + "</white> - <white>" +
                                    Bukkit.getOfflinePlayer(newClan.getChunkOwnerMap().get(toChunk)).getName() +
                                    "!</white></red>"
                    )
            );
        }
    }
}
