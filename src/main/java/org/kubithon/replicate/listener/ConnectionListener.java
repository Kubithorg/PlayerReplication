package org.kubithon.replicate.listener;

import org.kubithon.replicate.netty.ReplicateHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Oscar Davis
 * @since 1.0.0
 */
public class ConnectionListener implements Listener
{

    @EventHandler(priority = EventPriority.HIGHEST) // the last handler that should be called
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (player.hasPermission("kubithon.replicate") || player.isOp() /*isOp() -> for debugging */)
            ReplicateHandler.handle(player);
    }
}
