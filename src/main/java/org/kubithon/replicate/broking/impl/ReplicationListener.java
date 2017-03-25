package org.kubithon.replicate.broking.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Base64;
import net.minecraft.server.v1_9_R2.EnumProtocol;
import net.minecraft.server.v1_9_R2.EnumProtocolDirection;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketDataSerializer;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.MessageListener;

/**
 * Listens for replications.
 *
 * @author Oscar Davis
 * @since 1.0.0
 */
public class ReplicationListener implements MessageListener
{

    @Override
    public void patternReceive(String pattern, String topic, String message)
    {
        byte[] bytes = Base64.getDecoder().decode(message);
        ByteBuf buf = null;
        try
        {
            buf = Unpooled.wrappedBuffer(bytes);
            PacketDataSerializer serializer = new PacketDataSerializer(buf);
            Packet<?> packet = EnumProtocol.PLAY.a(EnumProtocolDirection.SERVERBOUND, serializer.g());
            packet.a(serializer);
        }
        catch (Exception e)
        {
            ReplicatePlugin.get().getLogger().severe("Could not read incoming packet: " + ExceptionUtils
                    .getFullStackTrace(e));
        }
        finally
        {
            if (buf != null)
                buf.release();
        }
    }

}
