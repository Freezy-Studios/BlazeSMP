package me.freezy.plugins.papermc.blazesmp;

import lombok.Getter;
import me.freezy.plugins.papermc.blazesmp.command.ClanCommand;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import me.freezy.plugins.papermc.blazesmp.module.manager.Homes;
import me.freezy.plugins.papermc.blazesmp.module.manager.ProtectedBlocks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public final class BlazeSMP extends JavaPlugin {
    @Getter private static BlazeSMP instance;
    @Getter private Homes homes;
    @Getter private ProtectedBlocks protectedBlocks;
    @Getter private Clans clans;
    @Getter private FileConfiguration configuration;
    @Getter private Logger log;

    @Override
    public void onLoad() {
        this.log=getSLF4JLogger();

        this.log.info("Loading Homes...");
        this.homes=new Homes();
        this.homes.load();
        this.log.info("Loaded Homes!");

        this.log.info("Loading ProtectedBlocks...");
        this.protectedBlocks=new ProtectedBlocks();
        this.protectedBlocks.load();
        this.log.info("Loaded ProtectedBlocks!");

        this.log.info("Loading Clans...");
        this.clans=new Clans();
        this.clans.loadAllClans();
        this.log.info("Loaded Clans!");

        this.log.info("Loading config...");
        saveDefaultConfig();
        this.configuration= getConfig();
        saveConfig();
        this.log.info("Loaded config!");
    }

    @Override
    public void onEnable() {
        BlazeSMP.instance=this;

        this.log.info("Registering Commands...");
        new ClanCommand().register();
        this.log.info("Registered Commands!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
