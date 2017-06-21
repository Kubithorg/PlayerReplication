package org.kubithon.replicate.netty;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.server.v1_9_R2.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.BrokingConstant;
import org.kubithon.replicate.replication.protocol.KubithonPacket;
import org.kubithon.replicate.replication.protocol.PlayerConnectionKubicket;
import org.kubithon.replicate.replication.protocol.PlayerEquipmentKubicket;

import java.util.Base64;

/**
 * @author Oscar Davis, troopy28
 * @since 1.0.0
 */
class ReplicationChannelHandler extends ChannelInboundHandlerAdapter {

    private ReplicatePlugin plugin = ReplicatePlugin.get();
    private Player player;

    ReplicationChannelHandler(Player player) {
        this.player = player;
        plugin.getLogger().info("Created a ReplicationChannelHandler for the player " + player.getDisplayName() + ".");

        // Then send the Redis Kubicket notifying the connection
        GameProfile playerProfile = ((CraftPlayer) player).getHandle().getProfile();

        PlayerConnectionKubicket connectionKubicket = new PlayerConnectionKubicket();
        connectionKubicket.setPlayerName(playerProfile.getName());
        connectionKubicket.setPlayerUuid(playerProfile.getId().toString());
        plugin.getMessageBroker().publish(
                BrokingConstant.REPLICATION_PATTERN.concat(String.valueOf(plugin.getServerId())).concat(player.getName()),
                Base64.getEncoder().encodeToString(connectionKubicket.serialize())
        );

        EntityEquipment playerEquipment = player.getEquipment();
        if (playerEquipment == null)
            return;

        // Send the player stuff
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PlayerEquipmentKubicket equipmentKubicket = new PlayerEquipmentKubicket();
            if (playerEquipment.getHelmet() != null)
                equipmentKubicket.setHelmetId((short) playerEquipment.getHelmet().getType().ordinal());
            if (playerEquipment.getChestplate() != null)
                equipmentKubicket.setChestId((short) playerEquipment.getChestplate().getType().ordinal());
            if (playerEquipment.getLeggings() != null)
                equipmentKubicket.setLeggingsId((short) playerEquipment.getLeggings().getType().ordinal());
            if (playerEquipment.getBoots() != null)
                equipmentKubicket.setBootsId((short) playerEquipment.getBoots().getType().ordinal());
            if (playerEquipment.getItemInMainHand() != null)
                equipmentKubicket.setMainHandId((short) playerEquipment.getItemInMainHand().getType().ordinal());
            if (playerEquipment.getItemInOffHand() != null)
                equipmentKubicket.setOffHandId((short) playerEquipment.getItemInOffHand().getType().ordinal());
            plugin.getMessageBroker().publish(
                    BrokingConstant.REPLICATION_PATTERN.concat(String.valueOf(plugin.getServerId())).concat(player.getName()),
                    Base64.getEncoder().encodeToString(equipmentKubicket.serialize())
            );
        }, 20);
    }

    // Called when a packet is received.
    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
        Packet<?> receivedPacket = (Packet<?>) msg;
        KubithonPacket kubicket = KubithonPacket.generateKubicket(receivedPacket);

        if (kubicket != null) { // The packet has been recognized
            plugin.getMessageBroker().publish(
                    BrokingConstant.REPLICATION_PATTERN.concat(String.valueOf(plugin.getServerId())).concat(player.getName()),
                    Base64.getEncoder().encodeToString(kubicket.serialize())
            );
        }

        super.channelRead(context, msg);
    }
}