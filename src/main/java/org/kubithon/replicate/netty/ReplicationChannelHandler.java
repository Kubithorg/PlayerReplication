package org.kubithon.replicate.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.server.v1_9_R2.EnumProtocolDirection;
import net.minecraft.server.v1_9_R2.NetworkManager;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketDataSerializer;
import org.bukkit.entity.Player;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.BrokingConstant;

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

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet<?> packet = (Packet<?>) msg;

        PacketDataSerializer serializer = null;
        Integer id = ctx.attr(NetworkManager.c).get().a(EnumProtocolDirection.SERVERBOUND, packet);
        if (id != null)
            try {
                serializer = new PacketDataSerializer(ctx.alloc().buffer());
                serializer.b(id);
                packet.b(serializer);
                plugin.getMessageBroker().publish(
                        BrokingConstant.REPLICATION_TOPIC
                                .concat(player.getUniqueId().toString())
                                .concat(player.getName()),
                        Base64.getEncoder().encodeToString(serializer.a()));
            } finally {
                if (serializer != null)
                    serializer.release();
            }

        super.channelRead(ctx, msg);
    }
}