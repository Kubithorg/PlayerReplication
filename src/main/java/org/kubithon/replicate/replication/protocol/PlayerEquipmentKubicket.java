package org.kubithon.replicate.replication.protocol;

/**
 * @author troopy28
 * @since 1.0.0
 */
public class PlayerEquipmentKubicket extends KubithonPacket {

    private short helmetId;
    private short chestId;
    private short leggingsId;
    private short bootsId;
    private short mainHandId;
    private short offHandId;

    public PlayerEquipmentKubicket() {
        super(KubicketType.PLAYER_EQUIPMENT);
    }

    @Override
    protected void composePacket() {
        writeShort(helmetId);
        writeShort(chestId);
        writeShort(leggingsId);
        writeShort(bootsId);
        writeShort(mainHandId);
        writeShort(offHandId);
    }

    public short getHelmetId() {
        return helmetId;
    }

    public void setHelmetId(short helmetId) {
        this.helmetId = helmetId;
    }

    public short getChestId() {
        return chestId;
    }

    public void setChestId(short chestId) {
        this.chestId = chestId;
    }

    public short getLeggingsId() {
        return leggingsId;
    }

    public void setLeggingsId(short leggingsId) {
        this.leggingsId = leggingsId;
    }

    public short getBootsId() {
        return bootsId;
    }

    public void setBootsId(short bootsId) {
        this.bootsId = bootsId;
    }

    public short getMainHandId() {
        return mainHandId;
    }

    public void setMainHandId(short mainHandId) {
        this.mainHandId = mainHandId;
    }

    public short getOffHandId() {
        return offHandId;
    }

    public void setOffHandId(short offHandId) {
        this.offHandId = offHandId;
    }

}