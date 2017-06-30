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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicatedPlayer implements Runnable {

    /**
     * The distance at which the NPC become visible.
     */
    private static final int NPC_VISIBILITY_DISTANCE = 70;
    private EntityPlayer npcEntity;
    private BukkitTask updateTask;

    /**
     * The connected players that are going to receive the packets from this NPC.
     */
    private List<Player> targets;

    public ReplicatedPlayer(UUID uuid, String name) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        npcEntity = new EntityPlayer(nmsServer, nmsWorld, new GameProfile(uuid, name), new PlayerInteractManager(nmsWorld));
        ReplicatePlugin.get().getLogger().info("######################");
        ReplicatePlugin.get().getLogger().info("This server is now displaying the fake player " + name + ".");
        targets = new ArrayList<>();

        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(ReplicatePlugin.get(), this, 5, 5); // Run this in 5 ticks
    }


    public void moveArm(EnumHand hand) {
        PacketPlayOutAnimation handPacket = new PacketPlayOutAnimation(npcEntity, hand.ordinal());
        sendPacketToAllTargets(handPacket);
    }

    public void updateLook(float pitch, float yaw) {
        npcEntity.pitch = pitch;
        npcEntity.yaw = yaw;

        PacketPlayOutEntity.PacketPlayOutEntityLook bodyLookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                npcEntity.getId(),
                getByteForAngle(yaw),
                getByteForAngle(pitch),
                npcEntity.onGround
        );
        sendPacketToAllTargets(bodyLookPacket);

        PacketPlayOutEntityHeadRotation newHeadLook = new PacketPlayOutEntityHeadRotation(
                npcEntity,
                getByteForAngle(npcEntity.yaw)
        );
        sendPacketToAllTargets(newHeadLook);
    }

    // <editor-fold desc="Player equipment and items">

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

    public void teleport(float x, float y, float z, boolean onGround) {
        npcEntity.setLocation(x, y, z, npcEntity.yaw, npcEntity.pitch);
        npcEntity.onGround = onGround;

        PacketPlayOutEntityTeleport newPosition = new PacketPlayOutEntityTeleport(npcEntity);
        sendPacketToAllTargets(newPosition);

        Log.info("Updated the position of the NPC " + npcEntity.displayName);
    }

    public void teleport(float x, float y, float z, float pitch, float yaw, boolean onGround) {
        npcEntity.setLocation(x, y, z, yaw, pitch);
        npcEntity.onGround = onGround;

        PacketPlayOutEntityTeleport newPosition = new PacketPlayOutEntityTeleport(npcEntity);
        sendPacketToAllTargets(newPosition);

        PacketPlayOutEntityHeadRotation newHeadLook = new PacketPlayOutEntityHeadRotation(
                npcEntity,
                getByteForAngle(npcEntity.yaw)
        );
        sendPacketToAllTargets(newHeadLook);

        Log.info("Updated the position of the NPC " + npcEntity.displayName);
    }

    private void sendPacketToAllTargets(Packet<?> packet) {
        targets.stream().forEach(target -> ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packet));
    }

    // </editor-fold>

    // <editor-fold desc="Spawning / dispawing / destroying">

    public void spawnFor(Player target) {
        PlayerConnection playerConnection = ((CraftPlayer) target).getHandle().playerConnection;
        playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npcEntity));
        playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(npcEntity));
        sendEquipmentTo(target);
    }

    public void dispawnFor(Player target) {
        PlayerConnection playerConnection = ((CraftPlayer) target).getHandle().playerConnection;

        playerConnection.sendPacket(new PacketPlayOutEntityDestroy(npcEntity.getId()));
        playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                npcEntity));
    }

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
                getByteForAngle(npcEntity.yaw),
                getByteForAngle(npcEntity.pitch),
                npcEntity.onGround);
        sendPacketToAllTargets(newLook);
    }

    private byte getByteForAngle(float angle) {
        return (byte) ((angle * 255) / 360);
    }
}