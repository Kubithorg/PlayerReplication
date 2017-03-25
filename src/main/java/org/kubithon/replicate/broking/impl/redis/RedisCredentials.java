package org.kubithon.replicate.broking.impl.redis;

import org.kubithon.replicate.broking.impl.AbstractCredentials;

/**
 * Redis-specific credentials.
 *
 * @author Oscar Davis
 * @since 1.0.0
 */
public class RedisCredentials extends AbstractCredentials
{

    public RedisCredentials(String host, int port, String password)
    {
        super(host, port, password);
    }

}
