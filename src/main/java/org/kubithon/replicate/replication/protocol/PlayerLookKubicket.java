package org.kubithon.replicate.replication.protocol;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class PlayerLookKubicket extends KubithonPacket {
    private float pitch;
    private float yaw;
    private byte pitchByte;
    private byte yawByte;

    PlayerLookKubicket() {
        super(KubicketType.PLAYER_LOOK);
    }

    @Override
    protected void composePacket() {
        writeByte(KubithonPacket.getByteFromAngle(pitch));
        writeByte(KubithonPacket.getByteFromAngle(yaw));
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

    public byte getPitchByte() {
        return pitchByte;
    }

    public void setPitchByte(byte pitchByte) {
        this.pitchByte = pitchByte;
    }

    public byte getYawByte() {
        return yawByte;
    }

    public void setYawByte(byte yawByte) {
        this.yawByte = yawByte;
    }
}
