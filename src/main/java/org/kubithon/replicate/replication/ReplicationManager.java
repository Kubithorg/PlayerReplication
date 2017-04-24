package org.kubithon.replicate.replication;

import org.bukkit.entity.Player;
import org.kubithon.replicate.replication.npc.ReplicatedPlayer;

import java.util.*;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicationManager {

    private PacketReader packetReader;
    private List<ReplicatedPlayer> replicatedPlayers;
    private List<Player> targets;

    public ReplicationManager() {
        replicatedPlayers = new ArrayList<>();
        targets = new ArrayList<>();
        packetReader = new PacketReader(this);
    }

    public void registerReplicatedPlayer(UUID uuid, String name) {
        ReplicatedPlayer replicatedPlayer = new ReplicatedPlayer(uuid, name);
        replicatedPlayers.add(replicatedPlayer);
    }

    public PacketReader getPacketReader() {
        return packetReader;
    }

    public void sendReplicatedToNewPlayers() {

    }

}
