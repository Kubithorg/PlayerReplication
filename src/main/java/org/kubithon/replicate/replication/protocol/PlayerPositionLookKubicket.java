package org.kubithon.replicate.replication.protocol;

import org.msgpack.annotation.Message;

/**
 * @author troopy28
 * @since 1.0.0
 */
@Message
public class PlayerPositionLookKubicket extends KubithonPacket{
    public float xPos;
    public float yPos;
    public float zPos;
    public float pitch;
    public float yaw;
}
