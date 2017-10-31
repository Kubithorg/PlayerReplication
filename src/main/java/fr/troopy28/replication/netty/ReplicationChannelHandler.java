package fr.troopy28.replication.netty;

import com.mojang.authlib.properties.Property;
import fr.troopy28.replication.ReplicationMod;
import fr.troopy28.replication.broking.BrokingConstant;
import fr.troopy28.replication.replication.ReplicationManager;
import fr.troopy28.replication.replication.protocol.KubithonPacket;
import fr.troopy28.replication.replication.protocol.PlayerConnectionKubicket;
import fr.troopy28.replication.utils.ForgeScheduler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;

import java.util.Base64;

/**
 * @author troopy28
 * @since 1.0.0
 */
class ReplicationChannelHandler extends ChannelInboundHandlerAdapter {
    /**
     * A pointer to the replication mod class.
     */
    private ReplicationMod mod = ReplicationMod.get();
    /**
     * The player to replicate.
     */
    private EntityPlayerMP player;

    /**
     * Package-local constructor. <br/>
     * Creates a {@link ReplicationChannelHandler} for the specified player. This will start listening to the packets
     * the player will send to the server, and create a copy for the Redis network. It will also send the player's
     * stuff through this same network one second later, to make sure any other task modifying the stuff is terminated.
     *
     * @param player The player to replicate.
     */
    ReplicationChannelHandler(EntityPlayerMP player) {
        this.player = player;
        mod.getLogger().info("Created a ReplicationChannelHandler for the player " + player.getName() + ".");

        // Then send the Redis Kubicket notifying the connection
        PlayerConnectionKubicket connectionKubicket = new PlayerConnectionKubicket();
        connectionKubicket.setPlayerName(player.getName());
        connectionKubicket.setPlayerUuid(player.getUniqueID().toString());

        // The skin
        Property skin = player.getGameProfile().getProperties().get("textures").iterator().next();
        connectionKubicket.setPlayerSkin(skin.getValue());
        connectionKubicket.setPlayerSkinSignature(skin.getSignature());

        mod.getMessageBroker().publish(
                BrokingConstant.REPLICATION_PATTERN.concat(String.valueOf(mod.getServerId())).concat(player.getName()),
                Base64.getEncoder().encodeToString(connectionKubicket.serialize())
        );

        // Send the player stuff
        ForgeScheduler.runTaskLater(() -> ReplicationManager.sendPlayerStuff(player), 1000);
    }

    /**
     * Called whenever a packet is received, right before the packet_handler stage. This way, the msg argument is a
     * packet object, as the previous stages managed to do so. This function is responsible of trying to create a
     * kubicket matching with the received packet, and if it could, send the byte array representation of this byte
     * array through the Redis network to replicate the received packet.
     *
     * @param context The context of this handler.
     * @param msg     The received message. Here a packet object.
     * @throws Exception
     * @see ChannelHandlerContext
     */
    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
        Packet<?> receivedPacket = (Packet<?>) msg;
        KubithonPacket kubicket = KubithonPacket.generateKubicket(receivedPacket);

        if (kubicket != null) { // The packet has been recognized
            mod.getMessageBroker().publish(
                    BrokingConstant.REPLICATION_PATTERN.concat(String.valueOf(mod.getServerId())).concat(player.getName()),
                    Base64.getEncoder().encodeToString(kubicket.serialize())
            );
        }

        super.channelRead(context, msg);
    }
}