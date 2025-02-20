package me.freezy.plugins.papermc.blazesmp;

import lombok.Getter;
import me.freezy.plugins.papermc.blazesmp.command.*;
import me.freezy.plugins.papermc.blazesmp.listener.*;
import me.freezy.plugins.papermc.blazesmp.module.manager.Clans;
import me.freezy.plugins.papermc.blazesmp.module.manager.Homes;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import me.freezy.plugins.papermc.blazesmp.module.manager.ProtectedBlocks;
import me.freezy.plugins.papermc.blazesmp.tasks.PlayerNameUpdate;
import me.freezy.plugins.papermc.blazesmp.tasks.TabListTimer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.slf4j.Logger;

public final class BlazeSMP extends JavaPlugin {
    @Getter private static BlazeSMP instance;
    @Getter private Homes homes;
    @Getter private ProtectedBlocks protectedBlocks;
    @Getter private Clans clans;
    @Getter private FileConfiguration configuration;
    @Getter private Logger log;
    @Getter private BukkitTask nameUpdateTask;
    @Getter private BukkitTask tabListUpdateTask;

    @Override
    public void onLoad() {
        this.log=getSLF4JLogger();

        this.log.info("Loading BlazeSMP...");

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

        this.log.info("Loading L4M4...");
        L4M4.init();
        this.log.info("Loaded L4M4!");

        this.log.info("Loaded BlazeSMP!");
    }

    @Override
    public void onEnable() {
        BlazeSMP.instance=this;

        this.log.info("Enabling BlazeSMP...");

        this.log.info("Loading Homes...");
        this.homes=new Homes();
        this.homes.load();
        this.log.info("Loaded Homes!");

        this.log.info("Registering Commands...");
        new ClanCommand().register();
        new ReportCommand().register();
        new ClaimCommand().register();
        new HomeCommand().register();
        new DiscordCommand().register();
        this.log.info("Registered Commands!");

        this.log.info("Registering EventListeners...");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new PlayerChatListener(), this);
        pm.registerEvents(new PlayerCommandBlockerListener(), this);
        pm.registerEvents(new PlayerClaimListener(), this);
        pm.registerEvents(new ChunkInventoryListener(), this);
        pm.registerEvents(new PressurePlateListener(), this);
        pm.registerEvents(new PlayerVsPlayerListener(clans), this);
        //pm.registerEvents(new ProtectedBlockListener(), this);
        this.log.info("Registered EventListeners!");

        this.log.info("Starting Timer tasks...");
        this.nameUpdateTask = new PlayerNameUpdate().runTaskTimer(this, 0L, 20L);
        this.tabListUpdateTask = new TabListTimer().runTaskTimer(this, 0L, 20L);
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

        this.log.info("Clearing Teams...");
        getServer().getScoreboardManager().getMainScoreboard().getTeams().forEach(Team::unregister);
        this.log.info("Cleared Teams!");

        this.log.info("Disabling BlazeSMP!");
    }
}
