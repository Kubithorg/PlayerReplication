package org.kubithon.replicate.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.replication.ReplicationManager;

/**
 * The class responsible of listening the changes of the item in hand.
 *
 * @author troopy28
 * @since 1.0.0
 */
public class ItemHeldChangedListener implements Listener {

    /**
     * Called whenever a player changes the active slot of his inventory (using the scroll, for instance). When it
     * occurs, if the player is a sponsor, sends the <b>visible</b> stuff of this player through the Redis network,
     * one 5 ticks later.
     *
     * @param event The item changing event.
     */
    @EventHandler
    public void onItemHeldChanged(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!ReplicatePlugin.get().shouldBeReplicated(player))
            return;

        Bukkit.getScheduler().runTaskLater(ReplicatePlugin.get(), () -> ReplicationManager.sendPlayerStuff(player), 5);
    }

}