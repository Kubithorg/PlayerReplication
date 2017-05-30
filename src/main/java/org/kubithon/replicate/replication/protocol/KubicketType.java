package org.kubithon.replicate.replication.protocol;

/**
 * @author troopy28
 * @since 1.0.0
 */
public enum KubicketType {

    PLAYER_CONNECTION((byte) 0x00),
    PLAYER_LOOK((byte) 0x1),
    PLAYER_POSITION((byte) 0x02),
    PLAYER_POSITION_LOOK((byte) 0x03);

    private byte id;

    KubicketType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }
}
