package org.kubithon.replicate.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.server.v1_9_R2.Packet;
import org.bukkit.entity.Player;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.BrokingConstant;
import org.kubithon.replicate.replication.protocol.KubicketContainer;

import java.util.Base64;

/**
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public class ReplicationChannelHandler extends ChannelInboundHandlerAdapter {

    private ReplicatePlugin plugin = ReplicatePlugin.get();
    private Player player;

    ReplicationChannelHandler(Player player) {
        this.player = player;
        plugin.getLogger().info("Created a ReplicationChannelHandler for the player " + player.getDisplayName() + ".");
    }

    // Called when a packet is received.
    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
        Packet<?> packet = (Packet<?>) msg;
        KubicketContainer container = KubicketContainer.generateKbContainer(packet);

        if (container != null) { // The packet has been recognized
            plugin.getMessageBroker().publish(
                    BrokingConstant.REPLICATION_TOPIC.concat(player.getName()),
                    Base64.getEncoder().encodeToString(container.serialize())
            );
        }

        super.channelRead(context, msg);
    }
}