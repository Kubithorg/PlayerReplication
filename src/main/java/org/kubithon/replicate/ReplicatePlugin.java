package org.kubithon.replicate;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.PermissionData;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.kubithon.replicate.broking.BrokingConstant;
import org.kubithon.replicate.broking.PubSubManager;
import org.kubithon.replicate.broking.impl.ReplicationListener;
import org.kubithon.replicate.broking.impl.redis.RedisCredentials;
import org.kubithon.replicate.broking.jedis.JedisPubSubManager;
import org.kubithon.replicate.listener.ConnectionListener;
import org.kubithon.replicate.listener.InventoryClickListener;
import org.kubithon.replicate.listener.ItemHeldChangedListener;
import org.kubithon.replicate.replication.ReplicationManager;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
public class ReplicatePlugin extends JavaPlugin {

    private static ReplicatePlugin instance;

    private final File configFile = new File(getDataFolder(), "replication-config.yml");
    private LuckPermsApi permissionApi;

    private PubSubManager<RedisCredentials> jedisBroker = new JedisPubSubManager();
    private ReplicationManager replicationManager;
    private int serverId;
    private boolean debug;
    private String permissionEnabledKey;

    public ReplicatePlugin() {
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!checkConfigFile())
            return;

        replicationManager = new ReplicationManager();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        getLogger().info("REDIS CREDENTIALS -------------------------");
        getLogger().info("HOST IS " + config.getString("redis-host"));
        getLogger().info("PORT IS " + config.getString("redis-port"));
        getLogger().info("PASSWORD IS " + config.getString("redis-pass"));
        getLogger().info("------------------------------------------");

        serverId = config.getInt("server-uid");
        getLogger().info("THE SERVER UNIQUE ID IS " + serverId);
        debug = config.getInt("debug") == 1;
        getLogger().info("DEBUG IS " + (debug ? "ENABLED" : "DISABLED"));
        permissionEnabledKey = config.getString("replication-enabled-permission-name");
        getLogger().info("PERMISSION NAME OF REPLICATION IS " + permissionEnabledKey);


        RedisCredentials redisCredentials = new RedisCredentials(
                config.getString("redis-host"),
                config.getInt("redis-port"),
                config.getString("redis-pass"));

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

        registerListeners(new ConnectionListener(), new InventoryClickListener(), new ItemHeldChangedListener());
        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info("Try to load the LuckPerms API...");
            try {
                permissionApi = LuckPerms.getApi();
                getLogger().info("Success!");
            } catch (Exception ex) {
                getLogger().info("Error!");
                getLogger().severe(ExceptionUtils.getFullStackTrace(ex));
            }
        }, 20);
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
        if (!configFile.exists()) {
            getLogger().log(Level.SEVERE, "Could not find \"" + configFile.getName() + "\"");
            try {
                getLogger().log(Level.SEVERE, "Creating the \"" + configFile.getName() + "\" file.");
                if (!getDataFolder().exists() && !getDataFolder().mkdir()) {
                    getLogger().log(Level.SEVERE, "Unable to create data directory.");
                    getLogger().log(Level.SEVERE, "Disabling the plugin. ");
                    getServer().getPluginManager().disablePlugin(this);
                    return false;
                }
                if (configFile.createNewFile()) {
                    PrintWriter out = new PrintWriter(configFile);
                    out.println("redis-host: \"\"");
                    out.println("redis-port: 3360");
                    out.println("redis-user: \"\"");
                    out.println("redis-pass: \"\"");
                    getLogger().log(Level.INFO, "Credentials file created.");
                    out.flush();
                    out.close();
                } else {
                    getLogger().log(Level.SEVERE, "Unable to create the \"" + configFile.getName() + "\" file.");
                }
            } catch (java.io.IOException e) {
                getLogger().log(Level.SEVERE, "Unable to create the \"" + configFile.getName() + "\" config file." + ExceptionUtils.getFullStackTrace(e));
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
     * @return the single instance of the ReplicatePlugin class.
     */
    public static ReplicatePlugin get() {
        return instance;
    }

    /**
     * @return Returns the LuckPerms API, used for managing the permissions.
     */
    public LuckPermsApi getPermissionApi() {
        return permissionApi;
    }

    /**
     * @return Returns that the specified player should be replicated.
     */
    public boolean shouldBeReplicated(Player player) {
        if (debug) // To debug, enable the replication of OP players
            return player.hasPermission("kubithon.replicate") || player.isOp();
        else {
            Optional<User> user = permissionApi.getUserSafe(player.getUniqueId());
            if (!user.isPresent()) {
                permissionApi.getStorage().loadUser(player.getUniqueId());
                user = permissionApi.getUserSafe(player.getUniqueId());
            }
            // Here the user's value cannot be null
            Contexts contexts = permissionApi.getContextForUser(user.get()).orElse(null);
            if (contexts == null)
                return false;

            PermissionData permissionData = user.get().getCachedData().getPermissionData(contexts);
            Map<String, Boolean> permissionsMap = permissionData.getImmutableBacking();
            return permissionsMap.get(permissionEnabledKey);
        }
    }

}