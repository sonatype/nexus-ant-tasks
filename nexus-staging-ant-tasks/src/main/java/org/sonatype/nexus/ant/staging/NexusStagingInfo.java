/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ant.staging;

import java.io.File;

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
