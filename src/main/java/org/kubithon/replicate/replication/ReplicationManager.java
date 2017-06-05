package org.kubithon.replicate.replication;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
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

    public void unregisterReplicatedPlayer(String name) {
        if (replicatedPlayers.containsKey(name)) {
            ReplicatedPlayer player = replicatedPlayers.get(name);
            player.dispawn(Bukkit.getOnlinePlayers());
        }
    }

    // TODO : disconnection handling
    public void handleKubicket(String playerName, KubithonPacket receivedPacket) {
        Log.info("Handling a kubicket. Player: " + playerName);

        if (receivedPacket instanceof PlayerLookKubicket) {
            PlayerLookKubicket lookKubicket = (PlayerLookKubicket) receivedPacket;
            replicatedPlayers.get(playerName).updateLook(
                    lookKubicket.getPitch(),
                    lookKubicket.getYaw()
            );
        } else if (receivedPacket instanceof PlayerPositionKubicket) {
            PlayerPositionKubicket positionKubicket = (PlayerPositionKubicket) receivedPacket;
            replicatedPlayers.get(playerName).teleport(
                    positionKubicket.getxPos(),
                    positionKubicket.getyPos(),
                    positionKubicket.getzPos()
            );
        } else if (receivedPacket instanceof PlayerPositionLookKubicket) {
            PlayerPositionLookKubicket positionLookKubicket = (PlayerPositionLookKubicket) receivedPacket;
            replicatedPlayers.get(playerName).teleport(
                    positionLookKubicket.getxPos(),
                    positionLookKubicket.getyPos(),
                    positionLookKubicket.getzPos(),
                    positionLookKubicket.getPitch(),
                    positionLookKubicket.getYaw()
            );
        } else if (receivedPacket instanceof PlayerConnectionKubicket) {
            handleConnectionPacket((PlayerConnectionKubicket) receivedPacket);
        }
    }

    private void handleConnectionPacket(PlayerConnectionKubicket receivedPacket) {
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