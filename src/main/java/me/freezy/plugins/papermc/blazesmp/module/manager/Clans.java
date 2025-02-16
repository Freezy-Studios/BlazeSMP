package me.freezy.plugins.papermc.blazesmp.module.manager;

import me.freezy.plugins.papermc.blazesmp.module.Clan;

import java.io.File;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Logger;

public class Clans {
    private static final Logger LOGGER = Logger.getLogger("ClanManager");
    private static final String CLAN_STORAGE_PATH = "plugins/BlazeSMP/storage/clans/";

    private final LinkedList<Clan> clans;

    public Clans() {
        this.clans = new LinkedList<>();
    }

    /**
     * Returns the list of loaded clans.
     *
     * @return LinkedList of Clan objects.
     */
    public LinkedList<Clan> getClans() {
        return clans;
    }

    /**
     * Loads all clan files from the storage folder.
     */
    public void loadAllClans() {
        File dir = new File(CLAN_STORAGE_PATH);
        if (!dir.exists()) {
            LOGGER.info("Clan storage directory does not exist. Creating directory...");
            dir.mkdirs();
            return;
        }
        File[] files = dir.listFiles((file, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            LOGGER.info("No clan files found in " + CLAN_STORAGE_PATH);
            return;
        }
        for (File file : files) {
            try {
                // Assume file name is <uuid>.json
                String filename = file.getName();
                String uuidString = filename.substring(0, filename.lastIndexOf('.'));
                UUID clanUUID = UUID.fromString(uuidString);
                Clan clan = Clan.load(clanUUID);
                if (clan != null) {
                    clans.add(clan);
                    LOGGER.info("Loaded clan: " + clan.getName() + " (" + clan.getUuid() + ")");
                } else {
                    LOGGER.warning("Failed to load clan with UUID: " + clanUUID);
                }
            } catch (Exception e) {
                LOGGER.warning("Error loading clan file " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Saves all loaded clans to their respective JSON files.
     */
    public void saveAllClans() {
        for (Clan clan : clans) {
            clan.save();
        }
    }

    /**
     * Retrieves a clan by its name (case-insensitive).
     *
     * @param name The name of the clan.
     * @return The matching Clan or null if not found.
     */
    public Clan getClanByName(String name) {
        for (Clan clan : clans) {
            if (clan.getName().equalsIgnoreCase(name)) {
                return clan;
            }
        }
        return null;
    }

    /**
     * Retrieves a clan by its UUID.
     *
     * @param uuid The UUID of the clan.
     * @return The matching Clan or null if not found.
     */
    public Clan getClanByUUID(UUID uuid) {
        for (Clan clan : clans) {
            if (clan.getUuid().equals(uuid)) {
                return clan;
            }
        }
        return null;
    }

    /**
     * Adds a clan to the manager.
     *
     * @param clan The Clan to add.
     */
    public void addClan(Clan clan) {
        if (!clans.contains(clan)) {
            clans.add(clan);
        }
    }

    /**
     * Removes a clan from the manager and deletes its JSON file.
     *
     * @param clan The Clan to remove.
     */
    public void removeClan(Clan clan) {
        clans.remove(clan);
        File file = new File(CLAN_STORAGE_PATH + clan.getUuid() + ".json");
        if (file.exists()) {
            if (!file.delete()) {
                LOGGER.warning("Failed to delete clan file: " + file.getAbsolutePath());
            }
        }
    }
}
