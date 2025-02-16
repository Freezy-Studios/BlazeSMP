package me.freezy.plugins.papermc.blazesmp.module;

import lombok.Getter;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Represents a protected block with an owner, a key, and a location.
 */
public record ProtectedBlock(UUID owner, UUID key, Location location) {}
