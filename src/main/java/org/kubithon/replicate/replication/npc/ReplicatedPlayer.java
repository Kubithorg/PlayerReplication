package org.kubithon.replicate.replication.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.replication.protocol.KubithonPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    /**
     * The NMS entity.
     */
    private EntityPlayer npcEntity;
    /**
     * A pointer to the bukkit task. Used to cancel it when the player should no more be replicated.
     */
    private BukkitTask updateTask;

    /**
     * The connected players that are going to receive the packets from this NPC.
     */
    private List<Player> targets;

    /**
     * Creates a NMS {@link EntityPlayer} with the specified name and UUID. Initializes the targets {@link ArrayList},
     * that is to say the list of the players that will receive the packets from this sponsor. Then starts a
     * {@link BukkitTask} to actualize the list of the targets, and send the position / rotation.
     *
     * @param uuid The UUID of the sponsor.
     * @param name The displaye name of the sponsor.
     */
    public ReplicatedPlayer(UUID uuid, String name) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        npcEntity = new EntityPlayer(nmsServer, nmsWorld, new GameProfile(uuid, name), new PlayerInteractManager(nmsWorld));
        ReplicatePlugin.get().getLogger().info("This server is now displaying the fake player " + name + ".");
        targets = new ArrayList<>();

        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(ReplicatePlugin.get(), this, 5, 5); // Run this in 5 ticks
    }

    /**
     * Sends a packet to all the targets saying that this sponsor has moved his hand.
     *
     * @param hand The hand that moved, according to the received packet.
     */
    public void moveArm(EnumHand hand) {
        PacketPlayOutAnimation handPacket = new PacketPlayOutAnimation(npcEntity, hand.ordinal());
        sendPacketToAllTargets(handPacket);
    }

    /**
     * Updates the look of this NPC. The pitch and the yaw are defined in term of 1/255 of circle (0 = 0°; 255 = 360°).
     * See wiki.vg for the details.
     *
     * @param pitchByte The byte representation of the pitch.
     * @param yawByte   The byte representation of the yaw.
     */
    public void updateLook(byte pitchByte, byte yawByte) {
        npcEntity.pitch = KubithonPacket.getAngleFromByte(pitchByte);
        npcEntity.yaw = KubithonPacket.getAngleFromByte(yawByte);

        PacketPlayOutEntity.PacketPlayOutEntityLook bodyLookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                npcEntity.getId(),
                yawByte,
                pitchByte,
                npcEntity.onGround
        );
        sendPacketToAllTargets(bodyLookPacket);

        PacketPlayOutEntityHeadRotation newHeadLook = new PacketPlayOutEntityHeadRotation(
                npcEntity,
                yawByte
        );
        sendPacketToAllTargets(newHeadLook);
    }

    // <editor-fold desc="Player equipment and items">
    // Self documenting....

    public void setItemInMainHand(org.bukkit.Material material) {
        setItemInSlot(material, EnumItemSlot.MAINHAND);
    }

    public void setItemInOffHand(org.bukkit.Material material) {
        setItemInSlot(material, EnumItemSlot.OFFHAND);
    }

    public void setHelmet(org.bukkit.Material material) {
        setItemInSlot(material, EnumItemSlot.HEAD);
    }

    public void setChestplate(org.bukkit.Material material) {
        setItemInSlot(material, EnumItemSlot.CHEST);
    }

    public void setLeggings(org.bukkit.Material material) {
        setItemInSlot(material, EnumItemSlot.LEGS);
    }

    public void setBoots(org.bukkit.Material material) {
        setItemInSlot(material, EnumItemSlot.FEET);
    }

    private void setItemInSlot(org.bukkit.Material material, EnumItemSlot slot) {
        org.bukkit.inventory.ItemStack bukkitStack = new ItemStack(material);

        switch (slot) {
            case HEAD:
                npcEntity.getBukkitEntity().getEquipment().setHelmet(bukkitStack);
                break;
            case CHEST:
                npcEntity.getBukkitEntity().getEquipment().setChestplate(bukkitStack);
                break;
            case LEGS:
                npcEntity.getBukkitEntity().getEquipment().setLeggings(bukkitStack);
                break;
            case FEET:
                npcEntity.getBukkitEntity().getEquipment().setLeggings(bukkitStack);
                break;
            case MAINHAND:
                npcEntity.getBukkitEntity().getEquipment().setItemInMainHand(bukkitStack);
                break;
            case OFFHAND:
                npcEntity.getBukkitEntity().getEquipment().setItemInOffHand(bukkitStack);
                break;
            default:
                break;
        }

        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(bukkitStack);

        PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment(
                npcEntity.getId(),
                slot,
                nmsStack
        );
        sendPacketToAllTargets(equipmentPacket);
    }

    private void sendEquipmentTo(Player target) {
        PlayerConnection playerConnection = ((CraftPlayer) target).getHandle().playerConnection;

        PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment(
                npcEntity.getId(),
                EnumItemSlot.HEAD,
                CraftItemStack.asNMSCopy(npcEntity.getBukkitEntity().getEquipment().getHelmet())
        );
        playerConnection.sendPacket(equipmentPacket);

        equipmentPacket = new PacketPlayOutEntityEquipment(
                npcEntity.getId(),
                EnumItemSlot.CHEST,
                CraftItemStack.asNMSCopy(npcEntity.getBukkitEntity().getEquipment().getChestplate())
        );
        playerConnection.sendPacket(equipmentPacket);

        equipmentPacket = new PacketPlayOutEntityEquipment(
                npcEntity.getId(),
                EnumItemSlot.LEGS,
                CraftItemStack.asNMSCopy(npcEntity.getBukkitEntity().getEquipment().getLeggings())
        );
        playerConnection.sendPacket(equipmentPacket);

        equipmentPacket = new PacketPlayOutEntityEquipment(
                npcEntity.getId(),
                EnumItemSlot.FEET,
                CraftItemStack.asNMSCopy(npcEntity.getBukkitEntity().getEquipment().getBoots())
        );
        playerConnection.sendPacket(equipmentPacket);

        equipmentPacket = new PacketPlayOutEntityEquipment(
                npcEntity.getId(),
                EnumItemSlot.MAINHAND,
                CraftItemStack.asNMSCopy(npcEntity.getBukkitEntity().getEquipment().getItemInMainHand())
        );
        playerConnection.sendPacket(equipmentPacket);

        equipmentPacket = new PacketPlayOutEntityEquipment(
                npcEntity.getId(),
                EnumItemSlot.OFFHAND,
                CraftItemStack.asNMSCopy(npcEntity.getBukkitEntity().getEquipment().getItemInOffHand())
        );
        playerConnection.sendPacket(equipmentPacket);
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
        npcEntity.setLocation(x, y, z, npcEntity.yaw, npcEntity.pitch);
        npcEntity.onGround = onGround;

        PacketPlayOutEntityTeleport newPosition = new PacketPlayOutEntityTeleport(npcEntity);
        sendPacketToAllTargets(newPosition);

        Log.info("Updated the position of the NPC " + npcEntity.displayName);
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
        npcEntity.setLocation(x, y, z, KubithonPacket.getAngleFromByte(yawByte), KubithonPacket.getAngleFromByte(pitchByte));
        npcEntity.onGround = onGround;

        PacketPlayOutEntityTeleport newPosition = new PacketPlayOutEntityTeleport(npcEntity);
        sendPacketToAllTargets(newPosition);

        PacketPlayOutEntityHeadRotation newHeadLook = new PacketPlayOutEntityHeadRotation(
                npcEntity,
                yawByte
        );
        sendPacketToAllTargets(newHeadLook);

        Log.info("Updated the position of the NPC " + npcEntity.displayName);
    }

    /**
     * Shorthand for sending the specified packet to all the targets.
     *
     * @param packet The packet to send.
     */
    private void sendPacketToAllTargets(Packet<?> packet) {
        targets.stream().forEach(target -> ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packet));
    }

    // </editor-fold>

    // <editor-fold desc="Spawning / dispawing / destroying">

    /**
     * Spawns this NPC for the specified target.
     *
     * @param target The player that will now see this NPC.
     */
    private void spawnFor(Player target) {
        PlayerConnection playerConnection = ((CraftPlayer) target).getHandle().playerConnection;
        playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npcEntity));
        playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(npcEntity));
        sendEquipmentTo(target);
    }

    /**
     * Destroys this NPC for the specify target.
     *
     * @param target The player that will no more see this NPC.
     */
    private void dispawnFor(Player target) {
        PlayerConnection playerConnection = ((CraftPlayer) target).getHandle().playerConnection;

        playerConnection.sendPacket(new PacketPlayOutEntityDestroy(npcEntity.getId()));
        playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                npcEntity));
    }

    /**
     * Destroys this NPC: dispawn it for all the targets, kill the NMS {@link EntityPlayer}, cancels the update task,
     * clears the list of targets, and outputs a message saying the sponsor corresponding to this NPC is no more
     * replicated.
     */
    public void destroy() {
        targets.stream().forEach(this::dispawnFor);
        npcEntity.die();
        updateTask.cancel();
        targets.clear();
        targets = null;
        Log.info("The player " + npcEntity.displayName + " is no more replicated.");
    }

    // </editor-fold>

    /**
     * Called every 5 ticks.
     */
    @Override
    public void run() {
        Location currentLocation = npcEntity.getBukkitEntity().getLocation();
        for (Player pls : Bukkit.getOnlinePlayers()) {
            double distance = pls.getLocation().distance(currentLocation);
            if (distance <= NPC_VISIBILITY_DISTANCE
                    && !targets.contains(pls)) {
                targets.add(pls);
                spawnFor(pls);
            } else if (distance > NPC_VISIBILITY_DISTANCE
                    && targets.contains(pls)) {
                dispawnFor(pls);
                targets.remove(pls);
            }
        }
        // Send at a regular interval the exact head rotation
        PacketPlayOutEntity.PacketPlayOutEntityLook newLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                npcEntity.getId(),
                KubithonPacket.getByteFromAngle(npcEntity.yaw),
                KubithonPacket.getByteFromAngle(npcEntity.pitch),
                npcEntity.onGround);
        sendPacketToAllTargets(newLook);
    }

}