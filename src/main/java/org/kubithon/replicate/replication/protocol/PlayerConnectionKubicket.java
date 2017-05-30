package org.kubithon.replicate.replication.protocol;

import org.msgpack.annotation.Message;

/**
 * @author troopy28
 * @since 1.0.0
 */
@Message
public class PlayerConnectionKubicket extends KubithonPacket {
    public String playerName;
    public String playerUuid;
}
