package me.freezy.plugins.papermc.blazesmp.tasks;

import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;

public class TabListTimer extends BukkitRunnable {
    private LinkedList<String> header = new LinkedList<>();
    private LinkedList<String> footer = new LinkedList<>();
    private int index = 0;

    public TabListTimer() {
        reloadData();
    }

    /**
     * Lädt die Header- und Footer-Daten neu.
     * Falls eine Liste leer ist, wird ein Standardwert gesetzt, um Fehler zu vermeiden.
     */
    private void reloadData() {
        header = L4M4.getStringList("tablist.header");
        footer = L4M4.getStringList("tablist.footer");

        // Falls eine Liste leer ist, setzen wir eine Fallback-Nachricht
        if (header.isEmpty()) {
            header.add("<gray>Error</gray>");
        }
        if (footer.isEmpty()) {
            footer.add("<gray>Error</gray>");
        }
    }

    @Override
    public void run() {
        if (header.isEmpty() || footer.isEmpty()) {
            reloadData();
        }

        int headerIndex = index % header.size();
        int footerIndex = index % footer.size();

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendPlayerListHeaderAndFooter(
                    MiniMessage.miniMessage().deserialize(header.get(headerIndex)),
                    MiniMessage.miniMessage().deserialize(footer.get(footerIndex))
            );
        });

        index++;

        if (index >= Math.max(header.size(), footer.size())) {
            index = 0;
        }
    }
}
