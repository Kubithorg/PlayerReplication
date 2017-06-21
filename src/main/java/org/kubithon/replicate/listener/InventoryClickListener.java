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
 * @author troopy28
 * @since 1.0.0
 */
public class InventoryClickListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST) // the last handler that should be called
    public void onInventoryClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (!isArmorSlot(slot))
            return;

        Player holder = (Player) event.getInventory().getHolder();
        if (!(holder.hasPermission("kubithon.replicate") || holder.isOp()) /*isOp() -> for debugging */)
            return;

        Bukkit.getScheduler().runTaskLater(ReplicatePlugin.get(), () -> ReplicationManager.sendPlayerStuff(holder), 5);
    }

    private boolean isArmorSlot(int slot) {
        return slot <= 8 && slot >= 5;
    }
}