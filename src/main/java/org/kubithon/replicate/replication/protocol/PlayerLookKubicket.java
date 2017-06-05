package org.kubithon.replicate.replication.protocol;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class PlayerLookKubicket extends KubithonPacket {
    private float pitch;
    private float yaw;

    PlayerLookKubicket() {
        super(KubicketType.PLAYER_LOOK);
    }

    @Override
    protected void composePacket() {
        writeFloat(pitch);
        writeFloat(yaw);
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
