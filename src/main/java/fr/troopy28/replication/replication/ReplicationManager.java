package fr.troopy28.replication.replication;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.troopy28.replication.ReplicationMod;
import fr.troopy28.replication.broking.BrokingConstant;
import fr.troopy28.replication.replication.npc.ReplicatedPlayer;
import fr.troopy28.replication.replication.protocol.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicationManager {

    /**
     * The map(pseudo, NPC) of the NPCs that are currently displayed on the server.
     */
    private Map<String, ReplicatedPlayer> replicatedPlayers;

    public ReplicationManager() {
        replicatedPlayers = new HashMap<>();
    }

    /**
     * Adds a new player to the list of the NPCs to show, and thus starts replicating him.
     *
     * @param profile The Mojang profile of the player to replicate.
     */
    private void registerReplicatedPlayer(GameProfile profile) {
        WorldServer worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
        ReplicatedPlayer replicatedPlayer = new ReplicatedPlayer(worldServer, profile);
        replicatedPlayers.put(profile.getName(), replicatedPlayer);
    }

    /**
     * Stops the replication of the player identified by the specified name, and then removes him from the map of the
     * replicated players.
     *
     * @param name The pseudo of the player you want to stop the replication.
     */
    private void unregisterReplicatedPlayer(String name) {
        if (replicatedPlayers.containsKey(name)) {
            ReplicatedPlayer player = replicatedPlayers.get(name);
            player.destroy();
            replicatedPlayers.remove(name);
        }
    }

    /**
     * Manages the specified kubicket. The actions that this kubicket carries will be applied to the player with the
     * specified name.
     *
     * @param playerName       The name of the player concerned by this kubicket.
     * @param receivedKubicket The kubicket received through Redis.
     */
    public void handleKubicket(String playerName, KubithonPacket receivedKubicket) {
        if (receivedKubicket instanceof PlayerLookKubicket) {
            PlayerLookKubicket lookKubicket = (PlayerLookKubicket) receivedKubicket;
            replicatedPlayers.get(playerName).updateLook(
                    lookKubicket.getPitchByte(),
                    lookKubicket.getYawByte()
            );
        } else if (receivedKubicket instanceof PlayerPositionKubicket) {
            PlayerPositionKubicket positionKubicket = (PlayerPositionKubicket) receivedKubicket;
            replicatedPlayers.get(playerName).teleport(
                    positionKubicket.getxPos(),
                    positionKubicket.getyPos(),
                    positionKubicket.getzPos(),
                    positionKubicket.isOnGround()
            );
        } else if (receivedKubicket instanceof PlayerPositionLookKubicket) {
            PlayerPositionLookKubicket positionLookKubicket = (PlayerPositionLookKubicket) receivedKubicket;
            replicatedPlayers.get(playerName).teleport( // PITCH & YAW = 0
                    positionLookKubicket.getxPos(),
                    positionLookKubicket.getyPos(),
                    positionLookKubicket.getzPos(),
                    positionLookKubicket.getPitchByte(),
                    positionLookKubicket.getYawByte(),
                    positionLookKubicket.isOnGround()
            );
        } else if (receivedKubicket instanceof PlayerConnectionKubicket) {
            handleConnectionKubicket((PlayerConnectionKubicket) receivedKubicket);
        } else if (receivedKubicket instanceof PlayerHandAnimationKubicket) {
            PlayerHandAnimationKubicket handAnimationKubicket = (PlayerHandAnimationKubicket) receivedKubicket;
            replicatedPlayers.get(playerName).moveArm(handAnimationKubicket.getHand());
        } else if (receivedKubicket instanceof PlayerEquipmentKubicket) {
            handleEquipmentKubicket(playerName, (PlayerEquipmentKubicket) receivedKubicket);
        } else if (receivedKubicket instanceof PlayerChatMessageKubicket) {
            handleChatMessageKubicket(playerName, (PlayerChatMessageKubicket) receivedKubicket);
        }
    }

    private void handleChatMessageKubicket(String playerName, PlayerChatMessageKubicket receivedPacket) {
        MinecraftServer server = ReplicationMod.get().getMinecraftServer();
        if (server != null) { // Server is null while no player came on the server
            ITextComponent message = ITextComponent.Serializer.jsonToComponent(receivedPacket.getJsonMessage());
            if (message != null)
                server.getPlayerList().sendMessage(message);
        }


    }

    private void handleEquipmentKubicket(String playerName, PlayerEquipmentKubicket receivedPacket) {
        ReplicatedPlayer replicatedPlayer = replicatedPlayers.get(playerName);
        if (replicatedPlayer == null)
            return;
        replicatedPlayer.setHelmet(
                Item.getItemById(receivedPacket.getHelmetId()),
                receivedPacket.isHelmetEnchanted(),
                receivedPacket.getHelmetMeta()
        );
        replicatedPlayer.setChestplate(Item.getItemById(receivedPacket.getChestId()),
                receivedPacket.isChestEnchanted(),
                receivedPacket.getChestMeta()
        );
        replicatedPlayer.setLeggings(Item.getItemById(receivedPacket.getLeggingsId()),
                receivedPacket.isLeggingsEnchanted(),
                receivedPacket.getLeggingsMeta()
        );
        replicatedPlayer.setBoots(Item.getItemById(receivedPacket.getBootsId()),
                receivedPacket.isBootsEnchanted(),
                receivedPacket.getBootsMeta()
        );
        replicatedPlayer.setItemInMainHand(Item.getItemById(receivedPacket.getMainHandId()),
                receivedPacket.isMainHandEnchanted(),
                receivedPacket.getMainHandMeta()
        );
        replicatedPlayer.setItemInOffHand(Item.getItemById(receivedPacket.getOffHandId()),
                receivedPacket.isOffHandEnchanted(),
                receivedPacket.getOffHandMeta()
        );
    }

    /**
     * The function managing the reception of a kubicket holding a sponsor connection data. If the packet is saying
     * that the player has just connected to the server (state = 0), then it registers this player and starts replicating
     * it, using the registerReplicatedPlayer(uuid, name) method. <br/>
     * If the packet is saying that the player has disconnected, it unregisters him, using the unregisterReplicatedPlayer(
     * name) method, thus stopping its replication.
     *
     * @param receivedPacket The received connection kubicket.
     */
    private void handleConnectionKubicket(PlayerConnectionKubicket receivedPacket) {
        if (receivedPacket.getState() == 0) {

            GameProfile profile = new GameProfile(
                    UUID.fromString(receivedPacket.getPlayerUuid()),
                    receivedPacket.getPlayerName()
            );
            profile.getProperties().put("textures", new Property(
                    "textures",
                    receivedPacket.getPlayerSkin(),
                    receivedPacket.getPlayerSkinSignature())
            );
            registerReplicatedPlayer(profile);
        } else if (receivedPacket.getState() == 1) {
            unregisterReplicatedPlayer(receivedPacket.getPlayerName());
        }
    }

    /**
     * Publish the stuff of the specified player in the Redis network, using the usual conventions on the topic
     * and the pattern.
     *
     * @param entityPlayer The player you want to send the stuff.
     */
    public static void sendPlayerStuff(EntityPlayer entityPlayer) {
        ItemStack helmet = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        ItemStack chestplate = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        ItemStack leggings = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        ItemStack boots = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        ItemStack mainHand = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        ItemStack offHand = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);

        final int helmetId = Item.getIdFromItem(helmet.getItem());
        final int chestplateId = Item.getIdFromItem(chestplate.getItem());
        final int leggingsId = Item.getIdFromItem(leggings.getItem());
        final int bootsId = Item.getIdFromItem(boots.getItem());
        final int mainHandId = Item.getIdFromItem(mainHand.getItem());
        final int offHandId = Item.getIdFromItem(offHand.getItem());

        PlayerEquipmentKubicket equipmentKubicket = new PlayerEquipmentKubicket();

        // The IDs
        equipmentKubicket.setHelmetId((short) helmetId);
        equipmentKubicket.setChestId((short) chestplateId);
        equipmentKubicket.setLeggingsId((short) leggingsId);
        equipmentKubicket.setBootsId((short) bootsId);
        equipmentKubicket.setMainHandId((short) mainHandId);
        equipmentKubicket.setOffHandId((short) offHandId);

        // The metadata
        equipmentKubicket.setHelmetMeta(helmet.getMetadata());
        equipmentKubicket.setChestMeta(chestplate.getMetadata());
        equipmentKubicket.setLeggingsMeta(leggings.getMetadata());
        equipmentKubicket.setBootsMeta(boots.getMetadata());
        equipmentKubicket.setMainHandMeta(mainHand.getMetadata());
        equipmentKubicket.setOffHandMeta(offHand.getMetadata());

        // The enchantments
        equipmentKubicket.setHelmetEnchanted(helmet.isItemEnchanted());
        equipmentKubicket.setChestEnchanted(chestplate.isItemEnchanted());
        equipmentKubicket.setLeggingsEnchanted(leggings.isItemEnchanted());
        equipmentKubicket.setBootsEnchanted(boots.isItemEnchanted());
        equipmentKubicket.setMainHandEnchanted(mainHand.isItemEnchanted());
        equipmentKubicket.setOffHandEnchanted(offHand.isItemEnchanted());

        // Send it
        ReplicationMod.get().getMessageBroker().publish(
                BrokingConstant.REPLICATION_PATTERN.concat(
                        String.valueOf(ReplicationMod.get().getServerId()))
                        .concat(entityPlayer.getName()),
                Base64.getEncoder().encodeToString(equipmentKubicket.serialize())
        );
    }

    public static void sendPlayerMessage(ITextComponent message, EntityPlayer entityPlayer) {
        // Debug
        String rawJson = ITextComponent.Serializer.componentToJson(message);
        ReplicationMod.get().getLogger().info(rawJson);

        // Create the kubicket
        PlayerChatMessageKubicket chatMessageKubicket = new PlayerChatMessageKubicket();
        chatMessageKubicket.setJsonMessage(rawJson);

        // Send it
        ReplicationMod.get().getMessageBroker().publish(
                BrokingConstant.REPLICATION_PATTERN.concat(
                        String.valueOf(ReplicationMod.get().getServerId()))
                        .concat(entityPlayer.getName()),
                Base64.getEncoder().encodeToString(chatMessageKubicket.serialize())
        );
    }
}