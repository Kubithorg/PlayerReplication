package fr.troopy28.replication;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.troopy28.replication.broking.BrokingConstant;
import fr.troopy28.replication.broking.PubSubManager;
import fr.troopy28.replication.broking.impl.ReplicationListener;
import fr.troopy28.replication.broking.impl.redis.RedisCredentials;
import fr.troopy28.replication.broking.jedis.JedisPubSubManager;
import fr.troopy28.replication.netty.ReplicateHandler;
import fr.troopy28.replication.replication.ReplicationManager;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.PermissionData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mod(modid = ReplicationMod.MODID, version = ReplicationMod.VERSION, serverSideOnly = true, acceptableRemoteVersions = "*")
// Server side only mod, accept both forge and no-forge versions
public class ReplicationMod {

    static final String MODID = "forge-replication";
    static final String VERSION = "1.0";
    private static ReplicationMod instance;


    private final File configFile = new File("replication-config.json");
    private LuckPermsApi permissionApi;

    private PubSubManager<RedisCredentials> jedisBroker;
    private ReplicationManager replicationManager;
    private ReplicationConfig config;
    private Logger logger;

    /**
     * Here is initialized everything that doesn't depends on any other plugin that might not be initialized yet (Redis).
     *
     * @param event Initialization event.
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger = LogManager.getLogger("PlayerReplication");
        instance = this;
        if (!checkConfigFile())
            return;

        replicationManager = new ReplicationManager();
        jedisBroker = new JedisPubSubManager();

        Gson gson = new Gson();
        try {
            config = gson.fromJson(new String(Files.readAllBytes(configFile.toPath())), ReplicationConfig.class);
        } catch (IOException e) {
            logger.error("An error occurred.");
            logger.trace(ExceptionUtils.getStackTrace(e));
            logger.error("Server will stop.");
        }

        logger.info("REDIS CREDENTIALS -------------------------");
        logger.info("HOST IS " + config.getRedisHost());
        logger.info("PORT IS " + config.getRedisPort());
        logger.info("PASSWORD IS " + config.getRedisPassword());
        logger.info("------------------------------------------");

        logger.info("THE SERVER UNIQUE ID IS " + config.getServerUuid());
        logger.info("DEBUG IS " + (config.isDebug() ? "ENABLED" : "DISABLED"));
        logger.info("PERMISSION NAME OF REPLICATION IS " + config.getReplicationPermissionName());

        RedisCredentials redisCredentials = new RedisCredentials(
                config.getRedisHost(),
                config.getRedisPort(),
                config.getRedisPassword());

        try {
            logger.info("Attempting to connect to Redis...");
            connectToRedis(redisCredentials);
            logger.info("Attempting to subscribe to the pattern :..." + BrokingConstant.REPLICATION_PATTERN.concat("*"));
            jedisBroker.psubscribe(BrokingConstant.REPLICATION_PATTERN.concat("*"), BrokingConstant.REPLICATION_TOPIC, new ReplicationListener());
            logger.info(
                    "Successfully subscribed to the pattern :'" + BrokingConstant.REPLICATION_PATTERN.concat("*") + "'"
                            + " on the topic '" + BrokingConstant.REPLICATION_TOPIC + "'.");
        } catch (Exception ex) {
            logger.trace(ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Here is initialized everything that might depend on an other mod or Sponge plugin (LuckPerms). Registers the
     * listeners.
     *
     * @param event Start event.
     */
    @EventHandler
    public void onStart(FMLServerStartedEvent event) {
        logger.info("Trying to load the LuckPerms API...");
        try {
            permissionApi = LuckPerms.getApi();
            logger.info("Success!");
        } catch (Exception ex) {
            logger.info("Error!");
            logger.trace(ExceptionUtils.getStackTrace(ex));
        }
        logger.info("Registering the listeners");
        MinecraftForge.EVENT_BUS.register(this);
        logger.info("Listeners registered in the event bus.");
    }

    // Player logs in
    @SubscribeEvent
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        logger.info(event.player.getName() + " joined us!");

