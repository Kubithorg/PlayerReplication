package fr.troopy28.replication.broking.jedis;

import fr.troopy28.replication.ReplicationMod;
import fr.troopy28.replication.broking.MessageListener;
import fr.troopy28.replication.broking.PubSubManager;
import fr.troopy28.replication.broking.impl.AbstractPubSubManager;
import fr.troopy28.replication.broking.impl.redis.RedisCredentials;
import fr.troopy28.replication.utils.ForgeScheduler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class JedisPubSubManager extends AbstractPubSubManager<RedisCredentials> implements PubSubManager<RedisCredentials> {

    private static final String REDIS_SERVERID_KEY = "replication:current_server_id:integer";

    private Jedis publisherJedis;
    private Jedis subscriberJedis;
    private JedisPubSub pubSub;

    public JedisPubSubManager() {
        super();
    }

    @Override
    public void connect(RedisCredentials credentials) {
        this.publisherJedis = new Jedis(credentials.host(), credentials.port(), 2000); //NOSONAR: stupid...
        this.subscriberJedis = new Jedis(credentials.host(), credentials.port(), 2000); //NOSONAR: same...
        String redisPassword = credentials.password();
        if (redisPassword != null) {
            try {
                publisherJedis.auth(redisPassword);
                subscriberJedis.auth(redisPassword);
            } catch (Exception ex) {
                String fullStackTrace = ExceptionUtils.getStackTrace(ex);
                if (fullStackTrace.contains("but no password is set"))
                    ReplicationMod.get().getLogger().error("Error while connecting to the Redis Server: " + ex.getMessage());
                else
                    ReplicationMod.get().getLogger().error(fullStackTrace);
            }
        }
        this.pubSub = createPubSub();
    }

    @Override
    public void publish(String topic, String message) {
        publisherJedis.publish(topic, message);
    }

    @Override
    public void subscribe(String topic, MessageListener listener) {
        ForgeScheduler.runTaskLater(() -> subscriberJedis.subscribe(pubSub, topic), 1);
        super.subscribe(topic, listener);
    }

    @Override
    public void psubscribe(String pattern, String topic, MessageListener listener) {
        ForgeScheduler.runTaskLater(() -> subscriberJedis.psubscribe(pubSub, pattern), 1);
        super.psubscribe(pattern, topic, listener);
    }

    @Override
    public void disconnect() {
        if (publisherJedis != null && publisherJedis.isConnected())
            publisherJedis.close();
        if (subscriberJedis != null && subscriberJedis.isConnected())
            subscriberJedis.close();
    }

    @Override
    public int queryServerId() {
        if (!publisherJedis.exists(REDIS_SERVERID_KEY)) {
            // The key doesn't exist: create with value "1", and set the id of THIS instance to 0
            publisherJedis.set(REDIS_SERVERID_KEY, "1");
            ReplicationMod.get().getLogger().info("First instance: ID=0. Created the key " + REDIS_SERVERID_KEY + " and set its value to 1.");
            return 0;
        }
        String msg = "Instance #";
        // The key exists
        int serverID;
        String keyString = publisherJedis.get(REDIS_SERVERID_KEY);
        msg+=keyString;
        msg+=". Next instance ID will be ";
        int key = Integer.parseInt(keyString);
        serverID = key;
        key++;
        keyString = String.valueOf(key);
        msg+=keyString;
        publisherJedis.set(REDIS_SERVERID_KEY, keyString);
        ReplicationMod.get().getLogger().info(msg);
        return serverID;
    }

    private JedisPubSub createPubSub() {
        return new JedisPubSub() {
            @Override
            public void onMessage(String topic, String message) {
                ReplicationMod.get().getLogger().info("A non-pattern message has been received and will not be processed.");
            }

            @Override
            public void onPMessage(String pattern, String topic, String message) {
                callListeners(pattern, topic, message);
            }
        };
    }
}