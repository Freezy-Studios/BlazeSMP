package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PressurePlateListener implements Listener {
    private static final Plugin plugin = JavaPlugin.getPlugin(BlazeSMP.class);
    private final Location pressurePlateLocation;
    private final Location spawnLocation;
    private final Map<UUID, BukkitRunnable> playerTasks = new HashMap<>();
    private final String teleportMessage;
    private final long teleportDelay;

    public PressurePlateListener() {
        FileConfiguration config = plugin.getConfig();
        pressurePlateLocation = new Location(
                Bukkit.getWorld(config.getString("pressure-plate.world", "world")),
                config.getDouble("pressure-plate.x", 1),
                config.getDouble("pressure-plate.y", 68),
                config.getDouble("pressure-plate.z", 0)
        );
        spawnLocation = new Location(
                Bukkit.getWorld(config.getString("spawn-location.world", "world")),
                config.getDouble("spawn-location.x", 0),
                config.getDouble("spawn-location.y", 200),
                config.getDouble("spawn-location.z", 0)
        );
        teleportMessage = L4M4.get("pressureplate.teleport");
        teleportDelay = 5 * 20L;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getBlock().getLocation().equals(pressurePlateLocation)) {
            if (!playerTasks.containsKey(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(teleportMessage);
                BukkitRunnable task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().teleport(spawnLocation);
                        playerTasks.remove(event.getPlayer().getUniqueId());
                    }
                };
                task.runTaskLater(plugin, teleportDelay);
                playerTasks.put(event.getPlayer().getUniqueId(), task);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (playerTasks.containsKey(event.getPlayer().getUniqueId())) {
                            event.getPlayer().getWorld().spawnParticle(Particle.PORTAL, event.getPlayer().getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
                        } else {
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 10);
            }
        } else {
            if (playerTasks.containsKey(event.getPlayer().getUniqueId())) {
                if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                    playerTasks.get(event.getPlayer().getUniqueId()).cancel();
                    playerTasks.remove(event.getPlayer().getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getLocation().equals(pressurePlateLocation)
                && event.getBlock().getType() == Material.POLISHED_BLACKSTONE_PRESSURE_PLATE) {
            event.setCancelled(true);
        }
    }
}