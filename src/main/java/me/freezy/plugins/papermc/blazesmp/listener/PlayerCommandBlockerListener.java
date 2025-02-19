package me.freezy.plugins.papermc.blazesmp.listener;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class PlayerCommandBlockerListener implements Listener {
    private final Set<String> blockedCommands = new LinkedHashSet<>(Arrays.asList(
            "/bukkit:?",
            "/?",
            "/bukkit:about",
            "/about",
            "/bukkit:help",
            "/help",
            "/bukkit:pl",
            "/pl",
            "/bukkit:plugins",
            "/plugins",
            "/bukkit:reload",
            "/reload",
            "/bukkit:rl",
            "/rl",
            "/bukkit:timings",
            "/timings",
            "/bukkit:ver",
            "/ver",
            "/bukkit:version",
            "/version",
            "/paper:callback",
            "/callback",
            "/paper:mspt",
            "/mspt",
            "/paper:paper",
            "/paper",
            "/paper:spark",
            "/spark",
            "/minecraft:advancement",
            "/advancement",
            "/minecraft:attribute",
            "/attribute",
            "/minecraft:ban",
            "/ban",
            "/minecraft:ban-ip",
            "/ban-ip",
            "/minecraft:banlist",
            "/banlist",
            "/minecraft:bossbar",
            "/bossbar",
            "/minecraft:clear",
            "/clear",
            "/minecraft:clone",
            "/clone",
            "/minecraft:damage",
            "/damage",
            "/minecraft:data",
            "/data",
            "/minecraft:datapack",
            "/datapack",
            "/minecraft:difficulty",
            "/difficulty",
            "/minecraft:effect",
            "/effect",
            "/minecraft:enchant",
            "/enchant",
            "/minecraft:execute",
            "/execute",
            "/minecraft:experience",
            "/experience",
            "/minecraft:fill",
            "/fill",
            "/minecraft:fillbiome",
            "/fillbiome",
            "/minecraft:foreload",
            "/foreload",
            "/minecraft:gamerule",
            "/gamerule",
            "/minecraft:give",
            "/give",
            "/minecraft:help",
            "/help",
            "/minecraft:item",
            "/item",
            "/minecraft:jfr",
            "/jfr",
            "/minecraft:kick",
            "/kick",
            "/minecraft:kill",
            "/kill",
            "/minecraft:list",
            "/list",
            "/minecraft:locate",
            "/locate",
            "/minecraft:loot",
            "/loot",
            "/minecraft:me",
            "/me",
            "/minecraft:op",
            "/op",
            "/minecraft:pardon",
            "/pardon",
            "/minecraft:pardon-ip",
            "/pardon-ip",
            "/minecraft:particle",
            "/particle",
            "/minecraft:perf",
            "/perf",
            "/minecraft:place",
            "/place",
            "/minecraft:playsound",
            "/playsound",
            "/minecraft:random",
            "/random",
            "/minecraft:recipe",
            "/recipe",
            "/minecraft:reload",
            "/reload",
            "/minecraft:ride",
            "/ride",
            "/minecraft:rotate",
            "/rotate",
            "/minecraft:save-all",
            "/save-all",
            "/minecraft:save-off",
            "/save-off",
            "/minecraft:save-on",
            "/save-on",
            "/minecraft:say",
            "/say",
            "/minecraft:schedule",
            "/schedule",
            "/minecraft:scoreboard",
            "/scoreboard",
            "/minecraft:seed",
            "/seed",
            "/minecraft:setblock",
            "/setblock",
            "/minecraft:setidletimeout",
            "/setidletimeout",
            "/minecraft:setworldspawn",
            "/setworldspawn",
            "/minecraft:spawnpoint",
            "/spawnpoint",
            "/minecraft:spectate",
            "/spectate",
            "/minecraft:tag",
            "/tag",
            "/minecraft:team",
            "/team",
            "/minecraft:teammsg",
            "/teammsg",
            "/minecraft:teleport",
            "/teleport",
            "/minecraft:tellraw",
            "/tellraw",
            "/minecraft:tick",
            "/tick",
            "/minecraft:time",
            "/time",
            "/minecraft:title",
            "/title",
            "/minecraft:tm",
            "/tm",
            "/minecraft:tp",
            "/tp",
            "/minecraft:transfer",
            "/transfer",
            "/minecraft:trigger",
            "/trigger",
            "/minecraft:weather",
            "/weather",
            "/minecraft:whitelist",
            "/whitelist",
            "/minecraft:worldborder",
            "/worldborder",
            "/minecraft:xp",
            "/xp",
            "/icanhasbukkit"
    ));

    private boolean isBlocked(String input) {
        String normalized = input.toLowerCase().trim();
        for (String blocked : blockedCommands) {
            blocked = blocked.toLowerCase().trim();
            if (normalized.equals(blocked) || normalized.startsWith(blocked + " ")) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return; // OPs werden nicht blockiert

        String message = event.getMessage();
        if (isBlocked(message)) {
            event.setCancelled(true);
            String blockedPart = message.split(" ")[0];
            // Nutze den zentralen Nachrichtentext aus messages.json
            String msg = String.format(L4M4.get("command.blocked"), blockedPart);
            player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
        }
    }

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent event) {
        Player player = (Player) event.getSender();
        if (player.isOp()) return; // OPs erhalten weiterhin Tab-Vorschläge

        String buffer = event.getBuffer();
        if (isBlocked(buffer)) {
            event.getCompletions().clear();
            event.setCancelled(true);
        }
    }
}
