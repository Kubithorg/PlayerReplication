package org.kubithon.replicate.netty;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Util class for adding the {@link ReplicationChannelHandler}.
 *
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public class ReplicateHandler {

    private ReplicateHandler() {
    }

    /**
     * Adds a channel handler to the given player which is going to replicate his packets
     * to servers on the whole network.
     *
     * @param player The player.
     */
    public static void handle(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection
                .networkManager.channel.pipeline()
                .addBefore("packet_handler", "replication-channel", new ReplicationChannelHandler(player));
    }
}