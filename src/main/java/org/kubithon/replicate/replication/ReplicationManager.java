package org.kubithon.replicate.replication;

import org.bukkit.entity.Player;
import org.kubithon.replicate.replication.npc.ReplicatedPlayer;
import org.kubithon.replicate.replication.protocol.*;

import java.util.*;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicationManager {

    /**
     * The map(pseudo, NPC) of the NPCs that are currently displayed on the server.
     */
    private Map<String, ReplicatedPlayer> replicatedPlayers;
    /**
     * The connected players that are going to receive the packets from the NPCs.
     */
    private List<Player> targets;

    public ReplicationManager() {
        replicatedPlayers = new HashMap<>();
        targets = new ArrayList<>();
    }

    /**
     * Adds a new player to the list of the NPCs to show.
     *
     * @param uuid The UUID of the player to replicate.
     * @param name The nickname of the player to replicate.
     */
    public void registerReplicatedPlayer(UUID uuid, String name) {
        ReplicatedPlayer replicatedPlayer = new ReplicatedPlayer(uuid, name);
        replicatedPlayers.put(name, replicatedPlayer);
    }
    // TODO : disconnection handling
    public void handleKubicket(String playerName, KubicketContainer receivedContainer) {
        switch (receivedContainer.getPacketType()) {
            case PLAYER_CONNECTION:
                PlayerConnectionKubicket conKbckt = receivedContainer.extractPacket();
                registerReplicatedPlayer(UUID.fromString(conKbckt.playerUuid), conKbckt.playerName);
                break;
            case PLAYER_LOOK:
                PlayerLookKubicket lookKbckt = receivedContainer.extractPacket();
                replicatedPlayers.get(playerName).updateLook(lookKbckt.pitch, lookKbckt.yaw);
                break;
            case PLAYER_POSITION:
                PlayerPositionKubicket posKbckt = receivedContainer.extractPacket();
                replicatedPlayers.get(playerName).teleport(posKbckt.xPos, posKbckt.yPos, posKbckt.zPos);
                break;
            case PLAYER_POSITION_LOOK:
                PlayerPositionLookKubicket posLookKbckt = receivedContainer.extractPacket();
                replicatedPlayers.get(playerName).teleport(posLookKbckt.xPos, posLookKbckt.yPos, posLookKbckt.zPos, posLookKbckt.pitch, posLookKbckt.yaw);
                break;
            default:
                break;
        }
    }
}