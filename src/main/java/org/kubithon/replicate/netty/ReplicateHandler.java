package org.kubithon.replicate.netty;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.BrokingConstant;
import org.kubithon.replicate.replication.protocol.PlayerConnectionKubicket;

import java.util.Base64;

/**
 * Util class for adding the {@link ReplicationChannelHandler}.
 *
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public class ReplicateHandler {

    private static ReplicatePlugin plugin = ReplicatePlugin.get();

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

    /**
     * Creates a {@link PlayerConnectionKubicket} to send a message saying that the specified player has disconnected,
     * and should no longer be replicated.
     *
     * @param player The player you no more want to be replicated.
     */
    public static void stopHandling(Player player) {
        PlayerConnectionKubicket connectionKubicket = new PlayerConnectionKubicket();
        connectionKubicket.setPlayerName(player.getName());
        connectionKubicket.setPlayerUuid(player.getUniqueId().toString());
        connectionKubicket.setState((byte) 1);
        ReplicatePlugin.get().getMessageBroker().publish(
                BrokingConstant.REPLICATION_PATTERN.concat(String.valueOf(plugin.getServerId())).concat(player.getName()),
                Base64.getEncoder().encodeToString(connectionKubicket.serialize()));
    }
}