package org.kubithon.replicate.broking;

/**
 * @author Oscar Davis
 * @since 1.0.0
 */
public class BrokingConstant
{

    /**
     * The pattern used for replications. The topic depends on the packet that has been sent.
     * The part after the colon is the name of the player.
     */
    public static final String REPLICATION_TOPIC = "replicate:";

    private BrokingConstant()
    {
    }

}
