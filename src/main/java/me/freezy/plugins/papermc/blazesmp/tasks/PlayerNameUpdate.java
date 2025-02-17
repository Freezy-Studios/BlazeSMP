package me.freezy.plugins.papermc.blazesmp.tasks;

import me.freezy.plugins.papermc.blazesmp.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerNameUpdate extends BukkitRunnable {
    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            new PlayerManager().setPlayerTeam(player);
        });
    }
}
