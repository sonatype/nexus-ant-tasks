package org.sonatype.nexus.ant.staging;

import java.io.File;

import org.apache.tools.ant.Project;

/**
 * DTO for Nexus staging info. Example with all bells and whistles:
 * 
 * <pre>
 *   &lt;nexusStagingInfo stagingDirectory="/some/path/to/directory" &gt;
 *     &lt;projectInfo groupId="G" artifactId="A" version="V" /&gt;
 *     &lt;connectionInfo baseUrl="http://nexus.mycorp.com" &gt;
 *       &lt;authentication username="nexususer" password="nexussecret" /&gt;
 *       &lt;proxy host="proxy.mycorp.com" port="8080"&lt;
 *         &lt;authentication username="proxyUser" password="proxySecret" /&gt;
 *       &lt;/proxy&lt;
 *     &lt;/connectionInfo&gt;
 *   &lt;/nexusStagingInfo&gt;
 * </pre>
 * 
 * @author cstamas
 */
public class NexusStagingInfo
{
    private String refid;
    
    private File stagingDirectory;

    private ProjectInfo projectInfo;

    private ConnectionInfo connectionInfo;

    // attributes

    public String getRefid()
    {
        return refid;
    }

    public void setRefid( String refid )
    {
        this.refid = refid;
    }

    public File getStagingDirectory()
    {
        return stagingDirectory;
    }

    public void setStagingDirectory( File stagingDirectory )
    {
        this.stagingDirectory = stagingDirectory;
    }

    // children/refs

    public ProjectInfo getProjectInfo()
    {
        return projectInfo;
    }

    public void add( ProjectInfo projectInfo )
    {
        this.projectInfo = projectInfo;
    }

    public ConnectionInfo getConnectionInfo()
    {
        return connectionInfo;
    }

    public void add( ConnectionInfo connectionInfo )
    {
        this.connectionInfo = connectionInfo;
    }
}
