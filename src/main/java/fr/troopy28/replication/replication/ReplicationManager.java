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
        //ReplicationMod.get().getLogger().info("Handling a kubicket. Player: " + playerName + " | PacketType: " + receivedKubicket.getType());

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
            replicatedPlayers.get(playerName).teleport(
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
        }
    }

    private void handleEquipmentKubicket(String playerName, PlayerEquipmentKubicket receivedPacket) {
        ReplicatedPlayer replicatedPlayer = replicatedPlayers.get(playerName);
        replicatedPlayer.setHelmet(Item.getItemById(receivedPacket.getHelmetId()));
        replicatedPlayer.setChestplate(Item.getItemById(receivedPacket.getChestId()));
        replicatedPlayer.setLeggings(Item.getItemById(receivedPacket.getLeggingsId()));
        replicatedPlayer.setBoots(Item.getItemById(receivedPacket.getBootsId()));
        replicatedPlayer.setItemInMainHand(Item.getItemById(receivedPacket.getMainHandId()));
        replicatedPlayer.setItemInOffHand(Item.getItemById(receivedPacket.getOffHandId()));
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
        net.minecraft.item.ItemStack helmet = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        net.minecraft.item.ItemStack chestplate = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        net.minecraft.item.ItemStack leggings = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        net.minecraft.item.ItemStack boots = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        net.minecraft.item.ItemStack mainHand = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        net.minecraft.item.ItemStack offHand = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);

        final int helmetId = helmet != null ? Item.getIdFromItem(helmet.getItem()) : 0;
        final int chestplateId = chestplate != null ? Item.getIdFromItem(chestplate.getItem()) : 0;
        final int leggingsId = leggings != null ? Item.getIdFromItem(leggings.getItem()) : -1;
        final int bootsId = boots != null ? Item.getIdFromItem(boots.getItem()) : 0;
        final int mainHandId = mainHand != null ? Item.getIdFromItem(mainHand.getItem()) : 0;
        final int offHandId = offHand != null ? Item.getIdFromItem(offHand.getItem()) : 0;

        PlayerEquipmentKubicket equipmentKubicket = new PlayerEquipmentKubicket();

        equipmentKubicket.setHelmetId((short) helmetId);
        equipmentKubicket.setChestId((short) chestplateId);
        equipmentKubicket.setLeggingsId((short) leggingsId);
        equipmentKubicket.setBootsId((short) bootsId);
        equipmentKubicket.setMainHandId((short) mainHandId);
        equipmentKubicket.setOffHandId((short) offHandId);

        ReplicationMod.get().getMessageBroker().publish(
                BrokingConstant.REPLICATION_PATTERN.concat(
                        String.valueOf(ReplicationMod.get().getServerId()))
                        .concat(entityPlayer.getName()),
                Base64.getEncoder().encodeToString(equipmentKubicket.serialize())
        );
    }
}