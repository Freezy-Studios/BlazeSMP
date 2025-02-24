package me.freezy.plugins.papermc.blazesmp.module.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Getter
public class Homes {
    private static final String FILE_PATH = "plugins/BlazeSMP/storage/homes.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = Logger.getLogger("Homes");

    // Mapping of player UUID to their home location
    private final LinkedHashMap<UUID, Location> homes;

    public Homes() {
        this.homes = new LinkedHashMap<>();
    }

    /**
     * Loads homes from the JSON file.
     */
    public void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            LOGGER.info("Homes file does not exist, a new one will be created upon saving.");
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            // Use a TypeToken to handle the Map<String, LocationJson> structure
            Type type = new TypeToken<Map<String, LocationJson>>() {
            }.getType();
            Map<String, LocationJson> jsonMap = GSON.fromJson(reader, type);
            if (jsonMap == null) {
                return;
            }
            for (Map.Entry<String, LocationJson> entry : jsonMap.entrySet()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(entry.getKey());
                } catch (IllegalArgumentException ex) {
                    LOGGER.warning("Invalid UUID key in homes file: " + entry.getKey());
                    continue;
                }
                LocationJson locJson = entry.getValue();
                // Assume the default world "world" for homes

                try {
                    double x = Double.parseDouble(locJson.x);
                    double y = Double.parseDouble(locJson.y);
                    double z = Double.parseDouble(locJson.z);
                    float yaw = Float.parseFloat(locJson.yaw);
                    float pitch = Float.parseFloat(locJson.pitch);
                    String worldName = locJson.world;
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        LOGGER.warning("World '%s' not found. Skipping home for %s".formatted(worldName, uuid));
                        continue;
                    }
                    Location location = new Location(world, x, y, z, yaw, pitch);
                    homes.put(uuid, location);
                } catch (NumberFormatException ex) {
                    LOGGER.warning("Invalid number format for home of " + uuid + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error loading homes: " + e.getMessage());
        }
    }

    /**
     * Saves the homes mapping to the JSON file.
     */
    public void save() {
        // Convert the homes map to a map of String keys (UUIDs) to LocationJson objects.
        Map<String, LocationJson> jsonMap = new LinkedHashMap<>();
        for (Map.Entry<UUID, Location> entry : homes.entrySet()) {
            Location location = entry.getValue();
            LocationJson locJson = new LocationJson();
            locJson.x = String.valueOf(location.getX());
            locJson.y = String.valueOf(location.getY());
            locJson.z = String.valueOf(location.getZ());
            locJson.yaw = String.valueOf(location.getYaw());
            locJson.pitch = String.valueOf(location.getPitch());
            locJson.world = location.getWorld().getName();
            jsonMap.put(entry.getKey().toString(), locJson);
        }
        File file = new File(FILE_PATH);
        // Ensure the parent directory exists
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (parent.mkdirs()) {
                LOGGER.info("Successfully created folder structure!");
            } else {
                LOGGER.severe("Failed to create folder structure!");
            }
        }
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(jsonMap, writer);
        } catch (IOException e) {
            LOGGER.severe("Error saving homes: " + e.getMessage());
        }
    }

    public boolean hasHome(Player player) {
        return homes.containsKey(player.getUniqueId());
    }

    public Location getHome(Player player) {
        return homes.get(player.getUniqueId());
    }

    public void setHome(Player player) {
        homes.put(player.getUniqueId(), player.getLocation());
    }

    public void removeHome(Player player) {
        homes.remove(player.getUniqueId());
    }

    /**
     * Inner class representing the JSON structure for a location.
     */
    private static class LocationJson {
        String x;
        String y;
        String z;
        String yaw;
        String pitch;
        String world;
    }
}
