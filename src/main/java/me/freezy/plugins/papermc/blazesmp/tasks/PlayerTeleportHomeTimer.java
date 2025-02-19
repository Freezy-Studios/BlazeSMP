package me.freezy.plugins.papermc.blazesmp.tasks;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerTeleportHomeTimer extends BukkitRunnable {
    private final Player player;
    private final double origX;
    private final double origY;
    private final double origZ;
    // Countdown in Ticks (5 Sekunden * 20 Ticks pro Sekunde)
    private int ticksRemaining = 5 * 20;

    // Felder für den Spiraleneffekt
    private double angle = 0;
    private double yOffset = 0;

    public PlayerTeleportHomeTimer(Player player) {
        this.player = player;
        // Speichere die ursprüngliche Position, um Bewegungen zu erkennen
        origX = player.getLocation().getX();
        origY = player.getLocation().getY();
        origZ = player.getLocation().getZ();
    }

    @Override
    public void run() {
        // Abbruch, falls sich der Spieler bewegt hat
        if (player.getLocation().getX() != origX ||
                player.getLocation().getY() != origY ||
                player.getLocation().getZ() != origZ) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("teleport.cancelled")));
            cancel();
            return;
        }

        Location baseLoc = player.getLocation();
        double radius = 1.5;

        // Schleife für einen schnelleren Effekt: Mehrere Partikel pro Tick
        // Basis-Inkrement des Winkels
        double angleIncrement = Math.PI / 4;
        for (int i = 0; i < 3; i++) {
            double currentAngle = angle + (i * angleIncrement);
            double offsetX = Math.cos(currentAngle) * radius;
            double offsetZ = Math.sin(currentAngle) * radius;
            // Verwende yOffset für die vertikale Verschiebung und setze zurück, wenn er über 2 liegt
            double currentYOffset = yOffset;
            if (currentYOffset > 2) {
                currentYOffset = 0;
            }
            Location particleLoc = baseLoc.clone().add(offsetX, currentYOffset, offsetZ);
            player.getWorld().spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);
        }

        // Erhöhe den globalen Winkel und yOffset für den nächsten Tick
        angle += angleIncrement * 3; // da 3 Partikel pro Tick
        yOffset += 0.05 * 3;
        if (yOffset > 2) {
            yOffset = 0;
        }

        // Teleport-Countdown und Nachrichten
        if (ticksRemaining <= 0) {
            player.teleportAsync(BlazeSMP.getInstance().getHomes().getHome(player));
            player.sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("teleport.success")));
            cancel();
        } else if (ticksRemaining % 20 == 0) {
            int secondsLeft = ticksRemaining / 20;
            String message = String.format(L4M4.get("teleport.countdown"), secondsLeft);
            player.sendMessage(MiniMessage.miniMessage().deserialize(message));
        }

        ticksRemaining--;
    }
}
