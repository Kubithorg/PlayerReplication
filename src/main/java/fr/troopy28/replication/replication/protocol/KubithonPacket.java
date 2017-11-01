package fr.troopy28.replication.replication.protocol;


import com.google.common.primitives.Ints;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The base class of all the kubithon packets used in this plugin. It contains the functions for converting the data
 * types into their byte array representation, the type of this Kubicket (shorthand for kubithon-packet), and a way
 * to build their byte array representation very easily.
 *
 * @author troopy28
 * @since 1.0.0
 */
public abstract class KubithonPacket {

    /**
     * The type of this kubicket.
     */
    private KubicketType type;
    /**
     * The list of the bytes of this packet, that will then be packet in an array after the serialization job.
     */
    private List<Byte> packetBytesList;

    /**
     * Package-local constructor. Creates the {@link ArrayList} of the bytes of this packet, that will be used to
     * serialize the packet.
     *
     * @param kubicketType The type of this kubicket.
     */
    KubithonPacket(KubicketType kubicketType) {
        type = kubicketType;
        packetBytesList = new ArrayList<>();
    }

    /**
     * @return Returns the type of this kubicket.
     * @see KubicketType
     */
    public KubicketType getType() {
        return type;
    }

    /**
     * Creates a kubicket to send the data over the servers.
     *
     * @param receivedPacket The packet that has been received.
     * @return Return the created kubicket corresponding to the specified packet. Null if the packet isn't a packet
     * to replicate.
     */
    public static KubithonPacket generateKubicket(Packet<?> receivedPacket, EntityPlayerMP player) {
        KubithonPacket finalKubicket = null;

        // Position only
        if (receivedPacket instanceof CPacketPlayer.Position) {
            CPacketPlayer.Position posPacket = (CPacketPlayer.Position) receivedPacket;
            PlayerPositionKubicket kubicket = new PlayerPositionKubicket();

            kubicket.setxPos((float) posPacket.getX(player.posX));
            kubicket.setyPos((float) posPacket.getY(player.posY));
            kubicket.setzPos((float) posPacket.getZ(player.posZ));
            kubicket.setOnGround(posPacket.isOnGround());
            finalKubicket = kubicket;
        }
        // Position and look
        else if (receivedPacket instanceof CPacketPlayer.PositionRotation) {
            CPacketPlayer.PositionRotation posLookPacket = (CPacketPlayer.PositionRotation) receivedPacket;
            PlayerPositionLookKubicket kubicket = new PlayerPositionLookKubicket();

            kubicket.setxPos((float) posLookPacket.getX(player.posX));
            kubicket.setyPos((float) posLookPacket.getY(player.posY));
            kubicket.setzPos((float) posLookPacket.getZ(player.posZ));
            kubicket.setYaw(posLookPacket.getYaw(player.rotationYaw));
            kubicket.setPitch(posLookPacket.getPitch(player.rotationPitch));
            kubicket.setOnGround(posLookPacket.isOnGround());
            finalKubicket = kubicket;
        }
        // Look only
        else if (receivedPacket instanceof CPacketPlayer.Rotation) {
            CPacketPlayer.Rotation lookPacket = (CPacketPlayer.Rotation) receivedPacket;
            PlayerLookKubicket kubicket = new PlayerLookKubicket();

            kubicket.setYaw(lookPacket.getYaw(player.rotationYaw));
            kubicket.setPitch(lookPacket.getPitch(player.rotationPitch));
            finalKubicket = kubicket;
        } else if (receivedPacket instanceof CPacketAnimation) {
            CPacketAnimation handPacket = (CPacketAnimation) receivedPacket;
            PlayerHandAnimationKubicket kubicket = new PlayerHandAnimationKubicket();
            kubicket.setHand((byte) handPacket.getHand().ordinal());
            finalKubicket = kubicket;
        }
        return finalKubicket;
    }

    /**
     * All packets have this method, which enables them to do their specific serialization job.
     */
    protected abstract void composePacket();

    /**
     * Writes the ID of the packet in a single byte, then call the serialization method specific to this packet, and
     * pack all these data into a byte array that is returned.
     *
     * @return The byte array corresponding to this packet.
     */
    public byte[] serialize() {
        writeByte(type.getId());
        composePacket();
        return ArrayUtils.toPrimitive(
                packetBytesList.toArray(new Byte[packetBytesList.size()])
        );
    }

