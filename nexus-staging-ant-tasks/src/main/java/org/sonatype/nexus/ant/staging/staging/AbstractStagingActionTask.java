package org.sonatype.nexus.ant.staging.staging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.ant.staging.AbstractStagingTask;
import org.sonatype.nexus.ant.staging.deploy.AbstractDeployTask;
import org.sonatype.nexus.client.srv.staging.StagingWorkflowV2Service;

import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Super class of Actions. These tasks are callable as part of the build, and will try to use the property file from
 * locally staged repository to get the repository ID if not configured directly. This way, you can integrate these
 * tasks in your build directly (ie. to release or promote even from build).
 * 
 * @author cstamas
 */
public abstract class AbstractStagingActionTask
    extends AbstractStagingTask
{
    /**
     * Specifies the staging repository ID (or multiple ones comma separated) on remote Nexus against which RC
     * staging action should happen. If not given, mojo will fail.
     */
    private String stagingRepositoryId;

    public void setStagingRepositoryId( String stagingRepositoryId )
    {
        this.stagingRepositoryId = stagingRepositoryId;
    }

    protected String[] getStagingRepositoryId()
        throws BuildException
    {
        String[] result = null;
        if ( stagingRepositoryId != null )
        {
            // explicitly configured either via config or CLI, use that
            result = StringUtils.split( stagingRepositoryId, "," );
        }
        else if ( getStagingDirectory() != null )
        {
            // try the properties file from the staging folder
            final File stagingRepositoryPropertiesFile =
                new File( getStagingDirectory(), AbstractDeployTask.STAGING_REPOSITORY_PROPERTY_FILE_NAME );
            // it will exist only if remote staging happened!
            if ( stagingRepositoryPropertiesFile.isFile() )
            {
                final Properties stagingRepositoryProperties = new Properties();
                FileInputStream fis;
                try
                {
                    fis = new FileInputStream( stagingRepositoryPropertiesFile );
                    stagingRepositoryProperties.load( fis );
                    result =
                        new String[] { stagingRepositoryProperties.getProperty( AbstractDeployTask.STAGING_REPOSITORY_ID ) };
                }
                catch ( IOException e )
                {
                    throw new BuildException( "Unexpected IO exception while loading up staging properties from "
                        + stagingRepositoryPropertiesFile.getAbsolutePath(), e );
                }
            }
        }

        // check did we get any result at all
        if ( result == null || result.length == 0 )
        {
            throw new BuildException( "The staging repository to operate against is not defined!" );
        }

        return result;
    }

    @Override
    public final void execute()
        throws BuildException
    {
        createTransport();

        final StagingWorkflowV2Service stagingWorkflow = getStagingWorkflowService();

        try
        {
            doExecute( stagingWorkflow );
        }
        catch ( UniformInterfaceException e )
        {
            // dump the response until no smarter error handling
            log( e.getResponse().getEntity( String.class ) );
            // fail the build
            throw new BuildException( "Could not perform action: "
                + e.getResponse().getClientResponseStatus().getReasonPhrase(), e );
        }
    }

    protected abstract void doExecute( final StagingWorkflowV2Service stagingWorkflow )
        throws BuildException;
}
