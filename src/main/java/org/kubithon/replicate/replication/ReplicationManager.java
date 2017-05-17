package org.kubithon.replicate.replication;

import org.bukkit.entity.Player;
import org.kubithon.replicate.replication.npc.ReplicatedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicationManager {

    private PacketReader packetReader;
    /**
     * The list of the NPCs that are currently displayed on the server.
     */
    private List<ReplicatedPlayer> replicatedPlayers;
    /**
     * The connected players that are going to receive the packets from the NPCs.
     */
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
