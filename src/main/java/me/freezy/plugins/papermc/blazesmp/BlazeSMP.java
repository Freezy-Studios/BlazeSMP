package me.freezy.plugins.papermc.blazesmp;

import lombok.Getter;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import me.freezy.plugins.papermc.blazesmp.module.manager.Homes;
import me.freezy.plugins.papermc.blazesmp.module.manager.ProtectedBlocks;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlazeSMP extends JavaPlugin {
    @Getter private static BlazeSMP instance;
    @Getter private Homes homes;
    @Getter private ProtectedBlocks protectedBlocks;
    @Getter private Clans clans;

    @Override
    public void onLoad() {
        this.homes=new Homes();
        this.homes.load();
        this.protectedBlocks=new ProtectedBlocks();
        this.protectedBlocks.load();
        this.clans=new Clans();
        this.clans.loadAllClans();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
