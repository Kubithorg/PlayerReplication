package org.kubithon.replicate.replication.protocol;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketLoginInStart;
import net.minecraft.server.v1_9_R2.PacketPlayInFlying;
import org.apache.commons.lang.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author troopy28
 * @since 1.0.0
 */
public abstract class KubithonPacket {

    private KubicketType type;
    protected List<Byte> packetBytesList;

    protected KubithonPacket(KubicketType kubicketType) {
        type = kubicketType;
        packetBytesList = new ArrayList<>();
    }

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
    public static KubithonPacket generateKubicket(Packet<?> receivedPacket) {
        KubithonPacket finalKubicket = null;

        // Position only
        if (receivedPacket instanceof PacketPlayInFlying.PacketPlayInPosition) {
            PacketPlayInFlying.PacketPlayInPosition posPacket = (PacketPlayInFlying.PacketPlayInPosition) receivedPacket;
            PlayerPositionKubicket kubicket = new PlayerPositionKubicket();

            /* ATTENTION!! IN THE NEXT THREE LINES :
            * Calling the a, b or c method with a DOUBLE as argument makes it returns the x, y, or z coordinate,
            * respectively.
            * Calling the a or b method with a FLOAT as argument makes it returns the yaw and the pitch, respectively.
            */
            kubicket.setxPos((float) posPacket.a(0.0d)); // a() returns the x coordinate...
            kubicket.setyPos((float) posPacket.b(0.0d)); // b() returns the x coordinate...
            kubicket.setzPos((float) posPacket.c(0.0d)); // c() returns the x coordinate...
            finalKubicket = kubicket;
        }
        // Position and look
        else if (receivedPacket instanceof PacketPlayInFlying.PacketPlayInPositionLook) {
            PacketPlayInFlying.PacketPlayInPositionLook posLookPacket = (PacketPlayInFlying.PacketPlayInPositionLook) receivedPacket;
            PlayerPositionLookKubicket kubicket = new PlayerPositionLookKubicket();

            /* ATTENTION!! IN THE NEXT FIVE LINES :
            * Calling the a, b or c method with a DOUBLE as argument makes it returns the x, y, or z coordinate,
            * respectively.
            * Calling the a or b method with a FLOAT as argument makes it returns the yaw and the pitch, respectively.
            */
            kubicket.setxPos((float) posLookPacket.a(0.0d)); // a() returns the x coordinate...
            kubicket.setyPos((float) posLookPacket.b(0.0d)); // b() returns the x coordinate...
            kubicket.setzPos((float) posLookPacket.c(0.0d)); // c() returns the x coordinate...
            kubicket.setYaw(posLookPacket.a(0.0f));          // a() returns the yaw...
            kubicket.setPitch(posLookPacket.b(0.0f));        // b() returns the pitch...
            finalKubicket = kubicket;
        }
        // Look only
        else if (receivedPacket instanceof PacketPlayInFlying.PacketPlayInLook) {
            PacketPlayInFlying.PacketPlayInLook lookPacket = (PacketPlayInFlying.PacketPlayInLook) receivedPacket;
            PlayerLookKubicket kubicket = new PlayerLookKubicket();

            /* ATTENTION!! IN THE NEXT TWO LINES :
            * Calling the a or b method with a FLOAT as argument makes it returns the yaw and the pitch, respectively.
            */
            kubicket.setYaw(lookPacket.a(0.0f));          // a() returns the yaw...
            kubicket.setPitch(lookPacket.b(0.0f));       // b() returns the pitch...
            finalKubicket = kubicket;
        }
        // Connection
        else if (receivedPacket instanceof PacketLoginInStart) {
            PacketLoginInStart loginPacket = (PacketLoginInStart) receivedPacket;
            PlayerConnectionKubicket kubicket = new PlayerConnectionKubicket();
            GameProfile playerProfile = loginPacket.a();
            kubicket.setPlayerName(playerProfile.getName());
            kubicket.setPlayerUuid(playerProfile.getId().toString());
            finalKubicket = kubicket;
        }

        return finalKubicket;
    }

    protected abstract void composePacket();

    public byte[] serialize() {
        composePacket();
        return ArrayUtils.toPrimitive(
                packetBytesList.toArray(new Byte[packetBytesList.size()])
        );
    }

