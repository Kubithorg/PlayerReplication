package fr.troopy28.replication.broking.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.troopy28.replication.broking.Credentials;
import fr.troopy28.replication.broking.MessageListener;
import fr.troopy28.replication.broking.PubSubManager;

/**
 * @param <T> Credentials specific to the message broker.
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public abstract class AbstractPubSubManager<T extends Credentials> implements PubSubManager<T> {
    /**
     * Key: the topic.
     * Value: the listener.
     */
    private Multimap<String, MessageListener> listeners = HashMultimap.create();

    @Override
    public void subscribe(String topic, MessageListener listener) {
        listeners.put(topic, listener);
    }

    @Override
    public void psubscribe(String pattern, String topic, MessageListener listener) {
        listeners.put(topic, listener);
    }

    protected final void callListeners(String pattern, String topic, String message) {
        listeners.values().stream()
                .filter(listener -> listener != null)
                .forEach(listener -> listener.patternReceive(pattern, topic, message));
    }
}