    /**
     * Deserializes the specified byte array in order to generate an object extending from {@link KubithonPacket}. If the
     * packet isn't recognized, then this function returns null.
     *
     * @param packetBytes The byte array of the kubicket.
     * @return Returns an object extending from {@link KubithonPacket} embedding the data that was in the specified byte
     * array.
     */
    public static KubithonPacket deserialize(byte[] packetBytes) { //NOSONAR : more than 10 packets so... shut up Sonar ;)
        byte packetId = packetBytes[0];
        switch (KubicketType.fromId(packetId)) {
            case PLAYER_CONNECTION:
                return deserializeConnectionKubicket(packetBytes);
            case PLAYER_LOOK:
                return deserializeLookKubicket(packetBytes);
            case PLAYER_POSITION:
                return deserializePositionKubicket(packetBytes);
            case PLAYER_POSITION_LOOK:
                return deserializePositionLookKubicket(packetBytes);
            case PLAYER_HAND_ANIMATION:
                return deserializeHandAnimKubicket(packetBytes);
            case PLAYER_EQUIPMENT:
                return deserializePlayerEquipmentKubicket(packetBytes);
            default:
                return null;
        }
    }

    // <editor-fold desc="Functions for deserializing the specified byte array, according to the ID.">

    // The code is self-documenting in the following function: only byte array conversions etc.

    private static PlayerHandAnimationKubicket deserializeHandAnimKubicket(byte[] packetBytes) {
        byte hand = packetBytes[1];
        PlayerHandAnimationKubicket handAnimKubicket = new PlayerHandAnimationKubicket();
        handAnimKubicket.setHand(hand);
        return handAnimKubicket;
    }

    private static PlayerConnectionKubicket deserializeConnectionKubicket(byte[] packetBytes) {
        byte state = packetBytes[1];

        byte[] uuidBytes = Arrays.copyOfRange(packetBytes, 2, 38); // 2 + 36 = 38. UUID has 36 characters.
        String uuid = new String(uuidBytes, StandardCharsets.UTF_8);

        byte pseudoLength = packetBytes[38];
        byte[] pseudoBytes = Arrays.copyOfRange(packetBytes, 39, 39 + pseudoLength);
        String pseudo = new String(pseudoBytes, StandardCharsets.UTF_8);

        int idx = 39 + pseudoLength;

        PlayerConnectionKubicket connectionKubicket = new PlayerConnectionKubicket();
        connectionKubicket.setPlayerUuid(uuid);
        connectionKubicket.setPlayerName(pseudo);
        connectionKubicket.setState(state);

        // If it is a connection
        if (state == 0) {
            // The skin
            byte[] skinLengthBytes = Arrays.copyOfRange(packetBytes, idx, idx + 2);
            idx += 2;
            short skinLength = byteArrayToShort(skinLengthBytes);
            byte[] skinBytes = Arrays.copyOfRange(packetBytes, idx, idx + skinLength);
            String skin = new String(skinBytes, StandardCharsets.UTF_8);
            idx += skinLength;

            // The signature
            byte[] signatureLengthBytes = Arrays.copyOfRange(packetBytes, idx, idx + 2);
            idx += 2;
            short signatureLength = byteArrayToShort(signatureLengthBytes);
            byte[] signatureBytes = Arrays.copyOfRange(packetBytes, idx, idx + signatureLength);
            String signature = new String(signatureBytes, StandardCharsets.UTF_8);
            //idx += signatureLength;
            connectionKubicket.setPlayerSkin(skin);
            connectionKubicket.setPlayerSkinSignature(signature);
        }
        return connectionKubicket;
    }

    private static PlayerLookKubicket deserializeLookKubicket(byte[] packetBytes) {
        byte pitchByte = packetBytes[1];
        byte yawByte = packetBytes[2];

        PlayerLookKubicket lookKubicket = new PlayerLookKubicket();
        lookKubicket.setPitchByte(pitchByte);
        lookKubicket.setYawByte(yawByte);

        return lookKubicket;
    }