    public static KubithonPacket deserialize(byte[] packetBytes) {
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
            default:
                return null;
        }
    }

    private static PlayerConnectionKubicket deserializeConnectionKubicket(byte[] packetBytes) {
        byte state = packetBytes[2];

        byte[] uuidBytes = Arrays.copyOfRange(packetBytes, 3, 39); // 3 + 36 = 39. UUID has 36 characters.
        String uuid = new String(uuidBytes, StandardCharsets.UTF_8);

        byte pseudoLength = packetBytes[39];
        byte[] pseudoBytes = Arrays.copyOfRange(packetBytes, 40, 40 + pseudoLength);
        String pseudo = new String(pseudoBytes, StandardCharsets.UTF_8);

        PlayerConnectionKubicket connectionKubicket = new PlayerConnectionKubicket();
        connectionKubicket.setPlayerUuid(uuid);
        connectionKubicket.setPlayerName(pseudo);
        connectionKubicket.setState(state);

        return connectionKubicket;
    }

    private static PlayerLookKubicket deserializeLookKubicket(byte[] packetBytes) {
        byte[] pitchBytes = Arrays.copyOfRange(packetBytes, 2, 6);
        float pitch = KubithonPacket.byteArrayToFloat(pitchBytes);

        byte[] yawBytes = Arrays.copyOfRange(packetBytes, 6, 10);
        float yaw = KubithonPacket.byteArrayToFloat(yawBytes);

        PlayerLookKubicket lookKubicket = new PlayerLookKubicket();
        lookKubicket.setPitch(pitch);
        lookKubicket.setYaw(yaw);

        return lookKubicket;
    }

    private static PlayerPositionKubicket deserializePositionKubicket(byte[] packetBytes) {
        byte[] xBytes = Arrays.copyOfRange(packetBytes, 2, 6);
        float x = KubithonPacket.byteArrayToFloat(xBytes);

        byte[] yBytes = Arrays.copyOfRange(packetBytes, 6, 10);
        float y = KubithonPacket.byteArrayToFloat(yBytes);

        byte[] zBytes = Arrays.copyOfRange(packetBytes, 10, 14);
        float z = KubithonPacket.byteArrayToFloat(zBytes);

        PlayerPositionKubicket positionKubicket = new PlayerPositionKubicket();
        positionKubicket.setxPos(x);
        positionKubicket.setyPos(y);
        positionKubicket.setzPos(z);

        return positionKubicket;
    }

    private static PlayerPositionLookKubicket deserializePositionLookKubicket(byte[] packetBytes) {
        byte[] xBytes = Arrays.copyOfRange(packetBytes, 2, 6);
        float x = KubithonPacket.byteArrayToFloat(xBytes);

        byte[] yBytes = Arrays.copyOfRange(packetBytes, 6, 10);
        float y = KubithonPacket.byteArrayToFloat(yBytes);

        byte[] zBytes = Arrays.copyOfRange(packetBytes, 10, 14);
        float z = KubithonPacket.byteArrayToFloat(zBytes);

        byte[] pitchBytes = Arrays.copyOfRange(packetBytes, 14, 18);
        float pitch = KubithonPacket.byteArrayToFloat(pitchBytes);

        byte[] yawBytes = Arrays.copyOfRange(packetBytes, 18, 22);
        float yaw = KubithonPacket.byteArrayToFloat(yawBytes);

        PlayerPositionLookKubicket positionLookKubicket = new PlayerPositionLookKubicket();
        positionLookKubicket.setxPos(x);
        positionLookKubicket.setyPos(y);
        positionLookKubicket.setzPos(z);
        positionLookKubicket.setPitch(pitch);
        positionLookKubicket.setYaw(yaw);

        return positionLookKubicket;
    }

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

    public byte booleanToByte(boolean value) {
        return (byte) (value ? 1 : 0);
    }

    public boolean byteToBoolean(byte value) {
        return value == 1;
    }

    public byte[] shortToByteArray(short value) {
        return new byte[]{(byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF)};
    }

    public static short byteArrayToShort(byte[] value) {
        return (short) (((value[0] & 0xFF) << 8) | (value[1] & 0xFF));
    }

    public byte[] longToByteArray(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public byte[] floatToByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static float byteArrayToFloat(byte[] value) {
        return ByteBuffer.wrap(value).getFloat();
    }

    public static double byteArrayToDouble(byte[] value) {
        return ByteBuffer.wrap(value).getDouble();
    }

    public byte[] doubleToByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public byte[] integerToByteArray(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    public static int byteArrayToInteger(byte[] bytes) {
        ByteBuffer wrapped = ByteBuffer.wrap(bytes); // big-endian by default
        return wrapped.getInt(); // 1
    }

    /**
     * @param string The string to get the byte[] length.
     * @return Returns the length of a string once it would've been converted into a bytes array.
     */
    public int getByteStringLength(String string) {
        return string.getBytes(StandardCharsets.UTF_8).length;
    }

    // </editor-fold>
}