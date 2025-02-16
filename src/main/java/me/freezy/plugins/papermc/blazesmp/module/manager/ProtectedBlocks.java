package me.freezy.plugins.papermc.blazesmp.module.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.freezy.plugins.papermc.blazesmp.module.ProtectedBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manager class for loading and saving protected blocks.
 */
@Getter
public class ProtectedBlocks {
    private static final String FILE_PATH = "plugins/BlazeSMP/storage/protected_blocks.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = Logger.getLogger("ProtectedBlocks");

    // List of protected blocks
    private final List<ProtectedBlock> blocks;

    public ProtectedBlocks() {
        this.blocks = new ArrayList<>();
    }

    /**
     * Loads protected blocks from the JSON file.
     */
    public void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            LOGGER.info("Protected blocks file does not exist, a new one will be created upon saving.");
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            // Deserialize the JSON into a ProtectedBlocksJson object
            ProtectedBlocksJson jsonData = GSON.fromJson(reader, ProtectedBlocksJson.class);
            if (jsonData == null || jsonData.blocks == null) {
                return;
            }
            blocks.clear();
            for (BlockJson blockJson : jsonData.blocks) {
                try {
                    UUID owner = (blockJson.owner == null || blockJson.owner.isEmpty())
                            ? null : UUID.fromString(blockJson.owner);
                    UUID key = (blockJson.key == null || blockJson.key.isEmpty())
                            ? null : UUID.fromString(blockJson.key);

                    // Use default world "world" since no world field is provided
                    World world = Bukkit.getWorld("world");
                    if (world == null) {
                        LOGGER.warning("Default world 'world' not found. Skipping block for owner: " + blockJson.owner);
                        continue;
                    }
                    double x = Double.parseDouble(blockJson.location.x);
                    double y = Double.parseDouble(blockJson.location.y);
                    double z = Double.parseDouble(blockJson.location.z);
                    Location location = new Location(world, x, y, z);
                    blocks.add(new ProtectedBlock(owner, key, location));
                } catch (Exception e) {
                    LOGGER.warning("Error loading a protected block: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error loading protected blocks: " + e.getMessage());
        }
    }

    /**
     * Saves the protected blocks to the JSON file.
     */
    public void save() {
        ProtectedBlocksJson jsonData = new ProtectedBlocksJson();
        jsonData.blocks = new ArrayList<>();
        for (ProtectedBlock block : blocks) {
            BlockJson blockJson = new BlockJson();
            blockJson.owner = (block.owner() == null) ? "" : block.owner().toString();
            blockJson.key = (block.key() == null) ? "" : block.key().toString();
            blockJson.location = new LocationJson();
            blockJson.location.x = String.valueOf(block.location().getX());
            blockJson.location.y = String.valueOf(block.location().getY());
            blockJson.location.z = String.valueOf(block.location().getZ());
            jsonData.blocks.add(blockJson);
        }
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (parent.mkdirs()) {
                LOGGER.info("Successfully created folder structure!");
            } else {
                LOGGER.severe("Failed to create folder structure!");
            }        }
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(jsonData, writer);
        } catch (IOException e) {
            LOGGER.severe("Error saving protected blocks: " + e.getMessage());
        }
    }

    // Inner classes to match the JSON structure

    private static class ProtectedBlocksJson {
        List<BlockJson> blocks;
    }

    private static class BlockJson {
        String owner;
        String key;
        LocationJson location;
    }

    private static class LocationJson {
        String x;
        String y;
        String z;
    }
}
