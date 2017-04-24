package org.kubithon.replicate.replication.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

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
    }

    public void spawn(Location loc, List<Player> players) {
        npcEntity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        for (Player pls : players) {
            PlayerConnection playerConnection = ((CraftPlayer) pls).getHandle().playerConnection;
            playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npcEntity));
            playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(npcEntity));
        }
    }

    public void teleport(Location loc) {
        npcEntity.teleportTo(loc, false);
    }
}
