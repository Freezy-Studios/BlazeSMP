package me.freezy.plugins.papermc.blazesmp.listener;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.module.ProtectedBlock;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import me.freezy.plugins.papermc.blazesmp.module.manager.ProtectedBlocks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ProtectedBlockListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ProtectedBlocks protectedBlocks = BlazeSMP.getInstance().getProtectedBlocks();

    // Supported storage block types
    private final Set<Material> STORAGE_BLOCKS = Set.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL,
            Material.HOPPER, Material.DROPPER, Material.DISPENSER,
            Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER
    );

    @EventHandler
    public void onItemTransferHopperEvent(InventoryMoveItemEvent event) {
        Block destinationBlock = Objects.requireNonNull(event.getDestination().getLocation()).getBlock();
        if (isProtected(destinationBlock)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null || !STORAGE_BLOCKS.contains(block.getType())) return;

        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ProtectedBlock protectedBlock = getProtectedBlock(block);

        // Owner (if the block is locked) can always access the management GUI.
        if (player.isSneaking() && protectedBlock != null &&
                protectedBlock.owner().equals(player.getUniqueId())) {
            openManageKeysGUI(player, block);
            return;
        }

        // Shift + Right-click with a trial key: Relink the key to the new container.
        if (player.isSneaking() && isValidKey(mainHandItem)) {
            relinkKey(player, mainHandItem, block);
            return;
        }

        // Shift + Right-click without a key (and if the container is not locked) → open lock GUI.
        if (player.isSneaking() && protectedBlock == null) {
            openLockGUI(player, block);
            return;
        }

        // Normal right-click with a valid key: open the container inventory.
        if (protectedBlock != null && isValidKey(mainHandItem, protectedBlock.key())) {
            if (block.getState() instanceof Container container) {
                player.openInventory(container.getInventory());
            }
            return;
        }

        // Normal right-click without a key on a locked container: show locked message.
        if (protectedBlock != null) {
            String lockedMsg = String.format(L4M4.get("storage.locked"), "minecraft:trial_key");
            player.sendMessage(miniMessage.deserialize(lockedMsg));
        }
    }

    private void openLockGUI(Player player, Block block) {
        String titleRaw = String.format(L4M4.get("storage.lock_gui_title"), block.getType().toString());
        Inventory lockInventory = Bukkit.createInventory(player, InventoryType.HOPPER,
                miniMessage.deserialize(titleRaw));

        UUID lockUUID = UUID.randomUUID();
        ItemStack trialKey1 = createTrialKey(lockUUID);
        ItemStack trialKey2 = createTrialKey(lockUUID);

        lockInventory.setItem(0, trialKey1);
        lockInventory.setItem(4, trialKey2);

        player.openInventory(lockInventory);
        // Save the protected block.
        protectedBlocks.addBlock(new ProtectedBlock(player.getUniqueId(), lockUUID, block.getLocation()));
    }

    private void openManageKeysGUI(Player player, Block block) {
        String titleRaw = String.format(L4M4.get("storage.manage_gui_title"), block.getType().toString());
        Inventory manageKeysInventory = Bukkit.createInventory(player, InventoryType.HOPPER,
                miniMessage.deserialize(titleRaw));

        ItemStack addKey = new ItemStack(Material.PAPER);
        ItemMeta addKeyMeta = addKey.getItemMeta();
        addKeyMeta.displayName(miniMessage.deserialize(L4M4.get("storage.add_key")));
        addKey.setItemMeta(addKeyMeta);

        ItemStack removeKey = new ItemStack(Material.BARRIER);
        ItemMeta removeKeyMeta = removeKey.getItemMeta();
        removeKeyMeta.displayName(miniMessage.deserialize(L4M4.get("storage.remove_key")));
        removeKey.setItemMeta(removeKeyMeta);

        manageKeysInventory.setItem(1, addKey);
        manageKeysInventory.setItem(3, removeKey);

        player.openInventory(manageKeysInventory);
    }

    private void relinkKey(Player player, ItemStack key, Block newBlock) {
        UUID newLockUUID = UUID.randomUUID();
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            meta.lore(List.of(
                    miniMessage.deserialize(String.format(L4M4.get("storage.linked_to"), newLockUUID.toString())),
                    miniMessage.deserialize(L4M4.get("storage.not_usable_on_vaults"))
            ));
            key.setItemMeta(meta);
        }
        protectedBlocks.addBlock(new ProtectedBlock(player.getUniqueId(), newLockUUID, newBlock.getLocation()));
        player.sendMessage(miniMessage.deserialize(L4M4.get("storage.link_success")));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().title().toString();
        if (title.contains(L4M4.get("storage.lock_gui_title_prefix")) ||
                title.contains(L4M4.get("storage.manage_gui_title_prefix"))) {
            event.setCancelled(true);
            HumanEntity entity = event.getWhoClicked();
            if (entity instanceof Player player) {
                player.sendMessage(miniMessage.deserialize(L4M4.get("storage.action_completed")));
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ProtectedBlock protectedBlock = getProtectedBlock(block);

        if (protectedBlock != null) {
            if (protectedBlock.owner().equals(player.getUniqueId())) {
                protectedBlocks.removeBlock(protectedBlock);
                player.sendMessage(miniMessage.deserialize(L4M4.get("storage.removed_lock")));
            } else {
                event.setCancelled(true);
                player.sendMessage(miniMessage.deserialize(L4M4.get("storage.break_denied")));
            }
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isProtected);
    }

    private ItemStack createTrialKey(UUID lockUUID) {
        ItemStack trialKey = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = trialKey.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(L4M4.get("storage.trial_key")));
            meta.lore(List.of(
                    miniMessage.deserialize(String.format(L4M4.get("storage.linked_to"), lockUUID.toString())),
                    miniMessage.deserialize(L4M4.get("storage.not_usable_on_vaults"))
            ));
            trialKey.setItemMeta(meta);
        }
        return trialKey;
    }

    private boolean isValidKey(ItemStack item) {
        return item != null && item.getType() == Material.TRIPWIRE_HOOK && item.hasItemMeta();
    }

    private boolean isValidKey(ItemStack item, UUID lockUUID) {
        if (!isValidKey(item)) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.lore() == null) return false;
        return Objects.requireNonNull(meta.lore()).stream().anyMatch(line -> line.contains(Component.text(lockUUID.toString())));
    }

    private ProtectedBlock getProtectedBlock(Block block) {
        return protectedBlocks.getBlocks().stream()
                .filter(pb -> pb.location().equals(block.getLocation()))
                .findFirst()
                .orElse(null);
    }

    private boolean isProtected(Block block) {
        return getProtectedBlock(block) != null;
    }
}
