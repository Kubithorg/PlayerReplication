package org.kubithon.replicate.replication;

import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.kubithon.replicate.replication.npc.ReplicatedPlayer;
import org.kubithon.replicate.replication.protocol.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicationManager {

    /**
     * The map(pseudo, NPC) of the NPCs that are currently displayed on the server.
     */
    private Map<String, ReplicatedPlayer> replicatedPlayers;

    public ReplicationManager() {
        replicatedPlayers = new HashMap<>();
    }

    /**
     * Adds a new player to the list of the NPCs to show.
     *
     * @param uuid The UUID of the player to replicate.
     * @param name The nickname of the player to replicate.
     */
    private void registerReplicatedPlayer(UUID uuid, String name) {
        ReplicatedPlayer replicatedPlayer = new ReplicatedPlayer(uuid, name);
        replicatedPlayers.put(name, replicatedPlayer);
    }

    private void unregisterReplicatedPlayer(String name) {
        if (replicatedPlayers.containsKey(name)) {
            ReplicatedPlayer player = replicatedPlayers.get(name);
            player.destroy();
        }
    }

    public void handleKubicket(String playerName, KubithonPacket receivedKubicket) {
        Log.info("Handling a kubicket. Player: " + playerName + " | PacketType: " + receivedKubicket.getType());

        if (receivedKubicket instanceof PlayerLookKubicket) {
            PlayerLookKubicket lookKubicket = (PlayerLookKubicket) receivedKubicket;
            replicatedPlayers.get(playerName).updateLook(
                    lookKubicket.getPitch(),
                    lookKubicket.getYaw()
            );
        } else if (receivedKubicket instanceof PlayerPositionKubicket) {
            PlayerPositionKubicket positionKubicket = (PlayerPositionKubicket) receivedKubicket;
            replicatedPlayers.get(playerName).teleport(
                    positionKubicket.getxPos(),
                    positionKubicket.getyPos(),
                    positionKubicket.getzPos(),
                    positionKubicket.isOnGround()
            );
        } else if (receivedKubicket instanceof PlayerPositionLookKubicket) {
            PlayerPositionLookKubicket positionLookKubicket = (PlayerPositionLookKubicket) receivedKubicket;
            replicatedPlayers.get(playerName).teleport(
                    positionLookKubicket.getxPos(),
                    positionLookKubicket.getyPos(),
                    positionLookKubicket.getzPos(),
                    positionLookKubicket.getPitch(),
                    positionLookKubicket.getYaw(),
                    positionLookKubicket.isOnGround()
            );
        } else if (receivedKubicket instanceof PlayerConnectionKubicket) {
            handleConnectionKubicket((PlayerConnectionKubicket) receivedKubicket);
        } else if (receivedKubicket instanceof PlayerHandAnimationKubicket) {
            PlayerHandAnimationKubicket handAnimationKubicket = (PlayerHandAnimationKubicket) receivedKubicket;
            replicatedPlayers.get(playerName).moveArm(handAnimationKubicket.getHand());
        }
    }

    private void handleConnectionKubicket(PlayerConnectionKubicket receivedPacket) {
        if (receivedPacket.getState() == 0) {
            registerReplicatedPlayer(
                    UUID.fromString(receivedPacket.getPlayerUuid()),
                    receivedPacket.getPlayerName()
            );
        } else if (receivedPacket.getState() == 1) {
            unregisterReplicatedPlayer(receivedPacket.getPlayerName());
        }
    }
}