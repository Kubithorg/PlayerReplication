package fr.troopy28.replication.broking.impl;

import fr.troopy28.replication.ReplicationMod;
import fr.troopy28.replication.broking.BrokingConstant;
import fr.troopy28.replication.broking.MessageListener;
import fr.troopy28.replication.replication.ReplicationManager;
import fr.troopy28.replication.replication.protocol.KubithonPacket;

import java.util.Base64;

/**
 * Listens for replications.
 *
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public class ReplicationListener implements MessageListener {

    private ReplicationManager replicationManager;
    private ReplicationMod replicationMod;
    private int patternLength = BrokingConstant.REPLICATION_PATTERN.length();

    public ReplicationListener() {
        this.replicationMod = ReplicationMod.get();
        this.replicationManager = replicationMod.getReplicationManager();
    }

    @Override
    public void patternReceive(String pattern, String topic, String message) {
        int senderUid = Integer.parseInt(topic.substring(patternLength, patternLength + 1));

        String playerName = topic.substring(patternLength + 1);
        if (senderUid != ReplicationMod.get().getServerId()) {
            byte[] bytes = Base64.getDecoder().decode(message);

            KubithonPacket receivedKubicket = KubithonPacket.deserialize(bytes);
            if (receivedKubicket != null) {
                replicationManager.handleKubicket(playerName, receivedKubicket);
            }
            else {
                replicationMod.getLogger().info("Unknown kubicket.");
            }
        }
    }
}
