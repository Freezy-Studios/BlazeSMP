package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerVsPlayerListener implements Listener {

    private final Clans clanManager;

    public PlayerVsPlayerListener(Clans clanManager) {
        this.clanManager = clanManager;
    }

    @EventHandler
    public void onPVP(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player victim) {

            Clan damagerClan = clanManager.getClanByMember(damager.getUniqueId());
            Clan victimClan = clanManager.getClanByMember(victim.getUniqueId());

            if (damagerClan != null && damagerClan.equals(victimClan)) {
                event.setCancelled(true);
            }
        }
    }
}