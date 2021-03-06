package fr.troopy28.replication.replication.protocol;

/**
 * @author troopy28
 * @since 1.0.0
 * <p>
 * The packet that is sent by a server when a player that should be replicated has joined this server. All other servers
 * receive it by listening the Redis replication channel. This packet contains the connection state (0 = connection,
 * 1 = disconnection) as a byte, the UUID of the player encoded as a string and its pseudo encoded as string as well.
 */
public class PlayerConnectionKubicket extends KubithonPacket {

    private byte state; // 0 = connection; 1 = disconnection
    private String playerName;
    private String playerUuid;
    private String playerSkin;
    private String playerSkinSignature;

    public PlayerConnectionKubicket() {
        super(KubicketType.PLAYER_CONNECTION);
    }

    @Override
    protected void composePacket() {
        writeByte(state);
        writeString(playerUuid);
        writeByte((byte) getByteStringLength(playerName));
        writeString(playerName);
        if (state == 1)
            return;
        // Write this only for the connection
        writeShort((short) getByteStringLength(playerSkin));
        writeString(playerSkin);
        writeShort((short) getByteStringLength(playerSkinSignature));
        writeString(playerSkinSignature);
    }

    /**
     * @return Returns the base64 string storing the skin of the player.
     */
    public String getPlayerSkin() {
        return playerSkin;
    }

    /**
     * Set the base64 string storing the skin of the player.
     *
     * @param playerSkin The base64 string storing the skin of the player.
     */
    public void setPlayerSkin(String playerSkin) {
        this.playerSkin = playerSkin;
    }

    /**
     * @return Returns the signature of the skin property.
     */
    public String getPlayerSkinSignature() {
        return playerSkinSignature;
    }

    /**
     * Set the signature of the skin property.
     *
     * @param playerSkinSignature Signature of the skin property.
     */
    public void setPlayerSkinSignature(String playerSkinSignature) {
        this.playerSkinSignature = playerSkinSignature;
    }

    /**
     * @return Returns the name / pseudo of the player that has just joined the server.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Set the name of the player that has just joined the server.
     *
     * @param playerName Name / pseudo of the player.
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return Returns the {@link String} representation of the {@link java.util.UUID} of the player that has just
     * joined the server.
     */
    public String getPlayerUuid() {
        return playerUuid;
    }

    /**
     * Set the {@link java.util.UUID} of the player that has just joined the server.
     *
     * @param playerUuid {@link String} representation of the UUID of the player.
     */
    public void setPlayerUuid(String playerUuid) {
        this.playerUuid = playerUuid;
    }

    /**
     * @return Returns the state of the packet. 0 means this packet informs of a connection, and 1 means this packet informs of
     * a disconnection.
     */
    public byte getState() {
        return state;
    }

    /**
     * Set the state of the packet. 0 means this packet informs of a connection, and 1 means this packet informs of
     * a disconnection.
     *
     * @param state The state of the packet.
     */
    public void setState(byte state) {
        this.state = state;
    }
}