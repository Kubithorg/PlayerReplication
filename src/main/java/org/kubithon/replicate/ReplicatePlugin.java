package org.kubithon.replicate;

import org.kubithon.replicate.broking.BrokingConstant;
import org.kubithon.replicate.broking.PubSubManager;
import org.kubithon.replicate.broking.impl.ReplicationListener;
import org.kubithon.replicate.broking.impl.redis.RedisPubSubManager;
import org.kubithon.replicate.broking.impl.redis.RedisCredentials;
import org.kubithon.replicate.listener.ConnectionListener;
import java.io.File;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Oscar Davis
 * @since 1.0.0
 */
public class ReplicatePlugin extends JavaPlugin
{

    private static ReplicatePlugin instance;

    private final File credentialsFile = new File(getDataFolder(), "credentials.yml");

    private PubSubManager<RedisCredentials> broker = new RedisPubSubManager();

    public ReplicatePlugin()
    {
        instance = this;
    }

    @Override
    public void onEnable()
    {
        if (!credentialsFile.exists())
        {
            getLogger().log(Level.SEVERE, "Could not find \"" + credentialsFile.getName() + "\". Disabling the plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        YamlConfiguration credentials = YamlConfiguration.loadConfiguration(credentialsFile);

        RedisCredentials connect = new RedisCredentials(credentials.getString("redis-host"),
                credentials.getInt("redis-port"),
                credentials.getString("redis-password)"));
        broker.connect(connect);
        broker.subscribe(BrokingConstant.REPLICATION_TOPIC.concat("*"), new ReplicationListener());

        registerListeners(new ConnectionListener());
    }

    @Override
    public void onDisable()
    {
        broker.disconnect();
    }

    /**
     * Returns the message broker implementation.
     *
     * @return the message broker implementation.
     */
    public PubSubManager getMessageBroker()
    {
        return broker;
    }

    /**
     * Registers all the given listeners.
     *
     * @param listeners the listeners to register.
     */
    private void registerListeners(Listener... listeners)
    {
        for (Listener l : listeners)
            getServer().getPluginManager().registerEvents(l, this);
    }

    /**
     * Returns the single instance of the ReplicatePlugin class.
     *
     * @return the single instance of the ReplicatePlugin class.
     */
    public static ReplicatePlugin get()
    {
        return instance;
    }

}
