package org.kubithon.replicate.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicatedPlayer {

    private static final EntityType PLAYER_TYPE = EntityType.PLAYER;
    private NPC npc;

    public ReplicatedPlayer() {

    }

    public void spawn(String name, Location location) {
        if(this.npc != null)
            this.npc.destroy();
        this.npc = CitizensAPI.getNPCRegistry().createNPC(PLAYER_TYPE, name);
        this.npc.spawn(location);
    }

    public void setLocation(Location newLoc) {
        if(this.npc.isSpawned())
            this.npc.getBukkitEntity().teleport(newLoc);
    }

    public void goTo(Location destination) {
        if(this.npc.isSpawned())
            this.npc.getNavigator().setTarget(destination);
    }
}
