package org.kubithon.replicate.listener;

import org.bukkit.event.player.PlayerQuitEvent;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.netty.ReplicateHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * The class listening the connections and disconnections from the server, in order to start or stop the replication
 * of the sponsors.
 *
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public class ConnectionListener implements Listener {

    /**
     * Called whenever a player join the server. Check whether this player should be replicated, and starts his
     * replication if he should be.
     *
     * @param event The join event.
     */
    @EventHandler(priority = EventPriority.HIGHEST) // the last handler that should be called
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (ReplicatePlugin.get().shouldBeReplicated(player))
            ReplicateHandler.handle(player);
    }

    /**
     * Called whenever a player leaves the server. Check whether this player should be replicated, and if he is, stops
     * the replication of this player, as he was normally replicated (see the onJoin method).
     *
     * @param event The quit event.
     */
    @EventHandler(priority = EventPriority.HIGHEST) // the last handler that should be called
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (ReplicatePlugin.get().shouldBeReplicated(player))
            ReplicateHandler.stopHandling(player);
    }
}
