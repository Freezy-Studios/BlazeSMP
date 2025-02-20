package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ChunkInventoryListener implements Listener {

    // Speichert pro Spieler den aktuellen Seitenindex
    private final PaginatedData paginatedData = new PaginatedData();

    public static void openInv(Player player) {
        Clan clan = BlazeSMP.getInstance().getClans().getClanByMember(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    me.freezy.plugins.papermc.blazesmp.module.manager.L4M4.get("chunk.error.no_clan")
            ));
            return;
        }
        new ChunkInventoryListener().chunksInv(player, clan);
    }

    /**
     * Öffnet das Clan-Chunks-Inventar für den Spieler.
     * Die Chunks des Clans werden als Kopf-Items (PLAYER_HEAD) angezeigt.
     *
     * @param player Der Spieler, der das Inventar öffnet.
     * @param clan   Der Clan, dessen Chunks angezeigt werden sollen.
     */
    private void chunksInv(Player player, Clan clan) {
        // Erstelle eine Liste der Map-Einträge (Chunk -> Besitzer UUID) aus dem Clan
        List<Map.Entry<Chunk, UUID>> chunkEntries = new ArrayList<>(clan.getChunkOwnerMap().entrySet());
        int itemsPerPage = 45; // Plätze 0-44 für Items, untere Reihe für Navigation
        int totalPages = (int) Math.ceil(chunkEntries.size() / (double) itemsPerPage);
        int currentPage = 0;
        paginatedData.setPage(player.getUniqueId(), currentPage);
        openChunksMenu(player, chunkEntries, currentPage, totalPages, itemsPerPage, clan);
    }

    /**
     * Baut das Inventar basierend auf der aktuellen Seite auf und öffnet es für den Spieler.
     *
     * @param player       Der Spieler, der das Inventar sieht.
     * @param chunkEntries Liste der Clan-Chunks (als Map.Entry von Chunk und Besitzer UUID).
     * @param currentPage  Aktuelle Seite.
     * @param totalPages   Gesamtzahl der Seiten.
     * @param itemsPerPage Items pro Seite (hier 45).
     * @param ignoredClan         Der Clan, dessen Chunks angezeigt werden.
     */
    private void openChunksMenu(Player player, List<Map.Entry<Chunk, UUID>> chunkEntries,
                                int currentPage, int totalPages, int itemsPerPage,
                                Clan ignoredClan) {
        // Erstelle ein 54-Slot Inventar mit farbigem Titel (Adventure Component)
        Component title = MiniMessage.miniMessage().deserialize(
                me.freezy.plugins.papermc.blazesmp.module.manager.L4M4.get("chunk.title")
        );
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Berechne Start- und Endindex für die aktuelle Seite
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, chunkEntries.size());

        // Füge für jeden Chunk ein Kopf-Item hinzu
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<Chunk, UUID> entry = chunkEntries.get(i);
            Chunk chunk = entry.getKey();
            UUID ownerUUID = entry.getValue();
            OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(ownerUUID);

            // Erstelle ein Kopf-Item
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwningPlayer(ownerPlayer);
            Component itemName = MiniMessage.miniMessage().deserialize("<aqua>Chunk [" + chunk.getX() + ", " + chunk.getZ() + "]</aqua>");
            skullMeta.displayName(itemName);
            List<Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize(L4M4.get("chunk.unclaim_lore")));
            lore.add(MiniMessage.miniMessage().deserialize("<gray>World: " + chunk.getWorld().getName() + "</gray>"));
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Owner: " + ownerPlayer.getName() + "</gray>"));
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Index: " + (i + 1) + "</gray>"));
            skullMeta.lore(lore);
            head.setItemMeta(skullMeta);

            // Platziere das Item in den Slots 0 bis 44
            inv.setItem(i - startIndex, head);
        }

        // Navigation: Falls mehrere Seiten vorhanden sind, füge Navigations-Buttons in der untersten Reihe hinzu
        if (totalPages > 1) {
            // Vorherige Seite (Slot 45)
            if (currentPage > 0) {
                ItemStack prev = new ItemStack(Material.ARROW);
                prev.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize(
                        me.freezy.plugins.papermc.blazesmp.module.manager.L4M4.get("chunk.navigation.previous")
                )));
                inv.setItem(45, prev);
            }
            // Nächste Seite (Slot 53)
            if (currentPage < totalPages - 1) {
                ItemStack next = new ItemStack(Material.ARROW);
                next.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize(
                        me.freezy.plugins.papermc.blazesmp.module.manager.L4M4.get("chunk.navigation.next")
                )));
                inv.setItem(53, next);
            }
        }

        // Öffne das Inventar für den Spieler
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Component invTitle = event.getView().title();
        Component expectedTitle = MiniMessage.miniMessage().deserialize(
                L4M4.get("chunk.title")
        );
        if (!PlainTextComponentSerializer.plainText().serialize(invTitle)
                .equals(PlainTextComponentSerializer.plainText().serialize(expectedTitle))) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        Component itemNameComp = clickedItem.getItemMeta().displayName();
        if (itemNameComp == null) return;
        String displayName = PlainTextComponentSerializer.plainText().serialize(itemNameComp);

        Clan clan = BlazeSMP.getInstance().getClans().getClanByMember(player.getUniqueId());
        if (clan == null) return;

        List<Map.Entry<Chunk, UUID>> chunkEntries = new ArrayList<>(clan.getChunkOwnerMap().entrySet());
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil(chunkEntries.size() / (double) itemsPerPage);
        int currentPage = paginatedData.getPage(player.getUniqueId());

        if (displayName.contains("Previous Page")) {
            if (currentPage > 0) {
                currentPage--;
                paginatedData.setPage(player.getUniqueId(), currentPage);
                openChunksMenu(player, chunkEntries, currentPage, totalPages, itemsPerPage, clan);
            }
        } else if (displayName.contains("Next Page")) {
            if (currentPage < totalPages - 1) {
                currentPage++;
                paginatedData.setPage(player.getUniqueId(), currentPage);
                openChunksMenu(player, chunkEntries, currentPage, totalPages, itemsPerPage, clan);
            }
        } else {
            // Nutze den zentralen Nachrichtentext für Klicks
            String[] parts = displayName.substring(1, displayName.length() - 1).split(",");
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            Chunk chunk = Objects.requireNonNull(Bukkit.getWorld("world")).getChunkAt(x, y);
            if (clan.getChunkOwnerMap().containsKey(chunk)) {
                clan.getChunkOwnerMap().remove(chunk);
                clan.save();
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        L4M4.get("chunk.unclaimed")
                ));
                openChunksMenu(player, chunkEntries, currentPage, totalPages, itemsPerPage, clan);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        paginatedData.removePage(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        paginatedData.removePage(event.getPlayer().getUniqueId());
    }

    private static class PaginatedData {
        private final Map<UUID, Integer> playerPages = new HashMap<>();

        public void setPage(UUID playerUUID, int page) {
            playerPages.put(playerUUID, page);
        }

        public int getPage(UUID playerUUID) {
            return playerPages.getOrDefault(playerUUID, 1);
        }

        public void removePage(UUID playerUUID) {
            playerPages.remove(playerUUID);
        }
    }
}
