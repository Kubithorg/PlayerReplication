package org.kubithon.replicate.broking.impl;

import org.kubithon.replicate.broking.Credentials;

/**
 * @author Oscar Davis
 * @since 1.0.0
 */
public abstract class AbstractCredentials implements Credentials
{

    protected String host;
    protected int port;
    protected String password;

    /**
     * Default constructor.
     *
     * @param host     the host.
     * @param port     the port.
     * @param password the password
     */
    public AbstractCredentials(String host, int port, String password)
    {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    @Override
    public String host()
    {
        return host;
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public String password()
    {
        return password;
    }

}
