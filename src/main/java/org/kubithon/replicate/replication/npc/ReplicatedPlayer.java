package org.kubithon.replicate.replication.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
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

        Bukkit.getScheduler().runTaskTimerAsynchronously(ReplicatePlugin.get(), this, 5, 5); // Run this in 5 ticks
    }


    public void moveArm(EnumHand hand) {
        PacketPlayOutAnimation handPacket = new PacketPlayOutAnimation(npcEntity, hand.ordinal());
        sendPacketToAllTargets(handPacket);
    }

    public void updateLook(float pitch, float yaw) {

    }

    // <editor-fold desc="Teleportation methods">

    public void teleport(float x, float y, float z, boolean onGround) {
        npcEntity.setLocation(x, y, z, npcEntity.yaw, npcEntity.pitch);
        npcEntity.onGround = onGround;

        long deltaX = (long) (x * 32 - npcEntity.locX * 32) * 128;
        long deltaY = (long) (y * 32 - npcEntity.locY * 32) * 128;
        long deltaZ = (long) (z * 32 - npcEntity.locZ * 32) * 128;

        PacketPlayOutEntity.PacketPlayOutRelEntityMove motionPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                npcEntity.getId(),
                deltaX,
                deltaY,
                deltaZ,
                onGround
        );
        sendPacketToAllTargets(motionPacket);

        Log.info("Updated the position of the NPC " + npcEntity.displayName);
    }

    public void teleport(float x, float y, float z, float pitch, float yaw, boolean onGround) {
        long deltaX = (long) (x * 32 - npcEntity.locX * 32) * 128;
        long deltaY = (long) (y * 32 - npcEntity.locY * 32) * 128;
        long deltaZ = (long) (z * 32 - npcEntity.locZ * 32) * 128;

        npcEntity.setLocation(x, y, z, yaw, pitch);
        npcEntity.onGround = onGround;

        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook motionPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                npcEntity.getId(),
                deltaX,
                deltaY,
                deltaZ,
                getByteForAngle(yaw),
                getByteForAngle(pitch),
                onGround
        );

        sendPacketToAllTargets(motionPacket);
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
    }

    public void dispawnFor(Player target) {
        PacketPlayOutEntityDestroy deathPacket = new PacketPlayOutEntityDestroy(npcEntity.getId());
        PlayerConnection playerConnection = ((CraftPlayer) target).getHandle().playerConnection;
        playerConnection.sendPacket(deathPacket);
    }

    public void destroy() {
        targets.stream().forEach(this::dispawnFor);
        npcEntity.die();
    }

    // </editor-fold>

    public Location getLocation() {
        return npcEntity.getBukkitEntity().getLocation();
    }

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
        // Send at a regular interval the exact position to avoid movement offsets due to the relative movements
        PacketPlayOutEntityTeleport newPosition = new PacketPlayOutEntityTeleport(npcEntity);
        sendPacketToAllTargets(newPosition);

        PacketPlayOutEntity.PacketPlayOutEntityLook newLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                npcEntity.getId(),
                getByteForAngle(npcEntity.yaw),
                getByteForAngle(npcEntity.pitch),
                npcEntity.onGround);
        sendPacketToAllTargets(newLook);

        PacketPlayOutEntityHeadRotation newHeadLook = new PacketPlayOutEntityHeadRotation(
                npcEntity,
                getByteForAngle(npcEntity.pitch)
        );
        sendPacketToAllTargets(newHeadLook);
    }

    private byte getByteForAngle(float angle) {
        return (byte) ((angle * 255) / 360);
    }
}
