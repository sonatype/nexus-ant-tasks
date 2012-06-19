package org.sonatype.nexus.ant.staging;

/**
 * DTO for proxy to be used for Nexus connection.
 * 
 * @author cstamas
 */
public class Proxy
{
    private String host;

    private int port;

    private Authentication authentication;

    // attributes

    public String getHost()
    {
        return host;
    }

    public void setHost( String host )
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    // children/refs

    public Authentication getAuthentication()
    {
        return authentication;
    }

    public void add( Authentication authentication )
    {
        this.authentication = authentication;
    }
}
