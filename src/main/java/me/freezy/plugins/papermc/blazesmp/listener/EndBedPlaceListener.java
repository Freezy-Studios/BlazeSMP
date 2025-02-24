package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Set;

public class EndBedPlaceListener implements Listener {
    private final Set<Material> beds = Set.of(
            Material.BLACK_BED,
            Material.BLUE_BED,
            Material.BROWN_BED,
            Material.CYAN_BED,
            Material.GRAY_BED,
            Material.GREEN_BED,
            Material.LIGHT_BLUE_BED,
            Material.LIGHT_GRAY_BED,
            Material.LIME_BED,
            Material.MAGENTA_BED,
            Material.ORANGE_BED,
            Material.PINK_BED,
            Material.PURPLE_BED,
            Material.RED_BED,
            Material.WHITE_BED,
            Material.YELLOW_BED
    );

    @EventHandler
    public void onBedPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (beds.contains(block.getType()) && event.getPlayer().getWorld().getName().equalsIgnoreCase("world_the_end")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(L4M4.get("error.bed_place_in_end")));
        }
    }
}
