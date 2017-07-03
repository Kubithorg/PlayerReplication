package org.kubithon.replicate.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.replication.ReplicationManager;

/**
 * The class responsible of listening for clicks in inventories, in order to replicate the stuff of the sponsors.
 *
 * @author troopy28
 * @since 1.0.0
 */
public class InventoryClickListener implements Listener {

    /**
     * Called whenever a player clicks a slot in his inventory. When it occurs, if the player is a sponsor, sends the
     * <b>visible</b> stuff of this player through the Redis network, one 5 ticks later.
     *
     * @param event The click event.
     */
    @EventHandler(priority = EventPriority.HIGHEST) // The last handler that should be called
    public void onInventoryClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (!isArmorSlot(slot) && !isVisibleInventorySlot(slot))
            return;

        Player holder = (Player) event.getInventory().getHolder();
        if (!ReplicatePlugin.get().shouldBeReplicated(holder))
            return;

        Bukkit.getScheduler().runTaskLater(ReplicatePlugin.get(), () -> ReplicationManager.sendPlayerStuff(holder), 5);
    }

    private boolean isArmorSlot(int slot) {
        return slot <= 8 && slot >= 5;
    }

    private boolean isVisibleInventorySlot(int slot) {
        return slot <= 44 && slot >= 36;
    }
}