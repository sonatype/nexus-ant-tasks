/*
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
package org.sonatype.nexus.ant.staging.deploy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.ant.staging.AbstractStagingTask;
import org.sonatype.nexus.ant.staging.ErrorDumper;
import org.sonatype.nexus.client.core.NexusStatus;
import org.sonatype.nexus.client.core.exception.NexusClientErrorResponseException;

import com.sonatype.nexus.staging.client.Profile;
import com.sonatype.nexus.staging.client.ProfileMatchingParameters;
import com.sonatype.nexus.staging.client.StagingRuleFailuresException;
import com.sonatype.nexus.staging.client.StagingWorkflowV2Service;

/**
 * Abstract class for deploy related mojos.
 *
 * @author cstamas
 * @since 2.1
 */
public abstract class AbstractDeployTask
    extends AbstractStagingTask
{
    public static final String STAGING_REPOSITORY_PROPERTY_FILE_NAME = "stagingRepository.properties";

    public static final String STAGING_REPOSITORY_ID = "stagingRepository.id";

    public static final String STAGING_REPOSITORY_PROFILE_ID = "stagingRepository.profileId";

    public static final String STAGING_REPOSITORY_URL = "stagingRepository.url";

    public static final String STAGING_REPOSITORY_MANAGED = "stagingRepository.managed";

    // User configurable parameters

    /**
     * Controls whether the plugin remove or keep the staging repository that performed an IO exception during upload,
     * hence, it's contents are partial Defaults to {{false}}. If {{true}}, even in case of upload failure, the staging
     * repository (with partial content) will be left as is, left to the user to do whatever he wants.
     */
    private boolean keepStagingRepositoryOnFailure = false;

    /**
     * Set this to {@code true} to bypass staging repository closing at the workflow end.
     */
    private boolean skipStagingRepositoryClose = false;

    // methods

    public boolean isKeepStagingRepositoryOnFailure()
    {
        return keepStagingRepositoryOnFailure;
    }

    public void setKeepStagingRepositoryOnFailure( boolean keepStagingRepositoryOnFailure )
    {
        this.keepStagingRepositoryOnFailure = keepStagingRepositoryOnFailure;
    }

    public boolean isSkipStagingRepositoryClose()
    {
        return skipStagingRepositoryClose;
    }

    public void setSkipStagingRepositoryClose( boolean skipStagingRepositoryClose )
    {
        this.skipStagingRepositoryClose = skipStagingRepositoryClose;
    }

    /**
     * Stages an artifact from {@code path} under {@code baseDir} to {@code path} under {@link #getStagingDirectory()}.
     *
     * @param baseDir the base directory to copy {@code path} from
     * @param path the sub path under {@code baseDir} and staging target directory
     * @throws BuildException if an error occurred deploying the artifact
     */
    protected void stageLocally( File baseDir, String path )
        throws BuildException
    {
        try
        {
            final File source = new File( baseDir, path );
            final File target = new File( getStagingDirectory(), path );
            FileUtils.copyFile( source, target );
        }
        catch ( IOException e )
        {
            throw new BuildException( e );
        }
    }

    /**
     * Stages remotely locally staged artifacts.
     *
     * @throws BuildException if an error occurred deploying the artifact
     */
    protected void stageRemotely()
        throws BuildException
    {
        boolean successful = false;
        try
        {
            createTransport();
            final String deployUrl = beforeUpload();
            final ZapperRequest request = new ZapperRequest( getStagingDirectory(), deployUrl );
            if ( getConnectionInfo().getAuthentication() != null )
            {
                request.setRemoteUsername( getConnectionInfo().getAuthentication().getUsername() );
                request.setRemotePassword( getConnectionInfo().getAuthentication().getPassword() );
            }

            if ( getConnectionInfo().getProxy() != null )
            {
                request.setProxyProtocol( getNexusUrl().getProtocol().toString().toLowerCase() );
                request.setProxyHost( getConnectionInfo().getProxy().getHost() );
                request.setProxyPort( getConnectionInfo().getProxy().getPort() );
                if ( getConnectionInfo().getProxy().getAuthentication() != null )
                {
                    request.setProxyUsername( getConnectionInfo().getProxy().getAuthentication().getUsername() );
                    request.setProxyPassword( getConnectionInfo().getProxy().getAuthentication().getPassword() );
                }
            }

            log( " * Uploading locally staged artifacts to: " + deployUrl );
            // Zapper is a bit "chatty", if no Maven debug session is ongoing, then up logback to WARN
            final Zapper zapper = new ZapperImpl();
            zapper.deployDirectory( request );
            log( " * Upload of locally staged artifacts done." );
            successful = true;
        }
        catch ( IOException e )
        {
            throw new BuildException( "Cannot deploy!", e );
        }
        finally
        {
            afterUpload( skipStagingRepositoryClose, successful );
        }
    }

    // ==

    /**
     * This is the profile that was either "auto selected" (matched) or selection by ID happened if user provided
     * {@link #stagingRepositoryId} parameter.
     */
    private Profile stagingProfile;

    /**
     * The repo ID we use (filled in in both cases! see {@link #managedStagingRepositoryId}).
     */
    private String stagingRepositoryId;

    /**
     * This field being non-null means WE manage a staging repository, hence WE must to handle it too (close).
     */
    private String managedStagingRepositoryId;

    protected String beforeUpload()
        throws BuildException
    {
        try
        {
            log( "Performing staging against Nexus on URL " + getNexusUrl() );
            final NexusStatus nexusStatus = getNexusClient().getNexusStatus();
            log( String.format( " * Remote Nexus reported itself as version %s and edition \"%s\"",
                nexusStatus.getVersion(), nexusStatus.getEditionLong() ) );
            final StagingWorkflowV2Service stagingService = getStagingWorkflowService();

            // if profile is not "targeted", perform a match and save the result
            if ( StringUtils.isBlank( getTargetedStagingProfileId() ) )
            {
                final ProfileMatchingParameters params =
                    new ProfileMatchingParameters( getProjectInfo().getGroupId(), getProjectInfo().getArtifactId(),
                        getProjectInfo().getVersion() );
                stagingProfile = stagingService.matchProfile( params );
                log( " * Using staging profile ID \"" + stagingProfile.getId() + "\" (matched by Nexus)." );
            }
            else
            {
                stagingProfile = stagingService.selectProfile( getTargetedStagingProfileId() );
                log( " * Using staging profile ID \"" + stagingProfile.getId() + "\" (configured by user)." );
            }

            if ( StringUtils.isBlank( getTargetedStagingRepositoryId() ) )
            {
                stagingRepositoryId =
                    stagingService.startStaging( stagingProfile, "Started by nexus-staging-ant-tasks", null );
                managedStagingRepositoryId = stagingRepositoryId;
                log( " * Created staging repository with ID \"" + stagingRepositoryId + "\"." );
            }
            else
            {
                stagingRepositoryId = getTargetedStagingRepositoryId();
                managedStagingRepositoryId = null;
                log( " * Using non-managed staging repository with ID \"" + stagingRepositoryId
                    + "\" (we are NOT managing it)." ); // we will not close it! This might be created by some
                                                        // other automated component
            }

            // if this is 2nd or any subsequent staging attempt, the file will be left from previous run
            // but, the file contents will not be valid anyway, as we are doing next attempt. So
            // just handle it gracefully by removing the file and NOT upload it (the one with stale info, as we just
            // created a new repo but props points to old repo).
            final File stagingPropertiesFile = new File( getStagingDirectory(), STAGING_REPOSITORY_PROPERTY_FILE_NAME );
            if ( stagingPropertiesFile.isFile() )
            {
                log( " * Removing previous staging properties file (is this subsequent staging attempt?)." );
                try
                {
                    FileUtils.forceDelete( stagingPropertiesFile );
                }
                catch ( IOException e )
                {
                    throw new BuildException(
                        "Error when deleting staging properties file leftover from previous attempt: "
                            + stagingPropertiesFile.getAbsolutePath(), e );
                }
            }

            return stagingService.startedRepositoryBaseUrl( stagingProfile, stagingRepositoryId );
        }
        catch ( final NexusClientErrorResponseException e )
        {
            ErrorDumper.dumpErrors( this, e );
            // fail the build
            throw new BuildException( "Could not perform action: Nexus ErrorResponse received!", e );
        }
    }

    protected void afterUpload( final boolean skipClose, final boolean successful )
        throws BuildException
    {
        // if successful, write out the context
        if ( successful )
        {
            // this variable will be filled in only if we really staged: is it targeted repo (someone else created or
            // not)
            // does not matter, see managed flag
            // deployUrl perform "plain deploy", hence this will be no written out, it will be written out in any other
            // case
            if ( stagingRepositoryId != null )
            {
                final String stagingRepositoryUrl =
                    concat( getNexusUrl().toString(), "/content/repositories", stagingRepositoryId );

                final Properties stagingProperties = new Properties();
                // the staging repository ID where the staging went
                stagingProperties.put( STAGING_REPOSITORY_ID, stagingRepositoryId );
                // the staging repository's profile ID where the staging went
                stagingProperties.put( STAGING_REPOSITORY_PROFILE_ID, stagingProfile.getId() );
                // the staging repository URL (if closed! see below)
                stagingProperties.put( STAGING_REPOSITORY_URL, stagingRepositoryUrl );
                // targeted repo mode or not (are we closing it or someone else? If false, the URL above might not yet
                // exists if not yet closed....
                stagingProperties.put( STAGING_REPOSITORY_MANAGED, String.valueOf( managedStagingRepositoryId != null ) );

                final File stagingPropertiesFile =
                    new File( getStagingDirectory(), STAGING_REPOSITORY_PROPERTY_FILE_NAME );
                FileOutputStream fout = null;
                try
                {
                    fout = new FileOutputStream( stagingPropertiesFile );
                    stagingProperties.store( fout, "Generated by nexus-staging-ant-tasks" );
                    fout.flush();
                }
                catch ( IOException e )
                {
                    throw new BuildException( "Error saving staging repository properties to file "
                        + stagingPropertiesFile, e );
                }
                finally
                {
                    IOUtil.close( fout );
                }
            }
        }

        // in any other case nothing happens
        // by having stagingRepositoryId string non-empty, it means we created it, hence, we are managing it too
        if ( managedStagingRepositoryId != null )
        {
            final StagingWorkflowV2Service stagingService = getStagingWorkflowService();
            try
            {
                if ( !skipClose )
                {
                    if ( successful )
                    {
                        try
                        {
                            log( " * Closing staging repository with ID \"" + managedStagingRepositoryId + "\"." );
                            stagingService.finishStaging( stagingProfile, managedStagingRepositoryId, getDescription() );
                        }
                        catch ( StagingRuleFailuresException e )
                        {
                            log( "Error while trying to close staging repository with ID \""
                                + managedStagingRepositoryId + "\"." );
                            ErrorDumper.dumpErrors( this, e );
                            // drop the repository (this will break exception chain if there's new failure, like
                            // network)
                            if ( !isKeepStagingRepositoryOnCloseRuleFailure() )
                            {
                                log( "Dropping failed staging repository with ID \"" + managedStagingRepositoryId
                                    + "\"." );
                                stagingService.dropStagingRepositories(
                                    "Staging rules failed on closing staging repository: " + managedStagingRepositoryId,
                                    managedStagingRepositoryId );
                            }
                            else
                            {
                                log( "Not dropping failed staging repository with ID \"" + managedStagingRepositoryId
                                    + "\"." );
                            }
                            // fail the build
                            throw new BuildException( "Could not perform  action against repository \""
                                + managedStagingRepositoryId + "\": there are failing staging rules!", e );
                        }
                    }
                    else
                    {
                        if ( !keepStagingRepositoryOnFailure )
                        {
                            log( "WARN: Dropping failed staging repository with ID \"" + managedStagingRepositoryId
                                + "\" (due to unsuccessful upload)." );
                            stagingService.dropStagingRepositories(
                                "Dropped by nexus-staging-ant-tasks (due to unsuccessful upload).",
                                managedStagingRepositoryId );
                        }
                        else
                        {
                            log( "WARN: Not dropping failed staging repository with ID \"" + managedStagingRepositoryId
                                + "\" (due to unsuccessful upload)." );
                        }
                    }
                }
                else
                {
                    log( " * Not closing staging repository with ID \"" + managedStagingRepositoryId + "\"." );
                }
                log( "Finished staging against Nexus " + ( successful ? "with success." : "with failure." ) );
            }
            catch ( final NexusClientErrorResponseException e )
            {
                log( "Error while trying to close staging repository with ID \"" + managedStagingRepositoryId + "\"." );
                ErrorDumper.dumpErrors( this, e );
                // fail the build
                throw new BuildException( "Could not perform action against repository \"" + managedStagingRepositoryId
                    + "\": Nexus ErrorResponse received!", e );
            }
        }

        if ( !successful )
        {
            log( "Error: Remote staging was unsuccessful!" );
        }
    }

    protected String concat( String... paths )
    {
        StringBuilder result = new StringBuilder();

        for ( String path : paths )
        {
            while ( path.endsWith( "/" ) )
            {
                path = path.substring( 0, path.length() - 1 );
            }
            if ( result.length() > 0 && !path.startsWith( "/" ) )
            {
                result.append( "/" );
            }
            result.append( path );
        }

        return result.toString();
    }
}
