package org.kubithon.replicate.broking.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_9_R2.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.BrokingConstant;
import org.kubithon.replicate.broking.MessageListener;
import org.kubithon.replicate.replication.ReplicationManager;

import java.util.Base64;

/**
 * Listens for replications.
 *
 * @author Oscar Davis
 * @since 1.0.0
 */
public class ReplicationListener implements MessageListener {

    private ReplicationManager replicationManager;

    public ReplicationListener() {
        this.replicationManager = ReplicatePlugin.get().getReplicationManager();
    }


    @Override
    public void patternReceive(String pattern, String topic, String message) {
        byte[] bytes = Base64.getDecoder().decode(message);
        ByteBuf buf = null;
        try {
            buf = Unpooled.wrappedBuffer(bytes);
            PacketDataSerializer serializer = new PacketDataSerializer(buf);
            Packet<?> packet = EnumProtocol.PLAY.a(EnumProtocolDirection.SERVERBOUND, serializer.g());

            /**
             * Checking if the packet is null before calling "a". To please to sonar.
             */
            if (packet != null) {
                packet.a(serializer);
                String pName = topic.substring(BrokingConstant.REPLICATION_TOPIC.length(), BrokingConstant.REPLICATION_TOPIC.length() + 36);
                String pUuid = topic.substring(BrokingConstant.REPLICATION_TOPIC.length() + 36);
                replicationManager.getPacketReader().readPacket(packet, pName, pUuid);
            } else
                ReplicatePlugin.get().getLogger().severe("Could not read incoming packet");

        } catch (Exception e) {
            ReplicatePlugin.get().getLogger().severe("Could not read incoming packet: " + ExceptionUtils
                    .getFullStackTrace(e));
        } finally {
            if (buf != null)
                buf.release();
        }
    }
}