    private static PlayerPositionKubicket deserializePositionKubicket(byte[] packetBytes) {
        byte[] xBytes = Arrays.copyOfRange(packetBytes, 1, 5);
        float x = KubithonPacket.byteArrayToFloat(xBytes);

        byte[] yBytes = Arrays.copyOfRange(packetBytes, 5, 9);
        float y = KubithonPacket.byteArrayToFloat(yBytes);

        byte[] zBytes = Arrays.copyOfRange(packetBytes, 9, 13);
        float z = KubithonPacket.byteArrayToFloat(zBytes);

        PlayerPositionKubicket positionKubicket = new PlayerPositionKubicket();
        positionKubicket.setxPos(x);
        positionKubicket.setyPos(y);
        positionKubicket.setzPos(z);

        return positionKubicket;
    }

    private static PlayerPositionLookKubicket deserializePositionLookKubicket(byte[] packetBytes) {
        byte[] xBytes = Arrays.copyOfRange(packetBytes, 1, 5);
        float x = KubithonPacket.byteArrayToFloat(xBytes);

        byte[] yBytes = Arrays.copyOfRange(packetBytes, 5, 9);
        float y = KubithonPacket.byteArrayToFloat(yBytes);

        byte[] zBytes = Arrays.copyOfRange(packetBytes, 9, 13);
        float z = KubithonPacket.byteArrayToFloat(zBytes);

        byte pitchByte = packetBytes[13];
        byte yawByte = packetBytes[14];

        PlayerPositionLookKubicket positionLookKubicket = new PlayerPositionLookKubicket();
        positionLookKubicket.setxPos(x);
        positionLookKubicket.setyPos(y);
        positionLookKubicket.setzPos(z);
        positionLookKubicket.setPitch(getAngleFromByte(pitchByte));
        positionLookKubicket.setYaw(getAngleFromByte(yawByte));

        return positionLookKubicket;
    }

    private static PlayerEquipmentKubicket deserializePlayerEquipmentKubicket(byte[] packetBytes) {
        byte[] helmetBytes = Arrays.copyOfRange(packetBytes, 1, 3);
        short helmetId = KubithonPacket.byteArrayToShort(helmetBytes);

        byte[] chestBytes = Arrays.copyOfRange(packetBytes, 3, 5);
        short chestId = KubithonPacket.byteArrayToShort(chestBytes);

        byte[] leggingsBytes = Arrays.copyOfRange(packetBytes, 5, 7);
        short leggingsId = KubithonPacket.byteArrayToShort(leggingsBytes);

        byte[] bootsBytes = Arrays.copyOfRange(packetBytes, 7, 9);
        short bootsId = KubithonPacket.byteArrayToShort(bootsBytes);

        byte[] mainHandBytes = Arrays.copyOfRange(packetBytes, 9, 11);
        short mainHandId = KubithonPacket.byteArrayToShort(mainHandBytes);

        byte[] offHandBytes = Arrays.copyOfRange(packetBytes, 11, 13);
        short offHandId = KubithonPacket.byteArrayToShort(offHandBytes);

        byte[] helmetMetaBytes = Arrays.copyOfRange(packetBytes, 13, 15);
        short helmetMeta = KubithonPacket.byteArrayToShort(helmetMetaBytes);

        byte[] chestMetaBytes = Arrays.copyOfRange(packetBytes, 15, 17);
        int chestMeta = KubithonPacket.byteArrayToShort(chestMetaBytes);

        byte[] leggingsMetaBytes = Arrays.copyOfRange(packetBytes, 17, 19);
        int leggingsMeta = KubithonPacket.byteArrayToShort(leggingsMetaBytes);

        byte[] bootsMetaBytes = Arrays.copyOfRange(packetBytes, 19, 21);
        int bootsMeta = KubithonPacket.byteArrayToShort(bootsMetaBytes);

        byte[] mainHandMetaBytes = Arrays.copyOfRange(packetBytes, 21, 23);
        int mainHandMeta = KubithonPacket.byteArrayToShort(mainHandMetaBytes);

        byte[] offHandMetaBytes = Arrays.copyOfRange(packetBytes, 23, 25);
        int offHandMeta = KubithonPacket.byteArrayToShort(offHandMetaBytes);

        boolean helmetEnchanted = packetBytes[25] == 1;
        boolean chestEnchanted = packetBytes[26] == 1;
        boolean leggingsEnchanted = packetBytes[27] == 1;
        boolean bootsEnchanted = packetBytes[28] == 1;
        boolean mainHandEnchanted = packetBytes[29] == 1;
        boolean offHandEnchanted = packetBytes[30] == 1;

        PlayerEquipmentKubicket equipmentKubicket = new PlayerEquipmentKubicket();

        // IDs
        equipmentKubicket.setHelmetId(helmetId);
        equipmentKubicket.setChestId(chestId);
        equipmentKubicket.setLeggingsId(leggingsId);
        equipmentKubicket.setBootsId(bootsId);
        equipmentKubicket.setMainHandId(mainHandId);
        equipmentKubicket.setOffHandId(offHandId);

        // METAs
        equipmentKubicket.setHelmetMeta(helmetMeta);
        equipmentKubicket.setChestMeta(chestMeta);
        equipmentKubicket.setLeggingsMeta(leggingsMeta);
        equipmentKubicket.setBootsMeta(bootsMeta);
        equipmentKubicket.setMainHandMeta(mainHandMeta);
        equipmentKubicket.setOffHandMeta(offHandMeta);

        // Enchantments
        equipmentKubicket.setHelmetEnchanted(helmetEnchanted);
        equipmentKubicket.setChestEnchanted(chestEnchanted);
        equipmentKubicket.setLeggingsEnchanted(leggingsEnchanted);
        equipmentKubicket.setBootsEnchanted(bootsEnchanted);
        equipmentKubicket.setMainHandEnchanted(mainHandEnchanted);
        equipmentKubicket.setOffHandEnchanted(offHandEnchanted);

        return equipmentKubicket;
    }

