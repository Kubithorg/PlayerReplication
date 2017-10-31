package fr.troopy28.replication.netty;

import fr.troopy28.replication.ReplicationMod;
import fr.troopy28.replication.broking.BrokingConstant;
import fr.troopy28.replication.replication.protocol.PlayerConnectionKubicket;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Base64;

/**
 * Util class for adding the {@link ReplicationChannelHandler}.
 *
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public class ReplicateHandler {

    private static ReplicationMod plugin = ReplicationMod.get();

    private ReplicateHandler() {
    }

    /**
     * Adds a channel handler to the given player which is going to replicate his packets
     * to servers on the whole network.
     *
     * @param player The player.
     */
    public static void handle(EntityPlayerMP player) {
        player.connection.getNetworkManager().
                channel().pipeline().addBefore("packet_handler", "replication-channel", new ReplicationChannelHandler(player));
    }

    /**
     * Creates a {@link PlayerConnectionKubicket} to send a message saying that the specified player has disconnected,
     * and should no longer be replicated.
     *
     * @param player The player you no more want to be replicated.
     */
    public static void stopHandling(EntityPlayerMP player) {
        plugin.getLogger().info("Tyring to stop handling " + player);
        PlayerConnectionKubicket connectionKubicket = new PlayerConnectionKubicket();
        connectionKubicket.setPlayerName(player.getName());
        connectionKubicket.setPlayerUuid(player.getUniqueID().toString());
        connectionKubicket.setState((byte) 1); // 1 = disconnection
        plugin.getMessageBroker().publish(
                BrokingConstant.REPLICATION_PATTERN.concat(String.valueOf(plugin.getServerId())).concat(player.getName()),
                Base64.getEncoder().encodeToString(connectionKubicket.serialize()));
        plugin.getLogger().info("Sent the disconnection packet.");
    }
}