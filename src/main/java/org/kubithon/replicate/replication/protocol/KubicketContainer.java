package org.kubithon.replicate.replication.protocol;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketLoginInStart;
import net.minecraft.server.v1_9_R2.PacketPlayInFlying;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.kubithon.replicate.ReplicatePlugin;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;

import java.io.IOException;

/**
 * @author troopy28
 * @since 1.0.0
 */
@Message
public class KubicketContainer {
    private static MessagePack messagePack = new MessagePack();

    public byte packetId;
    public KubithonPacket packet;

    public <T extends KubithonPacket> T extractPacket() {
        try {
            return (T) packet;
        } catch (Exception e) {
            ReplicatePlugin.get().getLogger().severe(ExceptionUtils.getFullStackTrace(e));
            return null;
        }
    }

    /**
     * Creates a kubicket to send the data over the servers.
     *
     * @param receivedPacket The packet that has been received.
     * @return Return the created kubicket corresponding to the specified packet. Null if the packet isn't a packet
     * to replicate.
     */
    public static KubicketContainer generateKbContainer(Packet<?> receivedPacket) {
        KubicketContainer container = null;

        // Position only
        if (receivedPacket instanceof PacketPlayInFlying.PacketPlayInPosition) {
            PacketPlayInFlying.PacketPlayInPosition posPacket = (PacketPlayInFlying.PacketPlayInPosition) receivedPacket;
            PlayerPositionKubicket kubicket = new PlayerPositionKubicket();

            /* ATTENTION!! IN THE NEXT THREE LINES :
            * Calling the a, b or c method with a DOUBLE as argument makes it returns the x, y, or z coordinate,
            * respectively.
            * Calling the a or b method with a FLOAT as argument makes it returns the yaw and the pitch, respectively.
            */
            kubicket.xPos = (float) posPacket.a(0.0d); // a() returns the x coordinate...
            kubicket.yPos = (float) posPacket.b(0.0d); // b() returns the x coordinate...
            kubicket.zPos = (float) posPacket.c(0.0d); // c() returns the x coordinate...

            container = new KubicketContainer();
            container.packetId = KubicketType.PLAYER_POSITION.getId();
            container.packet = kubicket;
        }
        // Position and look
        else if (receivedPacket instanceof PacketPlayInFlying.PacketPlayInPositionLook) {
            PacketPlayInFlying.PacketPlayInPositionLook posLookPacket = (PacketPlayInFlying.PacketPlayInPositionLook) receivedPacket;
            PlayerPositionLookKubicket kubicket = new PlayerPositionLookKubicket();

            /* ATTENTION!! IN THE NEXT FIVE LINES :
            * Calling the a, b or c method with a DOUBLE as argument makes it returns the x, y, or z coordinate,
            * respectively.
            * Calling the a or b method with a FLOAT as argument makes it returns the yaw and the pitch, respectively.
            */
            kubicket.xPos = (float) posLookPacket.a(0.0d); // a() returns the x coordinate...
            kubicket.yPos = (float) posLookPacket.b(0.0d); // b() returns the x coordinate...
            kubicket.zPos = (float) posLookPacket.c(0.0d); // c() returns the x coordinate...
            kubicket.yaw = posLookPacket.a(0.0f);          // a() returns the yaw...
            kubicket.pitch = posLookPacket.b(0.0f);         // b() returns the pitch...

            container = new KubicketContainer();
            container.packetId = KubicketType.PLAYER_POSITION_LOOK.getId();
            container.packet = kubicket;
        }
        // Look only
        else if (receivedPacket instanceof PacketPlayInFlying.PacketPlayInLook) {
            PacketPlayInFlying.PacketPlayInLook lookPacket = (PacketPlayInFlying.PacketPlayInLook) receivedPacket;
            PlayerLookKubicket kubicket = new PlayerLookKubicket();

            /* ATTENTION!! IN THE NEXT TWO LINES :
            * Calling the a or b method with a FLOAT as argument makes it returns the yaw and the pitch, respectively.
            */
            kubicket.yaw = lookPacket.a(0.0f);          // a() returns the yaw...
            kubicket.pitch = lookPacket.b(0.0f);         // b() returns the pitch...

            container = new KubicketContainer();
            container.packetId = KubicketType.PLAYER_LOOK.getId();
            container.packet = kubicket;
        }
        // Connection
        else if (receivedPacket instanceof PacketLoginInStart) {
            PacketLoginInStart loginPacket = (PacketLoginInStart) receivedPacket;
            PlayerConnectionKubicket kubicket = new PlayerConnectionKubicket();
            GameProfile playerProfile = loginPacket.a();
            kubicket.playerName = playerProfile.getName();
            kubicket.playerUuid = playerProfile.getId().toString();

            container = new KubicketContainer();
            container.packetId = KubicketType.PLAYER_CONNECTION.getId();
            container.packet = kubicket;
        }

        return container;
    }

    public static KubicketContainer deserialize(byte[] kubicketBytes) throws IOException {
        return messagePack.read(kubicketBytes, KubicketContainer.class);
    }

    /**
     * @return Returns the type of the packet, in function of the id.
     */
    public KubicketType getPacketType() {
        return KubicketType.values()[packetId];
    }

    public byte[] serialize() throws IOException {
        return messagePack.write(this);
    }
}