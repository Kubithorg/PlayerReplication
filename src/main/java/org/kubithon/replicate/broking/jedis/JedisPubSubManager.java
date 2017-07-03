package org.kubithon.replicate.broking.jedis;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.MessageListener;
import org.kubithon.replicate.broking.PubSubManager;
import org.kubithon.replicate.broking.impl.AbstractPubSubManager;
import org.kubithon.replicate.broking.impl.redis.RedisCredentials;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class JedisPubSubManager extends AbstractPubSubManager<RedisCredentials> implements PubSubManager<RedisCredentials> {

    private Jedis publisherJedis;
    private Jedis subscriberJedis;
    private JedisPubSub pubSub;

    public JedisPubSubManager() {
        super();
    }

    @Override
    public void connect(RedisCredentials credentials) {
        JedisPool pool = new JedisPool(new JedisPoolConfig(), credentials.host(),
                credentials.port(), 2000);
        this.publisherJedis = pool.getResource();
        this.subscriberJedis = pool.getResource();
        String redisPassword = credentials.password();
        if (redisPassword != null) {
            try {
                publisherJedis.auth(redisPassword);
                subscriberJedis.auth(redisPassword);
            } catch (Exception ex) {
                String fullStackTrace = ExceptionUtils.getFullStackTrace(ex);
                if (fullStackTrace.contains("but no password is set"))
                    Log.error("Error while connecting to the Redis Server: " + ex.getMessage());
                else
                    Log.error(fullStackTrace);
            }
        }
        this.pubSub = createPubSub();
        pool.close();
    }

    @Override
    public void publish(String topic, String message) {
        Log.info("Publishing in topic '" + topic + "' the message '" + message + "'.");
        publisherJedis.publish(topic, message);
    }

    @Override
    public void subscribe(String topic, MessageListener listener) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                ReplicatePlugin.get(),
                () -> subscriberJedis.subscribe(pubSub, topic),
                1);
        super.subscribe(topic, listener);
    }

    @Override
    public void psubscribe(String pattern, String topic, MessageListener listener) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                ReplicatePlugin.get(),
                () -> subscriberJedis.psubscribe(pubSub, pattern),
                1);
        super.psubscribe(pattern, topic, listener);
    }

    @Override
    public void disconnect() {
        if (publisherJedis != null && publisherJedis.isConnected())
            publisherJedis.close();
        if (subscriberJedis != null && subscriberJedis.isConnected())
            subscriberJedis.close();
    }

    private JedisPubSub createPubSub() {
        return new JedisPubSub() {
            @Override
            public void onMessage(String topic, String message) {
                //TODO : remove this debug message
                ReplicatePlugin.get().getLogger().info("A non-pattern message has been received and will not be processed.");
            }

            @Override
            public void onPMessage(String pattern, String topic, String message) {
                callListeners(pattern, topic, message);
            }
        };
    }
}