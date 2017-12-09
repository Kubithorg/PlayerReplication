package fr.troopy28.replication.replication.npc;


import com.mojang.authlib.GameProfile;
import fr.troopy28.replication.ReplicationMod;
import fr.troopy28.replication.replication.protocol.KubithonPacket;
import fr.troopy28.replication.utils.ForgeScheduler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    private byte pitch;
    private byte yaw;

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

        ReplicationMod.get().getLogger().info("Trying to create a NPC for " + profile.getName() + ".");
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

    // <editor-fold desc="Player equipment and items">
    // Self documenting....

    public void setItemInMainHand(Item item, boolean enchanted, int meta) {
        ItemStack itemStack = new ItemStack(item, 1, meta);
        if (enchanted)
            itemStack.addEnchantment(Enchantment.getEnchantmentByID(0), 1);
        npcEntity.setHeldItem(EnumHand.MAIN_HAND, itemStack);
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.MAINHAND, itemStack);
        sendPacketToAllTargets(equipment);
    }

    public void setItemInOffHand(Item item, boolean enchanted, int meta) {
        ItemStack itemStack = new ItemStack(item, 1, meta);
        if (enchanted)
            itemStack.addEnchantment(Enchantment.getEnchantmentByID(0), 1);
        npcEntity.setHeldItem(EnumHand.OFF_HAND, itemStack);
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.OFFHAND, itemStack);
        sendPacketToAllTargets(equipment);
    }

    public void setHelmet(Item item, boolean enchanted, int meta) {
        ItemStack itemStack = new ItemStack(item, 1, meta);
        if (enchanted)
            itemStack.addEnchantment(Enchantment.getEnchantmentByID(0), 1);
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.HEAD, itemStack);
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.HEAD, itemStack);
        sendPacketToAllTargets(equipment);
    }

    public void setChestplate(Item item, boolean enchanted, int meta) {
        ItemStack itemStack = new ItemStack(item, 1, meta);
        if (enchanted)
            itemStack.addEnchantment(Enchantment.getEnchantmentByID(0), 1);
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.CHEST, itemStack);
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.CHEST, itemStack);
        sendPacketToAllTargets(equipment);
    }

    public void setLeggings(Item item, boolean enchanted, int meta) {
        ItemStack itemStack = new ItemStack(item, 1, meta);
        if (enchanted)
            itemStack.addEnchantment(Enchantment.getEnchantmentByID(0), 1);
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.LEGS, itemStack);
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.LEGS, itemStack);
        sendPacketToAllTargets(equipment);
    }

    public void setBoots(Item item, boolean enchanted, int meta) {
        ItemStack itemStack = new ItemStack(item, 1, meta);
        if (enchanted)
            itemStack.addEnchantment(Enchantment.getEnchantmentByID(0), 1);
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.FEET, itemStack);
        SPacketEntityEquipment equipment = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.FEET, itemStack);
        sendPacketToAllTargets(equipment);
    }

    private void sendStuff(EntityPlayerMP target) {
        SPacketEntityEquipment headPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.HEAD, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
        target.connection.sendPacket(headPacket);

        SPacketEntityEquipment chestPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.CHEST, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
        target.connection.sendPacket(chestPacket);

        SPacketEntityEquipment legsPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.LEGS, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.LEGS));
        target.connection.sendPacket(legsPacket);

        SPacketEntityEquipment feetPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.FEET, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.FEET));
        target.connection.sendPacket(feetPacket);

        SPacketEntityEquipment mainHandPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.MAINHAND, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND));
        target.connection.sendPacket(mainHandPacket);

        SPacketEntityEquipment offHandPacket = new SPacketEntityEquipment(npcEntity.getEntityId(), EntityEquipmentSlot.OFFHAND, npcEntity.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND));
        target.connection.sendPacket(offHandPacket);
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
        if (npcEntity.getDistance(x, y, z) > 8) { // If more than 8 blocks : TP
            npcEntity.setPosition(x, y, z);
            npcEntity.onGround = onGround;
            SPacketEntityTeleport teleport = new SPacketEntityTeleport(npcEntity);
            sendPacketToAllTargets(teleport);
            SPacketEntityHeadLook headLook = new SPacketEntityHeadLook(npcEntity, KubithonPacket.getByteFromAngle(npcEntity.getRotationYawHead()));
            sendPacketToAllTargets(headLook);
            //ReplicationMod.get().getLogger().info("TP only pitch = " + pitch + " ; yaw = " + yaw);
        } else { // Relative move
            SPacketEntity.S15PacketEntityRelMove movePacket = new SPacketEntity.S15PacketEntityRelMove(
                    npcEntity.getEntityId(),
                    (long) (x * 32 - npcEntity.posX * 32) * 128,
                    (long) (y * 32 - npcEntity.posY * 32) * 128,
                    (long) (z * 32 - npcEntity.posZ * 32) * 128,
                    onGround
            );
            sendPacketToAllTargets(movePacket);

            npcEntity.setPosition(x, y, z);
            npcEntity.onGround = onGround;
        }
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
        yaw = yawByte;
        pitch = pitchByte;

        if (npcEntity.getDistance(x, y, z) > 8) { // If more than 8 blocks : TP
            npcEntity.setPositionAndRotation(x, y, z, KubithonPacket.getAngleFromByte(yawByte), KubithonPacket.getAngleFromByte(pitchByte));
            npcEntity.setRotationYawHead(KubithonPacket.getAngleFromByte(yawByte));
            npcEntity.onGround = onGround;

            npcEntity.setPosition(x, y, z);
            npcEntity.onGround = onGround;
            SPacketEntityTeleport teleport = new SPacketEntityTeleport(npcEntity);
            sendPacketToAllTargets(teleport);

            SPacketEntity.S16PacketEntityLook look = new SPacketEntity.S16PacketEntityLook(
                    npcEntity.getEntityId(),
                    yaw,
                    pitch,
                    onGround
            );
            sendPacketToAllTargets(look);

            SPacketEntityHeadLook headLook = new SPacketEntityHeadLook(npcEntity, KubithonPacket.getByteFromAngle(npcEntity.rotationYawHead));
            sendPacketToAllTargets(headLook);
            //ReplicationMod.get().getLogger().info("TP & look pitch = " + pitch + " ; yaw = " + yaw);
        } else { // Relative move
            SPacketEntity.S17PacketEntityLookMove movePacket = new SPacketEntity.S17PacketEntityLookMove(
                    npcEntity.getEntityId(),
                    (long) (x * 32 - npcEntity.posX * 32) * 128,
                    (long) (y * 32 - npcEntity.posY * 32) * 128,
                    (long) (z * 32 - npcEntity.posZ * 32) * 128,
                    yaw,
                    pitch,
                    onGround
            );
            sendPacketToAllTargets(movePacket);

            npcEntity.setPositionAndRotation(x, y, z, KubithonPacket.getAngleFromByte(yawByte), KubithonPacket.getAngleFromByte(pitchByte));
            npcEntity.setRotationYawHead(KubithonPacket.getAngleFromByte(yawByte));
            npcEntity.onGround = onGround;

            SPacketEntityHeadLook headLook = new SPacketEntityHeadLook(npcEntity, yawByte);
            sendPacketToAllTargets(headLook);

            //ReplicationMod.get().getLogger().info("TP & look pitch = " + pitch + " ; yaw = " + yaw);
        }
    }

    /**
     * Updates the look of this NPC. The pitch and the yaw are defined in term of 1/255 of circle (0 = 0°; 255 = 360°).
     * See wiki.vg for the details.
     *
     * @param pitchByte The byte representation of the pitch.
     * @param yawByte   The byte representation of the yaw.
     */
    public void updateLook(byte pitchByte, byte yawByte) {

        yaw = yawByte;
        pitch = pitchByte;
        npcEntity.setPositionAndRotation(npcEntity.posX, npcEntity.posY, npcEntity.posZ, KubithonPacket.getAngleFromByte(yawByte), KubithonPacket.getAngleFromByte(pitchByte));
        npcEntity.setRotationYawHead(KubithonPacket.getAngleFromByte(yawByte));
        SPacketEntity.S16PacketEntityLook look = new SPacketEntity.S16PacketEntityLook(
                npcEntity.getEntityId(),
                yawByte,
                pitchByte,
                npcEntity.onGround
        );
        sendPacketToAllTargets(look);
        SPacketEntityHeadLook headLook = new SPacketEntityHeadLook(npcEntity, yawByte);
        sendPacketToAllTargets(headLook);
    }

    // </editor-fold>

    /**
     * Shorthand for sending the specified packet to all the targets.
     *
     * @param packet The packet to send.
     */
    private void sendPacketToAllTargets(Packet<?> packet) {
        targets.stream().forEach(target -> target.connection.sendPacket(packet));
    }


    // <editor-fold desc="Spawning / dispawing / destroying">

    /**
     * Destroys this NPC: dispawn it for all the targets, kill the NMS {@link EntityPlayer}, cancels the update task,
     * clears the list of targets, and outputs a message saying the sponsor corresponding to this NPC is no more
     * replicated.
     */
    public void destroy() {
        if (npcsEntityIds.contains(npcEntity.getEntityId()))
            // Another example of the stupidity of Java: the cast is necessary to remove an element, or it thinks it should remove at an index
            npcsEntityIds.remove((Integer) npcEntity.getEntityId());
        npcEntity.setDead();
        updateTimer.cancel();
        targets.forEach(this::dispawnFor);
        targets.clear();
        targets = null;
        ReplicationMod.get().getLogger().info("The player " + npcEntity.getName() + " is no more replicated.");
        npcEntity = null;
    }

    private void dispawnFor(EntityPlayerMP target) {
        if (target != null) {
            SPacketDestroyEntities destroyPacket = new SPacketDestroyEntities(npcEntity.getEntityId());
            target.connection.sendPacket(destroyPacket);
            SPacketPlayerListItem info = new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, npcEntity);
            target.connection.sendPacket(info);
        }
    }

    private void spawnFor(EntityPlayerMP target) {
        if (target != null && !target.hasDisconnected()) {
            SPacketPlayerListItem info = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, npcEntity);
            target.connection.sendPacket(info);

            SPacketSpawnPlayer spawn = new SPacketSpawnPlayer(npcEntity);
            target.connection.sendPacket(spawn);

            sendStuff(target);
        }
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
                        ReplicationMod.get().getLogger().info("Adding the target " + nearbyNotTargetPlayer.getName() + "...");
                        spawnFor(nearbyNotTargetPlayer);
                        ReplicationMod.get().getLogger().info("Done.");
                    }, 5000); // Absolutely necessary to wait, or a NPE will occur on the client side, because the world might not be fully loaded.
                } else if (nearbyNotTargetPlayer.getDistance(npcEntity) >= NPC_VISIBILITY_DISTANCE
                        && targets.contains(nearbyNotTargetPlayer)) {
                    ReplicationMod.get().getLogger().info("Removing the target " + nearbyNotTargetPlayer.getName() + "...");
                    dispawnFor(nearbyNotTargetPlayer);
                    targets.remove(nearbyNotTargetPlayer);
                    ReplicationMod.get().getLogger().info("Done.");
                }
            } catch (Exception ex) {
                ReplicationMod.get().getLogger().error("An error occurred while updating the NPC " + npcEntity.getName() + ":", ex);
            }
        }
        // Remove all disconnected targets
        targets.removeIf(EntityPlayerMP::hasDisconnected);
    }

    // </editor-fold>
}