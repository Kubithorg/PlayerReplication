package org.kubithon.replicate;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.kubithon.replicate.broking.BrokingConstant;
import org.kubithon.replicate.broking.PubSubManager;
import org.kubithon.replicate.broking.impl.ReplicationListener;
import org.kubithon.replicate.broking.impl.redis.RedisCredentials;
import org.kubithon.replicate.broking.jedis.JedisPubSubManager;
import org.kubithon.replicate.listener.ConnectionListener;
import org.kubithon.replicate.listener.InventoryClickListener;
import org.kubithon.replicate.replication.ReplicationManager;

import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Level;

/**
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public class ReplicatePlugin extends JavaPlugin {

    private static ReplicatePlugin instance;

    private final File credentialsFile = new File(getDataFolder(), "credentials.yml");

    private PubSubManager<RedisCredentials> jedisBroker = new JedisPubSubManager();
    private ReplicationManager replicationManager;
    private int serverId;

    public ReplicatePlugin() {
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!checkConfigFile())
            return;

        replicationManager = new ReplicationManager();

        YamlConfiguration credentials = YamlConfiguration.loadConfiguration(credentialsFile);

        getLogger().info("REDIS CREDENTIALS -------------------------");
        getLogger().info("HOST IS " + credentials.getString("redis-host"));
        getLogger().info("HOST IS " + credentials.getString("redis-port"));
        getLogger().info("PASSWORD IS " + credentials.getString("redis-pass"));
        getLogger().info("------------------------------------------");

        serverId = credentials.getInt("server-uid");
        getLogger().info("THE SERVER UNIQUE ID IS " + serverId);

        RedisCredentials redisCredentials = new RedisCredentials(
                credentials.getString("redis-host"),
                credentials.getInt("redis-port"),
                credentials.getString("redis-pass"));

        try {
            getLogger().info("Attempting to connect to Redis...");
            connectToRedis(redisCredentials);
            getLogger().info("Attempting to subscribe to the pattern :..." + BrokingConstant.REPLICATION_PATTERN.concat("*"));
            jedisBroker.psubscribe(BrokingConstant.REPLICATION_PATTERN.concat("*"), BrokingConstant.REPLICATION_TOPIC, new ReplicationListener());
            getLogger().info(
                    "Successfully subscribed to the pattern :'" + BrokingConstant.REPLICATION_PATTERN.concat("*") + "'"
                            + " on the topic '" + BrokingConstant.REPLICATION_TOPIC + "'.");
        } catch (Exception ex) {
            getLogger().severe(ExceptionUtils.getFullStackTrace(ex));
        }

        registerListeners(new ConnectionListener(), new InventoryClickListener());
    }

    private void connectToRedis(RedisCredentials credentials) {
        boolean connectionSuccess = false;
        try {
            jedisBroker.connect(credentials);
            connectionSuccess = true;
        } catch (Exception ex) {
            Log.error(ExceptionUtils.getFullStackTrace(ex));
        }
        if (connectionSuccess)
            getLogger().info("Successfully connected to Redis!");
    }

    /**
     * Check the existence of the YAML configuration file. If it doesn't exist, try to create the data directory of
     * this plugin and the YAML configuration file, with the default password (empty string), the default username
     * (empty string) and the default port (3360). <br/>
     * If the configuration file exists because of the creation of the file by this function during the current
     * execution of the plugin (the function created it), then it will return false and ask the user to fill it
     * correctly before restarting the server.
     *
     * @return Return that the YAML configuration file exists.
     */
    private boolean checkConfigFile() {
        if (!credentialsFile.exists()) {
            getLogger().log(Level.SEVERE, "Could not find \"" + credentialsFile.getName() + "\"");
            try {
                getLogger().log(Level.SEVERE, "Creating the \"" + credentialsFile.getName() + "\" file.");
                if (!getDataFolder().exists() && !getDataFolder().mkdir()) {
                    getLogger().log(Level.SEVERE, "Unable to create data directory.");
                    getLogger().log(Level.SEVERE, "Disabling the plugin. ");
                    getServer().getPluginManager().disablePlugin(this);
                    return false;
                }
                if (credentialsFile.createNewFile()) {
                    PrintWriter out = new PrintWriter(credentialsFile);
                    out.println("redis-host: \"\"");
                    out.println("redis-port: 3360");
                    out.println("redis-user: \"\"");
                    out.println("redis-pass: \"\"");
                    getLogger().log(Level.INFO, "Credentials file created.");
                    out.flush();
                    out.close();
                } else {
                    getLogger().log(Level.SEVERE, "Unable to create the \"" + credentialsFile.getName() + "\" file.");
                }
            } catch (java.io.IOException e) {
                getLogger().log(Level.SEVERE, "Unable to create the \"" + credentialsFile.getName() + "\" config file." + ExceptionUtils.getFullStackTrace(e));
            }
            getLogger().log(Level.SEVERE, "Disabling the plugin. ");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        getLogger().log(Level.INFO, "Credentials file found!");
        return true;
    }

    @Override
    public void onDisable() {
        jedisBroker.disconnect();
    }

    /**
     * Returns the message broker implementation.
     *
     * @return the message broker implementation.
     */
    public PubSubManager getMessageBroker() {
        return jedisBroker;
    }

    /**
     * @return Returns the replication manager.
     */
    public ReplicationManager getReplicationManager() {
        return replicationManager;
    }


    /**
     * Registers all the given listeners.
     *
     * @param listeners the listeners to register.
     */
    private void registerListeners(Listener... listeners) {
        for (Listener l : listeners)
            getServer().getPluginManager().registerEvents(l, this);
    }

    /**
     * @return Returns the unique id of this server in the infrastructure. This is used to retrieve the sender of a
     * packet over the Redis pub/sub system.
     */
    public int getServerId() {
        return serverId;
    }

    /**
     * Returns the single instance of the ReplicatePlugin class.
     *
     * @return the single instance of the ReplicatePlugin class.
     */
    public static ReplicatePlugin get() {
        return instance;
    }

}