    // </editor-fold>

    // <editor-fold desc="Shorthand for writing bytes in the packet">

    void writeByte(byte var) {
        packetBytesList.add(var);
    }

    void writeBytes(byte[] var) {
        for (byte b : var) {
            packetBytesList.add(b);
        }
    }

    void writeString(String var) {
        for (byte b : var.getBytes(StandardCharsets.UTF_8)) {
            packetBytesList.add(b);
        }
    }

    void writeFloat(float var) {
        for (byte b : floatToByteArray(var)) {
            packetBytesList.add(b);
        }
    }

    void writeShort(short var) {
        for (byte b : shortToByteArray(var)) {
            packetBytesList.add(b);
        }
    }

    void writeLong(long var) {
        for (byte b : longToByteArray(var)) {
            packetBytesList.add(b);
        }
    }

    void writeDouble(double var) {
        for (byte b : doubleToByteArray(var)) {
            packetBytesList.add(b);
        }
    }

    void writeBoolean(boolean var) {
        packetBytesList.add(booleanToByte(var));
    }

    void writeInteger(int var) {
        for (byte b : integerToByteArray(var)) {
            packetBytesList.add(b);
        }
    }
    // </editor-fold>

    // <editor-fold desc="Static utils methods">

    byte booleanToByte(boolean value) {
        return (byte) (value ? 1 : 0);
    }

    public boolean byteToBoolean(byte value) {
        return value == 1;
    }

    byte[] shortToByteArray(short value) {
        return new byte[]{(byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF)};
    }

    public static short byteArrayToShort(byte[] value) {
        return (short) (((value[0] & 0xFF) << 8) | (value[1] & 0xFF));
    }

    byte[] longToByteArray(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    byte[] floatToByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    static float byteArrayToFloat(byte[] value) {
        return ByteBuffer.wrap(value).getFloat();
    }

    public static double byteArrayToDouble(byte[] value) {
        return ByteBuffer.wrap(value).getDouble();
    }

    byte[] doubleToByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    byte[] integerToByteArray(int value) {
        return Ints.toByteArray(value);
    }

    public static int byteArrayToInteger(byte[] bytes) {
        return Ints.fromByteArray(bytes);
    }

    /**
     * @param string The string to get the byte[] length.
     * @return Returns the length of a string once it would've been converted into a bytes array.
     */
    public int getByteStringLength(String string) {
        return string.getBytes(StandardCharsets.UTF_8).length;
    }

    public static byte getByteFromAngle(float angle) {
        return (byte) ((angle * 256) / 360);
    }

    public static float getAngleFromByte(byte val) {
        return (float) ((val * 360) / 256);
    }

    // </editor-fold>
}