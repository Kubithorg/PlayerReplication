package org.kubithon.replicate.broking.impl;

import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.BrokingConstant;
import org.kubithon.replicate.broking.MessageListener;
import org.kubithon.replicate.replication.ReplicationManager;
import org.kubithon.replicate.replication.protocol.KubithonPacket;

import java.util.Base64;

/**
 * Listens for replications.
 *
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public class ReplicationListener implements MessageListener {

    private ReplicationManager replicationManager;

    public ReplicationListener() {
        this.replicationManager = ReplicatePlugin.get().getReplicationManager();
    }


    @Override
    public void patternReceive(String pattern, String topic, String message) {
        int patternLength = BrokingConstant.REPLICATION_PATTERN.length();

        String playerName = topic.substring(patternLength);
        Log.info("The name of the player is " + playerName);
        byte[] bytes = Base64.getDecoder().decode(message);

        KubithonPacket receivedContainer = KubithonPacket.deserialize(bytes);
        replicationManager.handleKubicket(playerName, receivedContainer);
    }
}
