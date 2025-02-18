package me.freezy.plugins.papermc.blazesmp.module.manager;

import lombok.Getter;
import me.freezy.plugins.papermc.blazesmp.module.Clan;
import org.bukkit.Chunk;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@SuppressWarnings("unused")
@Getter
public class Clans {
    private static final Logger LOGGER = Logger.getLogger("ClanManager");
    private static final String CLAN_STORAGE_PATH = "plugins/BlazeSMP/storage/clans/";

    /**
     * -- GETTER --
     *  Returns the list of loaded clans.
     *
     */
    private final LinkedList<Clan> clans;

    public Clans() {
        this.clans = new LinkedList<>();
    }

    /**
     * Loads all clan files from the storage folder.
     */
    public void loadAllClans() {
        File dir = new File(CLAN_STORAGE_PATH);
        if (!dir.exists()) {
            LOGGER.info("Clan storage directory does not exist. Creating directory...");
            if (!dir.mkdirs()) {
                LOGGER.warning("Failed to create clan storage directory.");
            } else {
                LOGGER.info("Created clan storage directory.");
            }
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

    public LinkedHashMap<UUID, LinkedList<Chunk>> getClanChunks(Clan clan) {
        LinkedHashMap<UUID, LinkedList<Chunk>> clanChunks = new LinkedHashMap<>();
        for (Map.Entry<Chunk, UUID> entry : clan.getChunkOwnerMap().entrySet()) {
            UUID ownerUUID = entry.getValue();
            Chunk chunk = entry.getKey();
            if (!clanChunks.containsKey(ownerUUID)) {
                clanChunks.put(ownerUUID, new LinkedList<>());
            }
            clanChunks.get(ownerUUID).add(chunk);
        }
        return clanChunks;
    }

    public LinkedList<Chunk> getPlayerChunks(UUID playerUUID) {
        LinkedList<Chunk> playerChunks = new LinkedList<>();
        for (Clan clan : clans) {
            if (clan.getChunkOwnerMap().containsValue(playerUUID)) {
                for (Map.Entry<Chunk, UUID> entry : clan.getChunkOwnerMap().entrySet()) {
                    if (entry.getValue().equals(playerUUID)) {
                        playerChunks.add(entry.getKey());
                    }
                }
            }
        }
        return playerChunks;
    }

    public boolean isLeader(UUID playerUUID) {
        for (Clan clan : clans) {
            if (clan.getLeaderUUID().equals(playerUUID)) {
                return true;
            }
        }
        return false;
    }

    public boolean isVice(UUID playerUUID) {
        for (Clan clan : clans) {
            if (clan.getViceUUID() != null && clan.getViceUUID().equals(playerUUID)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMember(UUID playerUUID) {
        for (Clan clan : clans) {
            if (clan.getMembers().contains(playerUUID)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInClan(UUID playerUUID) {
        for (Clan clan : clans) {
            if (clan.getMembers().contains(playerUUID) || clan.getLeaderUUID().equals(playerUUID) || (clan.getViceUUID() != null && clan.getViceUUID().equals(playerUUID))) {
                return true;
            }
        }
        return false;
    }

    public boolean isInClan(UUID playerUUID,Clan clan) {
        return clan.getMembers().contains(playerUUID) || clan.getLeaderUUID().equals(playerUUID) || (clan.getViceUUID() != null && clan.getViceUUID().equals(playerUUID));
    }

    public Clan getClanByMember(UUID playerUUID) {
        for (Clan clan : clans) {
            if (clan.getMembers().contains(playerUUID) || clan.getLeaderUUID().equals(playerUUID) || (clan.getViceUUID() != null && clan.getViceUUID().equals(playerUUID))) {
                return clan;
            }
        }
        return null;
    }

    public void setClanChunks(Clan playerClan, LinkedHashMap<UUID, LinkedList<Chunk>> existingClaims) {
        for (Map.Entry<UUID, LinkedList<Chunk>> entry : existingClaims.entrySet()) {
            UUID ownerUUID = entry.getKey();
            LinkedList<Chunk> chunks = entry.getValue();
            for (Chunk chunk : chunks) {
                playerClan.getChunkOwnerMap().put(chunk, ownerUUID);
            }
        }
        playerClan.save();
    }

    public Clan getClanByChunk(Chunk chunk) {
        for (Clan clan : clans) {
            if (clan.getChunks().contains(chunk)) {
                return clan;
            }
        }
        return null;
    }
}
