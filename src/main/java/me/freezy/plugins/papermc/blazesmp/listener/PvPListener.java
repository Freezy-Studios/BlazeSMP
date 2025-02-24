package me.freezy.plugins.papermc.blazesmp.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PvPListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
            String formattedNow = now.format(formatter);
            // Optional: Logge oder benutze formattedNow, falls benötigt

            if (now.getYear() == 2025 && now.getMonth() == Month.FEBRUARY && now.getDayOfMonth() == 23) {
                LocalTime currentTime = now.toLocalTime();
                LocalTime startTime = LocalTime.of(16, 30);
                LocalTime endTime = LocalTime.of(19, 0);
                // Prüft, ob die aktuelle Uhrzeit zwischen 17:30 und 20:00 liegt (inklusive beider Grenzen)
                if (!currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
