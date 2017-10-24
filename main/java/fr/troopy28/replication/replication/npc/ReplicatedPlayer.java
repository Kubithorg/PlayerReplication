package fr.troopy28.replication.replication.npc;


import com.mojang.authlib.GameProfile;
import fr.troopy28.replication.ReplicationMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

/**
 * The class responsible of replicating a single sponsor. All the things the online players can see are sent by hand
 * using packets.
 *
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicatedPlayer /*implements Runnable*/ {

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
    private Timer updateTimer;

    /**
     * The connected players that are going to receive the packets from this NPC.
     */
    private List<EntityPlayer> targets;

    /**
     * Creates a NMS {@link EntityPlayer} with the specified name and UUID. Initializes the targets {@link ArrayList},
     * that is to say the list of the players that will receive the packets from this sponsor. Then starts a
     * {@link Timer} to actualize the list of the targets, and send the position / rotation.
     *
     * @param uuid The UUID of the sponsor.
     * @param name The display name of the sponsor.
     */
    public ReplicatedPlayer(WorldServer world, UUID uuid, String name) {
        if(world.getMinecraftServer() == null) {
            ReplicationMod.get().getLogger().error("Error : Minecraft server can't be accessed.");
            return;
        }

        GameProfile gameProfile = new GameProfile(uuid, name);
        EntityPlayerMP fakePlayer = new EntityPlayerMP(
                world.getMinecraftServer(),
                world,
                gameProfile,
                new PlayerInteractionManager(world)
        );
        fakePlayer.setPosition(100, 200, 100);
        fakePlayer.swingArm(EnumHand.MAIN_HAND);

        ReplicationMod.get().getLogger().info("This server is now displaying the fake player " + name + ".");
        targets = new ArrayList<>();
    }

    /**
     * Sends a packet to all the targets saying that this sponsor has moved his hand.
     *
     * @param hand The hand that moved, according to the received packet.
     */
    public void moveArm(EnumHand hand) {
        npcEntity.swingArm(hand);
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
    }

    // <editor-fold desc="Player equipment and items">
    // Self documenting....

    public void setItemInMainHand(Item item) {
        npcEntity.setHeldItem(EnumHand.MAIN_HAND, new net.minecraft.item.ItemStack(item, 1));
    }

    public void setItemInOffHand(Item item) {
        npcEntity.setHeldItem(EnumHand.OFF_HAND, new net.minecraft.item.ItemStack(item, 1));
    }

    public void setHelmet(Item item) {
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new net.minecraft.item.ItemStack(item, 1));
    }

    public void setChestplate(Item item) {
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new net.minecraft.item.ItemStack(item, 1));
    }

    public void setLeggings(Item item) {
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new net.minecraft.item.ItemStack(item, 1));
    }

    public void setBoots(Item item) {
        npcEntity.setItemStackToSlot(EntityEquipmentSlot.FEET, new net.minecraft.item.ItemStack(item, 1));
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
        ReplicationMod.get().getLogger().info("Updated the position of the NPC " + npcEntity.getName());
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
        ReplicationMod.get().getLogger().info("Updated the position of the NPC " + npcEntity.getName());
    }

    /**
     * Shorthand for sending the specified packet to all the targets.
     *
     * @param packet The packet to send.
     *
    private void sendPacketToAllTargets(Packet<?> packet) {
    targets.stream().forEach(target -> ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packet));
    }*/

    // </editor-fold>

    // <editor-fold desc="Spawning / dispawing / destroying">

    /**
     * Destroys this NPC: dispawn it for all the targets, kill the NMS {@link EntityPlayer}, cancels the update task,
     * clears the list of targets, and outputs a message saying the sponsor corresponding to this NPC is no more
     * replicated.
     */
    public void destroy() {
        npcEntity.setDead();
        updateTimer.cancel();
        targets.clear();
        targets = null;
        ReplicationMod.get().getLogger().info("The player " + npcEntity.getName() + " is no more replicated.");
    }

    // </editor-fold>
}