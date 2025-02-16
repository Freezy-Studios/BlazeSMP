package me.freezy.plugins.papermc.blazesmp.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Getter
public class Clan {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String STORAGE_PATH = "plugins/BlazeSMP/storage/clans/";
    private static final Logger LOGGER = Logger.getLogger("Clan");

    private final UUID uuid;
    @Setter private String name;
    @Setter private Component tag;
    @Setter private UUID leaderUUID;
    @Setter private UUID viceUUID;
    private final LinkedList<UUID> members;
    private final LinkedList<Chunk> chunks;
    private final LinkedHashMap<Chunk, UUID> chunkOwnerMap;
    private int chunkAmount;

    public Clan(String name, Component tag, UUID leaderUUID) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.tag = tag;
        this.leaderUUID = leaderUUID;
        this.viceUUID = null;
        this.members = new LinkedList<>();
        this.chunks = new LinkedList<>();
        this.chunkOwnerMap = new LinkedHashMap<>();
        this.chunkAmount = 0;
    }

    public Clan(UUID clanUUID, String name, Component tag, UUID leaderUUID, UUID viceUUID,
                LinkedList<UUID> members, LinkedList<Chunk> chunks, LinkedHashMap<Chunk, UUID> chunkOwnerMap) {
        this.uuid = clanUUID;
        this.name = name;
        this.tag = tag;
        this.leaderUUID = leaderUUID;
        this.viceUUID = viceUUID;
        this.members = members;
        this.chunks = chunks;
        this.chunkOwnerMap = chunkOwnerMap;
        this.chunkAmount = chunks.size();
    }

    /**
     * Loads a Clan from the JSON file corresponding to the given UUID.
     *
     * @param uuid The UUID of the clan.
     * @return The loaded Clan or null if the file does not exist.
     */
    public static Clan load(UUID uuid) {
        File file = new File(STORAGE_PATH + uuid + ".json");
        if (!file.exists()) {
            LOGGER.warning("Clan file " + file.getAbsolutePath() + " does not exist.");
            return null;
        }
        try (FileReader reader = new FileReader(file)) {
            ClanJson jsonClan = GSON.fromJson(reader, ClanJson.class);
            if (jsonClan == null) {
                LOGGER.warning("Failed to parse clan JSON for UUID " + uuid);
                return null;
            }
            Component tagComponent = MiniMessage.miniMessage().deserialize(jsonClan.tag);
            UUID leader = (jsonClan.leader == null || jsonClan.leader.isEmpty()) ? null : UUID.fromString(jsonClan.leader);
            UUID vice = (jsonClan.vize == null || jsonClan.vize.isEmpty()) ? null : UUID.fromString(jsonClan.vize);

            // Convert members
            LinkedList<UUID> memberUUIDs = new LinkedList<>();
            if (jsonClan.members != null) {
                for (String s : jsonClan.members) {
                    if (s != null && !s.isEmpty()) {
                        memberUUIDs.add(UUID.fromString(s));
                    }
                }
            }

            // Process chunks with world information
            LinkedList<Chunk> chunkList = new LinkedList<>();
            LinkedHashMap<Chunk, UUID> chunkOwnerMap = new LinkedHashMap<>();
            if (jsonClan.chunks != null && jsonClan.chunks.locations != null) {
                for (LocationJson loc : jsonClan.chunks.locations) {
                    World world = Bukkit.getWorld(loc.world);
                    if (world == null) {
                        LOGGER.warning("World '" + loc.world + "' not found. Skipping chunk at " + loc.x + ", " + loc.z);
                        continue;
                    }
                    int x = Integer.parseInt(loc.x);
                    int z = Integer.parseInt(loc.z);
                    Chunk chunk = world.getChunkAt(x, z);
                    chunkList.add(chunk);
                    UUID ownerUUID = (loc.owner == null || loc.owner.isEmpty()) ? null : UUID.fromString(loc.owner);
                    chunkOwnerMap.put(chunk, ownerUUID);
                }
            }

            Clan clan = new Clan(uuid, jsonClan.name, tagComponent, leader, vice, memberUUIDs, chunkList, chunkOwnerMap);
            clan.chunkAmount = (jsonClan.chunks != null) ? jsonClan.chunks.amount : chunkList.size();
            return clan;
        } catch (IOException e) {
            LOGGER.severe("Error loading clan: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reloads the clan from its corresponding JSON file.
     */
    public void reload() {
        Clan loaded = load(this.uuid);
        if (loaded == null) {
            LOGGER.warning("Failed to reload clan with UUID: " + this.uuid);
            return;
        }
        this.name = loaded.name;
        this.tag = loaded.tag;
        this.leaderUUID = loaded.leaderUUID;
        this.viceUUID = loaded.viceUUID;
        this.members.clear();
        this.members.addAll(loaded.members);
        this.chunks.clear();
        this.chunks.addAll(loaded.chunks);
        this.chunkOwnerMap.clear();
        this.chunkOwnerMap.putAll(loaded.chunkOwnerMap);
        this.chunkAmount = loaded.chunkAmount;
    }

    /**
     * Saves the clan data to its corresponding JSON file.
     */
    public void save() {
        ClanJson jsonClan = new ClanJson();
        jsonClan.name = this.name;
        jsonClan.tag = MiniMessage.miniMessage().serialize(this.tag);
        jsonClan.leader = (this.leaderUUID == null) ? "" : this.leaderUUID.toString();
        jsonClan.vize = (this.viceUUID == null) ? "" : this.viceUUID.toString();
        jsonClan.members = this.members.stream().map(UUID::toString).toList();

        // Prepare chunks JSON
        jsonClan.chunks = new ChunksJson();
        jsonClan.chunks.amount = this.chunkAmount;
        jsonClan.chunks.locations = new LinkedList<>();
        for (Chunk chunk : this.chunks) {
            LocationJson loc = new LocationJson();
            // Assuming the owner mapping may be null
            UUID owner = this.chunkOwnerMap.getOrDefault(chunk, null);
            loc.owner = (owner == null) ? "" : owner.toString();
            // Store world name along with chunk coordinates
            loc.world = chunk.getWorld().getName();
            loc.x = String.valueOf(chunk.getX());
            loc.z = String.valueOf(chunk.getZ());
            jsonClan.chunks.locations.add(loc);
        }

        // Ensure directory exists
        File dir = new File(STORAGE_PATH);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                LOGGER.info("Successfully created folder structure!");
            } else {
                LOGGER.severe("Failed to create folder structure!");
            }
        }
        File file = new File(dir, this.uuid + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(jsonClan, writer);
        } catch (IOException e) {
            LOGGER.severe("Error saving clan: " + e.getMessage());
        }
    }

    public boolean isLeader(UUID playerUUID) {
        return this.leaderUUID.equals(playerUUID);
    }

    public boolean isVice(UUID playerUUID) {
        return this.viceUUID != null && this.viceUUID.equals(playerUUID);
    }

    public boolean isMember(UUID playerUUID) {
        return this.leaderUUID.equals(playerUUID) || (this.viceUUID != null && this.viceUUID.equals(playerUUID)) || this.members.contains(playerUUID);
    }

    // Helper classes to represent the JSON structure

    private static class ClanJson {
        String name;
        String tag;
        String leader;
        String vize;
        List<String> members;
        ChunksJson chunks;
    }

    private static class ChunksJson {
        int amount;
        List<LocationJson> locations;
    }

    private static class LocationJson {
        String owner;
        String world;
        String x;
        String z;
    }
}
