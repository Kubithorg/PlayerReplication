package org.kubithon.replicate.broking.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.stream.Stream;
import org.kubithon.replicate.broking.Credentials;
import org.kubithon.replicate.broking.PubSubManager;
import org.kubithon.replicate.broking.MessageListener;
import java.util.Collection;
import java.util.Objects;

/**
 * @param <T> Credentials specific to the message broker.
 * @author Oscar Davis
 * @since 1.0.0
 */
public abstract class AbstractPubSubManager<T extends Credentials> implements PubSubManager<T>
{

    protected Multimap<String, MessageListener> listeners = HashMultimap.create();

    @Override
    public void subscribe(String topic, MessageListener listener)
    {
        listeners.put(topic, listener);
    }

    @Override
    public void psubscribe(String pattern, String topic, MessageListener listener)
    {
        listeners.put(topic, listener);
    }


    protected final void callPatternListeners(String pattern, String topic, String message)
    {
        Stream<MessageListener> l = streamListeners(pattern);
        if (l != null)
            l.forEach(listener -> listener.patternReceive(pattern, topic, message));
    }

    /**
     * Calls the listeners subscribed to the given topic.
     *
     * @param topic   message's topic.
     * @param message the message.
     */
    protected final void callTopicListeners(String topic, String message)
    {
        Stream<MessageListener> l = streamListeners(topic);
        if (l != null)
            l.forEach(listener -> listener.topicReceive(topic, message));
    }

    private Stream<MessageListener> streamListeners(String index)
    {
        Collection<MessageListener> l = listeners.get(index);
        return l == null ? null : l.stream().filter(Objects::nonNull);
    }

}
