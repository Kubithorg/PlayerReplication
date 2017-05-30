package org.kubithon.replicate.replication.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.kubithon.replicate.ReplicatePlugin;

import java.util.List;
import java.util.UUID;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicatedPlayer {

    private EntityPlayer npcEntity;

    public ReplicatedPlayer(UUID uuid, String name) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        npcEntity = new EntityPlayer(nmsServer, nmsWorld, new GameProfile(uuid, name), new PlayerInteractManager(nmsWorld));
        ReplicatePlugin.get().getLogger().info("This server is now displaying the fake player " + name + ".");
    }

    public void spawn(Location loc, List<Player> players) {
        npcEntity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        for (Player pls : players) {
            PlayerConnection playerConnection = ((CraftPlayer) pls).getHandle().playerConnection;
            playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npcEntity));
            playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(npcEntity));
        }
    }

    public void moveArm(EnumHand hand) {
        PacketPlayOutAnimation handPacket = new PacketPlayOutAnimation();

    }

    public void updateLook(float pitch, float yaw) {

    }

    public void teleport(Location loc) {
        npcEntity.teleportTo(loc, false);
    }

    public void teleport(float x, float y, float z) {
        npcEntity.setPosition(x, y, z);
    }

    public void teleport(float x, float y, float z, float pitch, float yaw) {
        npcEntity.setLocation(x, y, z, yaw, pitch);
    }

    public Location getLocation() {
        return npcEntity.getBukkitEntity().getLocation();
    }
}
