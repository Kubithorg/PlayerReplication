package org.kubithon.replicate.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.replication.ReplicationManager;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ItemHeldChangedListener implements Listener {

    @EventHandler
    public void onItemHeldChanged(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!(player.hasPermission("kubithon.replicate") || player.isOp()) /*isOp() -> for debugging */)
            return;

        Bukkit.getScheduler().runTaskLater(ReplicatePlugin.get(), () -> ReplicationManager.sendPlayerStuff(player), 5);
    }

}