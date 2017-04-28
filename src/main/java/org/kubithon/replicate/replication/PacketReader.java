package org.kubithon.replicate.replication;

import net.minecraft.server.v1_9_R2.EnumHand;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayInArmAnimation;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class PacketReader {

    private ReplicationManager manager;

    public PacketReader(ReplicationManager manager) {
        this.manager = manager;
    }

    public void readPacket(Packet<?> packet, String playerUuid, String playerName) {
        System.out.println("Received a packet - Player : " + playerName);
        System.out.println("                  - UUID : " + playerUuid);
        if (packet instanceof PacketPlayInArmAnimation)
            readPlayerInArmAnimation((PacketPlayInArmAnimation) packet);
    }

    private void readPlayerInArmAnimation(PacketPlayInArmAnimation packet) {
        EnumHand animation = packet.a();
        System.out.println("received a fucking arm animation packet : " + animation.toString());
    }

}