        final EntityPlayerMP player = (EntityPlayerMP) event.player;
        //test(player);
        if (shouldBeReplicated(player)) {
            logger.info(player.getName() + " has the permission to be replicated.");
            ReplicateHandler.handle(player);
        }
    }

    private void test(EntityPlayerMP player) {
        GameProfile gameProfile = new GameProfile(UUID.fromString("6bd25c70-f838-494b-87fe-2311e95d16ef"), "goubidity");

        Property prop = player.getGameProfile().getProperties().get("textures").iterator().next();
        gameProfile.getProperties().put("textures", prop);

        EntityPlayerMP fakePlayer = new EntityPlayerMP(
                player.getServerWorld().getMinecraftServer(),
                player.getServerWorld(),
                gameProfile,
                new PlayerInteractionManager(player.getServerWorld())
        );

        fakePlayer.setPosition(player.posX, player.posY, player.posZ);
        player.getGameProfile().getProperties().get("textures").forEach(t -> logger.info(t.getName() + " = " + t.getValue()  + " ; " + t.getSignature()));

        SPacketPlayerListItem info = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, fakePlayer);
        player.connection.sendPacket(info);
        SPacketSpawnPlayer spawn = new SPacketSpawnPlayer(fakePlayer);
        player.connection.sendPacket(spawn);
        SPacketEntityTeleport pos = new SPacketEntityTeleport(fakePlayer);
        player.connection.sendPacket(pos);
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (shouldBeReplicated(event.player)) {
            ReplicateHandler.stopHandling((EntityPlayerMP) event.player);
        }
    }


    /**
     * @return Returns that the specified player should be replicated.
     */
    public boolean shouldBeReplicated(EntityPlayer player) {
        /*if (config.isDebug()) // To debug, enable the replication using sponge permissions
            return player.hasPermission("kubithon.replicate");
        else {*/
        Optional<User> user = permissionApi.getUserSafe(player.getUniqueID());
        if (!user.isPresent()) {
            permissionApi.getStorage().loadUser(player.getUniqueID());
            user = permissionApi.getUserSafe(player.getUniqueID());
        }
        // Here the user's value cannot be null
        Contexts contexts = permissionApi.getContextForUser(user.get()).orElse(null);
        if (contexts == null)
            return false;

        PermissionData permissionData = user.get().getCachedData().getPermissionData(contexts);
        Map<String, Boolean> permissionsMap = permissionData.getImmutableBacking();
        try {
            return permissionsMap.get(config.getReplicationPermissionName());
        } catch (Exception ex) {
            return false;
        }
        // }
    }

    private void connectToRedis(RedisCredentials credentials) {
        boolean connectionSuccess = false;
        try {
            jedisBroker.connect(credentials);
            connectionSuccess = true;
        } catch (Exception ex) {
            logger.error(ExceptionUtils.getStackTrace(ex));
        }
        if (connectionSuccess)
            logger.info("Successfully connected to Redis!");
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Check the existence of the JSON configuration file. If it doesn't exist, try to create  the JSON configuration
     * file, with the default password (empty string), the default username (empty string) and the default port (3360).
     * <br/>
     * If the configuration file exists because of the creation of the file by this function during the current
     * execution of the plugin (the function created it), then it will return false and ask the user to fill it
     * correctly before restarting the server.
     *
     * @return Return that the JSON configuration file exists.
     */
    private boolean checkConfigFile() {
        if (!configFile.exists()) {
            logger.error("Could not find \"" + configFile.getName() + "\"");
            try {
                logger.error("Creating the \"" + configFile.getName() + "\" file.");
                if (configFile.createNewFile()) {
                    config = new ReplicationConfig();
                    config.setDebug(false);
                    config.setRedisHost("REDIS-HOST");
                    config.setRedisPassword("REDIS-PASSWORD");
                    config.setRedisPort(3360);
                    config.setReplicationPermissionName("REPLICATION-PERMISSION-NAME");
                    config.setServerUuid(0);

                    Gson gson = new Gson();
                    Files.write(configFile.toPath(), gson.toJson(config, config.getClass()).getBytes());

                    logger.info("Credentials file created.");
                    logger.info("-------------------------------");
                    logger.info("PLEASE COMPLETE THE CONFIG FILE");
                    logger.info("-------------------------------");
                } else {
                    logger.error("Unable to create the \"" + configFile.getName() + "\" file.");
                }
            } catch (IOException e) {
                logger.error("Unable to create the \"" + configFile.getName() + "\" config file." + ExceptionUtils.getStackTrace(e));
            }
            logger.error("Shutting down the server.");
            //TODO: shutdown the server
            return false;
        }
        logger.info("Credentials file found!");
        return true;
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
     * @return Returns the unique id of this server in the infrastructure. This is used to retrieve the sender of a
     * packet over the Redis pub/sub system.
     */
    public int getServerId() {
        return config.getServerUuid();
    }

    public static ReplicationMod get() {
        return instance;
    }

}