package org.kubithon.replicate.broking.impl;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.MessageListener;
import org.kubithon.replicate.replication.ReplicationManager;
import org.kubithon.replicate.replication.protocol.KubicketContainer;

import java.io.IOException;
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
        byte[] bytes = Base64.getDecoder().decode(message);
        try {
            KubicketContainer receivedContainer = KubicketContainer.deserialize(bytes);
            replicationManager.handleKubicket(receivedContainer);
        } catch (IOException e) {
            ReplicatePlugin.get().getLogger().severe(ExceptionUtils.getFullStackTrace(e));
        }
    }
}
