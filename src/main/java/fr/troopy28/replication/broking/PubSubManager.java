package fr.troopy28.replication.broking;

/**
 * Manages a pub sub system.
 *
 * @param <T> Credentials specific to the pub sub manager.
 * @author Oscar Davis
 * @since 1.0.0
 */
public interface PubSubManager<T extends Credentials>
{

    /**
     * Connects the message broker.
     *
     * @param credentials the credentials.
     */
    void connect(T credentials);

    /**
     * Publishes the given message to the message broker.
     *
     * @param topic   messages's topic.
     * @param message the message.
     */
    void publish(String topic, String message);

    /**
     * Subscribes the given {@link MessageListener} to the given topic.
     *
     * @param topic    the topic to subscribe to.
     * @param listener the message listener (handler).
     */
    void subscribe(String topic, MessageListener listener);

    /**
     * Subscribes the given {@link MessageListener} to the given pattern.
     *
     * @param pattern  the pattern to subscribe to.
     * @param listener the message listener to register.
     */
    void psubscribe(String pattern, String topic, MessageListener listener);

    /**
     * Disconnects the broker if it has not been done yet.
     */
    void disconnect();

    /**
     * @return Returns the ID this server should take, and increments the value of the ID key of the REDIS.
     */
    int queryServerId();
}
