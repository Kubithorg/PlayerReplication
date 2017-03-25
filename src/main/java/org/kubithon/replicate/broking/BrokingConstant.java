package org.kubithon.replicate.broking;

/**
 * @author Oscar Davis
 * @since 1.0.0
 */
public class BrokingConstant
{

    /**
     * The pattern used for replications. The topic depends on the packet that has been sent.
     * If packet's id is 3, the topic will be <pre>replicate:3</pre>, for instance.
     */
    public static final String REPLICATION_TOPIC = "replicate:*";

    private BrokingConstant()
    {
    }

}
