package org.kubithon.replicate.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EntityEquipment;
import org.kubithon.replicate.ReplicatePlugin;
import org.kubithon.replicate.broking.BrokingConstant;
import org.kubithon.replicate.replication.protocol.PlayerEquipmentKubicket;

import java.util.Base64;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class InventoryClickListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST) // the last handler that should be called
    public void onInventoryClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (!isArmorSlot(slot))
            return;

        Player holder = (Player) event.getInventory().getHolder();
        if (!(holder.hasPermission("kubithon.replicate") || holder.isOp()) /*isOp() -> for debugging */)
            return;

        Bukkit.getScheduler().runTaskLater(ReplicatePlugin.get(), () -> sendUpdatedStuff(holder), 20);
    }

    private void sendUpdatedStuff(Player holder) {
        EntityEquipment playerEquipment = holder.getEquipment();

        // Send the player stuff
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

        ReplicatePlugin.get().getMessageBroker().publish(
                BrokingConstant.REPLICATION_PATTERN.concat(
                        String.valueOf(ReplicatePlugin.get().getServerId()))
                        .concat(holder.getName()),
                Base64.getEncoder().encodeToString(equipmentKubicket.serialize())
        );
    }

    private boolean isArmorSlot(int slot) {
        return slot <= 8 && slot >= 5;
    }

}
