package fr.troopy28.replication.replication.protocol;

/**
 * Creation: 06/12/2017.
 *
 * @author troopy28
 * @since 1.0.0
 */
public class BlockChangedKubicket extends KubithonPacket {

    private short posX;
    private short posY;
    private short posZ;
    private short blockID;
    private boolean blockBreak;
    private String serializedNBT;


    public BlockChangedKubicket() {
        super(KubicketType.BLOCK_CHANGED);
    }

    @Override
    protected void composePacket() {
        writeShort(posX);
        writeShort(posY);
        writeShort(posZ);
        if (blockBreak) {
            writeByte((byte) 1);
        } else {
            writeByte((byte) 0);
            writeShort(blockID);
            writeShort((short) getByteStringLength(serializedNBT));
            writeString(serializedNBT);
        }
    }

    public short getPosX() {
        return posX;
    }

    public void setPosX(short posX) {
        this.posX = posX;
    }

    public short getPosY() {
        return posY;
    }

    public void setPosY(short posY) {
        this.posY = posY;
    }

    public short getPosZ() {
        return posZ;
    }

    public void setPosZ(short posZ) {
        this.posZ = posZ;
    }

    public String getSerializedNBT() {
        return serializedNBT;
    }

    public void setSerializedNBT(String serializedNBT) {
        this.serializedNBT = serializedNBT;
    }

    public short getBlockID() {
        return blockID;
    }

    public void setBlockID(short blockID) {
        this.blockID = blockID;
    }

    public boolean isBlockBreak() {
        return blockBreak;
    }

    public void setBlockBreak(boolean blockBreak) {
        this.blockBreak = blockBreak;
    }
}
