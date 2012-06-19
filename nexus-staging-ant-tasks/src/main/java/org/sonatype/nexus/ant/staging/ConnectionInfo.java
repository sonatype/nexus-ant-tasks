package org.sonatype.nexus.ant.staging;

/**
 * DTO for Nexus connection.
 * 
 * @author cstamas
 */
public class ConnectionInfo
{
    private String baseUrl;

    private Authentication authentication;

    private Proxy proxy;

    // attributes

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl( String baseUrl )
    {
        this.baseUrl = baseUrl;
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

    public Proxy getProxy()
    {
        return proxy;
    }

    public void add( Proxy proxy )
    {
        this.proxy = proxy;
    }
}
