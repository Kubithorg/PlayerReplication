package org.kubithon.replicate.replication.protocol;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class PlayerPositionKubicket extends KubithonPacket {
    private float xPos;
    private float yPos;
    private float zPos;

    PlayerPositionKubicket() {
        super(KubicketType.PLAYER_POSITION);
    }


    @Override
    protected void composePacket() {
        writeFloat(xPos);
        writeFloat(yPos);
        writeFloat(zPos);
    }

    public float getxPos() {
        return xPos;
    }

    void setxPos(float xPos) {
        this.xPos = xPos;
    }

    public float getyPos() {
        return yPos;
    }

    void setyPos(float yPos) {
        this.yPos = yPos;
    }

    public float getzPos() {
        return zPos;
    }

    void setzPos(float zPos) {
        this.zPos = zPos;
    }
}
