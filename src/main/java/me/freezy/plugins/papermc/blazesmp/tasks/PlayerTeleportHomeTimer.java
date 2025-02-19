package me.freezy.plugins.papermc.blazesmp.tasks;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
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
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Teleporting cancelt you moved!</red>"));
            cancel();
            return;
        }

        // Berechne, wie viele Ticks bereits vergangen sind
        int ticksElapsed = (5 * 20) - ticksRemaining;

        // Erzeuge den Spiraleneffekt
        Location baseLoc = player.getLocation();
        double radius = 1.5;
        double offsetX = Math.cos(angle) * radius;
        double offsetZ = Math.sin(angle) * radius;
        // Inkrementiere den Y-Wert, sodass die Spirale nach oben steigt
        double offsetY = 0.2 + ticksElapsed * 0.05;
        Location particleLoc = baseLoc.clone().add(offsetX, offsetY, offsetZ);
        player.getWorld().spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);

        // Erhöhe den Winkel schneller, sodass sich die Spirale schneller dreht
        double angleIncrement = Math.PI / 4;
        angle += angleIncrement;

        // Teleport-Countdown und Nachrichten
        if (ticksRemaining <= 0) {
            player.teleportAsync(BlazeSMP.getInstance().getHomes().getHome(player));
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Teleported!</green>"));
            cancel();
        } else if (ticksRemaining % 20 == 0) {
            int secondsLeft = ticksRemaining / 20;
            String message = String.format("<yellow>Teleporting to home in %s seconds!</yellow>", secondsLeft);
            player.sendMessage(MiniMessage.miniMessage().deserialize(message));
        }

        ticksRemaining--;
    }
}
