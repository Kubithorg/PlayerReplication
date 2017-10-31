package fr.troopy28.replication.replication.npc;


import com.mojang.authlib.GameProfile;
import fr.troopy28.replication.ReplicationMod;
import fr.troopy28.replication.replication.protocol.KubithonPacket;
import fr.troopy28.replication.utils.ForgeScheduler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * The class responsible of replicating a single sponsor. All the things the online players can see are sent by hand
 * using packets.
 *
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicatedPlayer implements Runnable {

    /**
     * The distance at which the NPC become visible.
     */
    private static final int NPC_VISIBILITY_DISTANCE = 70;

    private static List<Integer> npcsEntityIds = new ArrayList<>();

    /**
     * The NMS entity.
     */
    private EntityPlayerMP npcEntity;
    /**
     * A pointer to the bukkit task. Used to cancel it when the player should no more be replicated.
     */
    private Timer updateTimer;

    /**
     * The connected players that are going to receive the packets from this NPC.
     */
    private List<EntityPlayerMP> targets;

    /**
     * Creates a NMS {@link EntityPlayer} with the specified name and UUID. Initializes the targets {@link ArrayList},
     * that is to say the list of the players that will receive the packets from this sponsor. Then starts a
     * {@link Timer} to actualize the list of the targets, and send the position / rotation.
     *
     * @param world   World where to spawn the NPC.
     * @param profile Mojang profile of the player to replicate.
     */
    public ReplicatedPlayer(WorldServer world, GameProfile profile) {
        if (world.getMinecraftServer() == null) {
            ReplicationMod.get().getLogger().error("Error : Minecraft server can't be accessed.");
            return;
        }

        npcEntity = new EntityPlayerMP(
                world.getMinecraftServer(),
                world,
                profile,
                new PlayerInteractionManager(world)
        );
        npcEntity.setPosition(0, -100, 0); // Hide it while we don't have any new position data

        ReplicationMod.get().getLogger().info("This server is now displaying the fake player " + npcEntity.getName() + " | entityID = " + npcEntity.getEntityId());
        targets = new ArrayList<>();

        npcsEntityIds.add(npcEntity.getEntityId());

        updateTimer = ForgeScheduler.runRepeatingTaskLater(this, 250, 250);
    }

    /**
     * Sends a packet to all the targets saying that this sponsor has moved his hand.
     *
     * @param hand The hand that moved, according to the received packet.
     */
    public void moveArm(EnumHand hand) {
        npcEntity.swingArm(hand);
        SPacketAnimation swingPacket = new SPacketAnimation(npcEntity, hand.ordinal());
        sendPacketToAllTargets(swingPacket);
    }

    /**
     * Updates the look of this NPC. The pitch and the yaw are defined in term of 1/255 of circle (0 = 0°; 255 = 360°).
     * See wiki.vg for the details.
     *
     * @param pitchByte The byte representation of the pitch.
     * @param yawByte   The byte representation of the yaw.
     */
    public void updateLook(byte pitchByte, byte yawByte) {
        npcEntity.setPositionAndRotation(npcEntity.posX, npcEntity.posY, npcEntity.posZ, yawByte, pitchByte);
        npcEntity.setRotationYawHead(yawByte);
        SPacketEntity.S16PacketEntityLook look = new SPacketEntity.S16PacketEntityLook(
                npcEntity.getEntityId(),
                pitchByte,
                yawByte,
                npcEntity.onGround
        );
        sendPacketToAllTargets(look);
        SPacketEntityHeadLook headLook = new SPacketEntityHeadLook(npcEntity, yawByte);
        sendPacketToAllTargets(headLook);
    }

    // <editor-fold desc="Player equipment and items">
    // Self documenting....

    public void setItemInMainHand(Item item) {
        npcEntity.setHeldItem(EnumHand.MAIN_HAND, new net.minecraft.item.ItemStack(item, 1));
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.MAINHAND, new net.minecraft.item.ItemStack(item, 1));
        sendPacketToAllTargets(equipment);
    }

    public void setItemInOffHand(Item item) {
        npcEntity.setHeldItem(EnumHand.OFF_HAND, new net.minecraft.item.ItemStack(item, 1));
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.OFFHAND, new net.minecraft.item.ItemStack(item, 1));
        sendPacketToAllTargets(equipment);
    }

    public void setHelmet(Item item) {
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new net.minecraft.item.ItemStack(item, 1));
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.HEAD, new net.minecraft.item.ItemStack(item, 1));
        sendPacketToAllTargets(equipment);
    }

    public void setChestplate(Item item) {
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new net.minecraft.item.ItemStack(item, 1));
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.CHEST, new net.minecraft.item.ItemStack(item, 1));
        sendPacketToAllTargets(equipment);
    }

    public void setLeggings(Item item) {
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new net.minecraft.item.ItemStack(item, 1));
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.LEGS, new net.minecraft.item.ItemStack(item, 1));
        sendPacketToAllTargets(equipment);
    }

    public void setBoots(Item item) {
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.FEET, new net.minecraft.item.ItemStack(item, 1));
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.FEET, new net.minecraft.item.ItemStack(item, 1));
        sendPacketToAllTargets(equipment);
    }


    // </editor-fold>

    // <editor-fold desc="Teleportation methods">

    /**
     * Updates the location of the player entity of this NPC, and then sends packets to notify the changes to the
     * targets.
     *
     * @param x        The x position of the sponsor.
     * @param y        The y position of the sponsor.
     * @param z        The z position of the sponsor.
     * @param onGround Is the sponsor on the ground?
     */
    public void teleport(float x, float y, float z, boolean onGround) {
        //ReplicationMod.get().getLogger().info("Updated the position of the NPC " + npcEntity.getName());
        npcEntity.setPosition(x, y, z);
        npcEntity.onGround = onGround;
        SPacketEntityTeleport teleport = new SPacketEntityTeleport(npcEntity);
        sendPacketToAllTargets(teleport);
    }

    private void sendStuff(EntityPlayerMP target) {
        /*ReplicationMod.get().getLogger().info("Gonna send the stuff packets to " + target.getName());

        SPacketEntityEquipment headPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.HEAD, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
        target.connection.sendPacket(headPacket);
        ReplicationMod.get().getLogger().info("Sent the head packet to" + target.getName());

        SPacketEntityEquipment chestPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.CHEST, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
        target.connection.sendPacket(chestPacket);
        ReplicationMod.get().getLogger().info("Sent the chest packet to" + target.getName());

        SPacketEntityEquipment legsPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.LEGS, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.LEGS));
        target.connection.sendPacket(legsPacket);
        ReplicationMod.get().getLogger().info("Sent the legs packet to" + target.getName());

        SPacketEntityEquipment feetPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.FEET, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.FEET));
        target.connection.sendPacket(feetPacket);
        ReplicationMod.get().getLogger().info("Sent the feet packet to" + target.getName());

        SPacketEntityEquipment mainHandPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.MAINHAND, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND));
        target.connection.sendPacket(mainHandPacket);
        ReplicationMod.get().getLogger().info("Sent the main hand packet to" + target.getName());

        SPacketEntityEquipment offHandPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.OFFHAND, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND));
        target.connection.sendPacket(offHandPacket);
        ReplicationMod.get().getLogger().info("Sent the off hand packet to" + target.getName());
*/
    }

    /**
     * Updates the location of the player entity of this NPC, and then sends packets to notify the changes to the
     * targets.
     *
     * @param x         The x position of the sponsor.
     * @param y         The y position of the sponsor.
     * @param z         The z position of the sponsor.
     * @param pitchByte The byte representation of the pitch.
     * @param yawByte   The byte representation of the yaw.
     * @param onGround  Is the sponsor on the ground?
     */
    public void teleport(float x, float y, float z, byte pitchByte, byte yawByte, boolean onGround) {
        //ReplicationMod.get().getLogger().info("Updated the position of the NPC " + npcEntity.getName());
        npcEntity.setPositionAndRotation(x, y, z, KubithonPacket.getAngleFromByte(yawByte), KubithonPacket.getAngleFromByte(pitchByte));
        npcEntity.onGround = onGround;

        SPacketEntityTeleport teleport = new SPacketEntityTeleport(npcEntity);
        sendPacketToAllTargets(teleport);
        SPacketEntity.S16PacketEntityLook look = new SPacketEntity.S16PacketEntityLook(
                npcEntity.getEntityId(),
                pitchByte,
                yawByte,
                onGround
        );
        sendPacketToAllTargets(look);
        SPacketEntityHeadLook headLook = new SPacketEntityHeadLook(npcEntity, yawByte);
        sendPacketToAllTargets(headLook);
    }

    /**
     * Shorthand for sending the specified packet to all the targets.
     *
     * @param packet The packet to send.
     */
    private void sendPacketToAllTargets(Packet<?> packet) {
        targets.stream().forEach(target -> target.connection.sendPacket(packet));
    }

    // </editor-fold>

    // <editor-fold desc="Spawning / dispawing / destroying">

    /**
     * Destroys this NPC: dispawn it for all the targets, kill the NMS {@link EntityPlayer}, cancels the update task,
     * clears the list of targets, and outputs a message saying the sponsor corresponding to this NPC is no more
     * replicated.
     */
    public void destroy() {
        npcsEntityIds.remove(npcEntity.getEntityId());
        npcEntity.setDead();
        updateTimer.cancel();
        targets.clear();
        targets.forEach(this::dispawnFor);
        targets = null;
        ReplicationMod.get().getLogger().info("The player " + npcEntity.getName() + " is no more replicated.");
        npcEntity = null;
    }

    private void dispawnFor(EntityPlayerMP target) {
        SPacketDestroyEntities destroyPacket = new SPacketDestroyEntities(npcEntity.getEntityId());
        target.connection.sendPacket(destroyPacket);
        SPacketPlayerListItem info = new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, npcEntity);
        target.connection.sendPacket(info);
    }

    private void spawnFor(EntityPlayerMP target) {
        ReplicationMod.get().getLogger().info("Gonna spawn the NPC for " + target.getName());
        SPacketPlayerListItem info = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, npcEntity);
        target.connection.sendPacket(info);
        ReplicationMod.get().getLogger().info("Sent the info packet to " + target.getName());

        SPacketSpawnPlayer spawn = new SPacketSpawnPlayer(npcEntity);
        target.connection.sendPacket(spawn);
        ReplicationMod.get().getLogger().info("Sent the spawn packet to " + target.getName());

        sendStuff(target);
    }

    @Override
    public void run() {
        for (EntityPlayerMP nearbyNotTargetPlayer : npcEntity.getEntityWorld().getPlayers(
                EntityPlayerMP.class,
                player -> !npcsEntityIds.contains(player.getEntityId()))) {

            try {
                // If player isn't targeted but should be, add them and spawn the NPC for them
                if (nearbyNotTargetPlayer.getDistance(npcEntity) < NPC_VISIBILITY_DISTANCE
                        && !targets.contains(nearbyNotTargetPlayer)) {
                    targets.add(nearbyNotTargetPlayer);
                    ForgeScheduler.runTaskLater(() -> {
                        ReplicationMod.get().getLogger().info("Adding the target " + nearbyNotTargetPlayer.getName() + ".");
                        spawnFor(nearbyNotTargetPlayer);
                    }, 1000);
                } else if (nearbyNotTargetPlayer.getDistance(npcEntity) >= NPC_VISIBILITY_DISTANCE
                        && targets.contains(nearbyNotTargetPlayer)) {
                    ReplicationMod.get().getLogger().info("Removing the target " + nearbyNotTargetPlayer.getName() + ".");
                    dispawnFor(nearbyNotTargetPlayer);
                    targets.remove(nearbyNotTargetPlayer);
                }
            } catch (Exception ex) {
                ReplicationMod.get().getLogger().error("An error occurred while updating the NPC " + npcEntity.getName() + ":", ex);
            }
        }
        // Remove all disconnected targets
        targets.stream().filter(EntityPlayerMP::hasDisconnected).forEach(targets::remove);
    }

    // </editor-fold>
}