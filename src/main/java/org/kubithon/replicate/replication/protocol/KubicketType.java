package org.kubithon.replicate.replication.protocol;

/**
 * @author troopy28
 * @since 1.0.0
 */
public enum KubicketType {

    PLAYER_CONNECTION((byte) 0x00),
    PLAYER_LOOK((byte) 0x1),
    PLAYER_POSITION((byte) 0x02),
    PLAYER_POSITION_LOOK((byte) 0x03),
    PLAYER_HAND_ANIMATION((byte) 0x04),
    PLAYER_EQUIPMENT((byte) 0x05),

    UNDEFINED((byte) 0xAA);

    private byte id;

    KubicketType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static KubicketType fromId(byte id) { //NOSONAR: more than 10 checks to do.........
        switch (id) {
            case 0x00:
                return PLAYER_CONNECTION;
            case 0x01:
                return PLAYER_LOOK;
            case 0x02:
                return PLAYER_POSITION;
            case 0x03:
                return PLAYER_POSITION_LOOK;
            case 0x04:
                return PLAYER_HAND_ANIMATION;
            case 0x05:
                return PLAYER_EQUIPMENT;
            default:
                return UNDEFINED;
        }
    }
}
