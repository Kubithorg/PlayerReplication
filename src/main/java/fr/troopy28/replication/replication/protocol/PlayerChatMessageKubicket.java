package fr.troopy28.replication.replication.protocol;

/**
 * Creation: 19/11/2017.
 *
 * @author troopy28
 * @since 1.0.0
 */
public class PlayerChatMessageKubicket extends KubithonPacket {

    private String jsonMessage;

    public PlayerChatMessageKubicket() {
        super(KubicketType.PLAYER_CHAT_MESSAGE);
    }

    @Override
    protected void composePacket() {
        writeShort((short) getByteStringLength(jsonMessage));
        writeString(jsonMessage);
    }

    public String getJsonMessage() {
        return jsonMessage;
    }

    public void setJsonMessage(String jsonMessage) {
        this.jsonMessage = jsonMessage;
    }
}
