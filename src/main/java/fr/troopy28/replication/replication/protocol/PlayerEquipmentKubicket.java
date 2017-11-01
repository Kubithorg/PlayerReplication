package fr.troopy28.replication.replication.protocol;

/**
 * The packet carrying a sponsor's stuff. All the IDs are the Bukkit IDs, not the NMS ones (but maybe they're the same,
 * dunno).
 *
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

    private short helmetMeta;
    private short chestMeta;
    private short leggingsMeta;
    private short bootsMeta;
    private short mainHandMeta;
    private short offHandMeta;

    private boolean helmetEnchanted;
    private boolean chestEnchanted;
    private boolean leggingsEnchanted;
    private boolean bootsEnchanted;
    private boolean mainHandEnchanted;
    private boolean offHandEnchanted;

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

        writeShort(helmetMeta);
        writeShort(chestMeta);
        writeShort(leggingsMeta);
        writeShort(bootsMeta);
        writeShort(mainHandMeta);
        writeShort(offHandMeta);

        writeByte((byte) (helmetEnchanted ? 1 : 0));
        writeByte((byte) (chestEnchanted ? 1 : 0));
        writeByte((byte) (leggingsEnchanted ? 1 : 0));
        writeByte((byte) (bootsEnchanted ? 1 : 0));
        writeByte((byte) (mainHandEnchanted ? 1 : 0));
        writeByte((byte) (offHandEnchanted ? 1 : 0));
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

    public int getHelmetMeta() {
        return helmetMeta;
    }

    public void setHelmetMeta(int helmetMeta) {
        this.helmetMeta = (short)helmetMeta;
    }

    public int getChestMeta() {
        return chestMeta;
    }

    public void setChestMeta(int chestMeta) {
        this.chestMeta = (short)chestMeta;
    }

    public int getLeggingsMeta() {
        return leggingsMeta;
    }

    public void setLeggingsMeta(int leggingsMeta) {
        this.leggingsMeta = (short)leggingsMeta;
    }

    public int getBootsMeta() {
        return bootsMeta;
    }

    public void setBootsMeta(int bootsMeta) {
        this.bootsMeta =(short) bootsMeta;
    }

    public int getMainHandMeta() {
        return mainHandMeta;
    }

    public void setMainHandMeta(int mainHandMeta) {
        this.mainHandMeta = (short)mainHandMeta;
    }

    public int getOffHandMeta() {
        return offHandMeta;
    }

    public void setOffHandMeta(int offHandMeta) {
        this.offHandMeta = (short)offHandMeta;
    }

    public boolean isHelmetEnchanted() {
        return helmetEnchanted;
    }

    public void setHelmetEnchanted(boolean helmetEnchanted) {
        this.helmetEnchanted = helmetEnchanted;
    }

    public boolean isChestEnchanted() {
        return chestEnchanted;
    }

    public void setChestEnchanted(boolean chestEnchanted) {
        this.chestEnchanted = chestEnchanted;
    }

    public boolean isLeggingsEnchanted() {
        return leggingsEnchanted;
    }

    public void setLeggingsEnchanted(boolean leggingsEnchanted) {
        this.leggingsEnchanted = leggingsEnchanted;
    }

    public boolean isBootsEnchanted() {
        return bootsEnchanted;
    }

    public void setBootsEnchanted(boolean bootsEnchanted) {
        this.bootsEnchanted = bootsEnchanted;
    }

    public boolean isMainHandEnchanted() {
        return mainHandEnchanted;
    }

    public void setMainHandEnchanted(boolean mainHandEnchanted) {
        this.mainHandEnchanted = mainHandEnchanted;
    }

    public boolean isOffHandEnchanted() {
        return offHandEnchanted;
    }

    public void setOffHandEnchanted(boolean offHandEnchanted) {
        this.offHandEnchanted = offHandEnchanted;
    }
}