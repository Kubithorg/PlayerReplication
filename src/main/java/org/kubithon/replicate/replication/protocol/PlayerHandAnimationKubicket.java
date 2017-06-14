package org.kubithon.replicate.replication.protocol;

import net.minecraft.server.v1_9_R2.EnumHand;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class PlayerHandAnimationKubicket extends KubithonPacket {

    private byte hand;

    PlayerHandAnimationKubicket() {
        super(KubicketType.PLAYER_HAND_ANIMATION);
    }

    @Override
    protected void composePacket() {
        writeByte(hand);
    }

    public void setHand(EnumHand hand) {
        this.hand = hand == EnumHand.MAIN_HAND ? 0 : (byte) 1;
    }

    public void setHand(byte hand) {
        this.hand = hand;
    }

    public EnumHand getHand() {
        return hand == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }
}
