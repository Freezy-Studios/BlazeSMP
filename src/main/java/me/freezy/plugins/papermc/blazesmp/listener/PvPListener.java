package me.freezy.plugins.papermc.blazesmp.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.time.LocalDateTime;
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

            if (now.getYear() == 2025 && now.getMonth() == Month.FEBRUARY && now.getDayOfMonth() == 23 &&
                now.getHour() >= 17 && (now.getHour() < 20 || (now.getHour() == 20 && now.getMinute() == 0))) {
                event.setCancelled(true);
            }
        }
    }
}