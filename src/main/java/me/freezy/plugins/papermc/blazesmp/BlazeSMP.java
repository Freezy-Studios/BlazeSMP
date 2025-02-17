package me.freezy.plugins.papermc.blazesmp;

import lombok.Getter;
import me.freezy.plugins.papermc.blazesmp.command.ClanCommand;
import me.freezy.plugins.papermc.blazesmp.listener.PlayerChatListener;
import me.freezy.plugins.papermc.blazesmp.listener.PlayerCommandBlockerListener;
import me.freezy.plugins.papermc.blazesmp.listener.PlayerJoinListener;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import me.freezy.plugins.papermc.blazesmp.module.manager.Homes;
import me.freezy.plugins.papermc.blazesmp.module.manager.ProtectedBlocks;
import me.freezy.plugins.papermc.blazesmp.tasks.PlayerNameUpdate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

public final class BlazeSMP extends JavaPlugin {
    @Getter private static BlazeSMP instance;
    @Getter private Homes homes;
    @Getter private ProtectedBlocks protectedBlocks;
    @Getter private Clans clans;
    @Getter private FileConfiguration configuration;
    @Getter private Logger log;
    @Getter private BukkitTask nameUpdateTask;

    @Override
    public void onLoad() {
        this.log=getSLF4JLogger();

        this.log.info("Loading BlazeSMP...");

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

        this.log.info("Loaded BlazeSMP!");
    }

    @Override
    public void onEnable() {
        BlazeSMP.instance=this;

        this.log.info("Enabling BlazeSMP...");

        this.log.info("Registering Commands...");
        new ClanCommand().register();
        this.log.info("Registered Commands!");

        this.log.info("Registering EventListeners...");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new PlayerChatListener(), this);
        pm.registerEvents(new PlayerCommandBlockerListener(), this);
        this.log.info("Registered EventListeners!");

        this.log.info("Starting Timer tasks...");
        this.nameUpdateTask = new PlayerNameUpdate().runTaskTimer(this, 0L, 20L);
        this.log.info("Started Timer tasks!");

        this.log.info("Enabled BlazeSMP!");
    }

    @Override
    public void onDisable() {
        this.log.info("Disabling BlazeSMP...");

        this.log.info("Cancelling Timer tasks...");
        this.nameUpdateTask.cancel();
        this.log.info("Cancelled Timer tasks!");

        this.log.info("Saving Homes...");
        this.homes.save();
        this.log.info("Saved Homes!");

        this.log.info("Saving ProtectedBlocks...");
        this.protectedBlocks.save();
        this.log.info("Saved ProtectedBlocks!");

        this.log.info("Saving Clans...");
        this.clans.saveAllClans();
        this.log.info("Saved Clans!");

        this.log.info("Disabling BlazeSMP!");
    }
}
