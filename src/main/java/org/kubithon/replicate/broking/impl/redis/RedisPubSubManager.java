package org.kubithon.replicate.broking.impl.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.MessageListener;
import org.kubithon.replicate.broking.impl.AbstractPubSubManager;

import java.util.logging.Level;

/**
 * @author Oscar Davis
 * @since 1.0.0
 */
public class RedisPubSubManager extends AbstractPubSubManager<RedisCredentials> {

    private RedisClient client;
    private StatefulRedisPubSubConnection<String, String> connection;

    @Override
    public void connect(RedisCredentials credentials) {
        client = RedisClient.create(RedisURI.builder()
                .withHost(credentials.host())
                .withPort(credentials.port())
                .withPassword(credentials.password())
                .build());
        connection = client.connectPubSub();
        connection.async().addListener(new RedisListener());
    }

    @Override
    public void publish(String topic, String message) {
        connection.async().publish(topic, message);
    }

    @Override
    public void subscribe(String topic, MessageListener listener) {
        try {
            connection.sync().subscribe(topic);
        } catch (Exception e) {
            ReplicatePlugin.get().getLogger()
                    .log(Level.SEVERE, "Encountered an exception during subscription to topic \""
                            + topic + "\": " + ExceptionUtils.getFullStackTrace(e));
            return;
        }
        super.subscribe(topic, listener);
    }

    @Override
    public void psubscribe(String pattern, String topic, MessageListener listener) {
        try {
            connection.sync().psubscribe(pattern);
        } catch (Exception e) {
            ReplicatePlugin.get().getLogger()
                    .log(Level.SEVERE, "Encountered an exception during subscription to pattern \""
                            + topic + "\": " + ExceptionUtils.getFullStackTrace(e));
            return;
        }
        super.psubscribe(pattern, topic, listener);
    }

    @Override
    public void disconnect() {
        if (connection != null && connection.isOpen())
            connection.close();
        if (client != null)
            client.shutdown();
    }

    private class RedisListener implements RedisPubSubListener<String, String> {
        @Override
        public void message(String topic, String message) {
            RedisPubSubManager.this.callTopicListeners(topic, message);
        }

        @Override
        public void message(String pattern, String topic, String message) {
            RedisPubSubManager.this.callPatternListeners(pattern, topic, message);
        }

        @Override
        public void subscribed(String s, long l) {
            // unused.
        }

        @Override
        public void psubscribed(String s, long l) {
            // unused.
        }

        @Override
        public void unsubscribed(String s, long l) {
            // unused.
        }

        @Override
        public void punsubscribed(String s, long l) {
            // unused.
        }
    }

}
