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
package org.sonatype.nexus.ant.staging.it;

import static org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy.Strategy.EACH_TEST;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.goodies.common.Varargs.$;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.mindexer.client.MavenIndexer;
import org.sonatype.nexus.mindexer.client.SearchResponse;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import org.sonatype.sisu.goodies.common.Time;

import com.google.common.base.Throwables;
import com.sonatype.nexus.staging.client.Profile;
import com.sonatype.nexus.staging.client.StagingRepository;
import com.sonatype.nexus.staging.client.StagingWorkflowV2Service;
import com.sonatype.nexus.testsuite.support.NexusProConfigurator;

/**
 * A base class that adds support for Ant task tests against real nexus instance.
 * 
 * @author cstamas
 */
@NexusStartAndStopStrategy( EACH_TEST )
public abstract class StagingAntPluginITSupport
    extends NexusRunningParametrizedITSupport
{

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        return firstAvailableTestParameters(
            systemTestParameters(),
            testParameters(
                $( "com.sonatype.nexus:nexus-professional:zip:bundle" )
            )
        ).load();
    }

    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    @Rule
    public final Timeout defaultTimeout = new Timeout( Time.minutes( 5 ).toMillisI() );

    private NexusClient nexusDeploymentClient;

    public StagingAntPluginITSupport( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        return new NexusProConfigurator( this ).configure( configuration );
    }

    @Before
    public void createClient()
    {
        logger.info( "Creating NexusClient..." );
        nexusDeploymentClient = createNexusClient( nexus(), "deployment", "deployment123" );
    }

    public MavenIndexer getMavenIndexer()
    {
        return nexusDeploymentClient.getSubsystem( MavenIndexer.class );
    }

    public StagingWorkflowV2Service getStagingWorkflowV2Service()
    {
        return nexusDeploymentClient.getSubsystem( StagingWorkflowV2Service.class );
    }

    public PreparedVerifier createPreparedVerifier( final String testId, final File baseDir, final String dist,
                                                    final Properties properties )
        throws IOException
    {
        final String projectGroupId;
        final String projectArtifactId;
        final String projectVersion;
        // filter the POM if needed
        final File rawPom = new File( baseDir, "raw-build.xml" );
        if ( rawPom.isFile() )
        {
            // we use "sample" dist here, so what we deploy is static
            projectGroupId = "org.apache.ant"; // getClass().getPackage().getName();
            projectArtifactId = "ant"; // baseDir.getName();
            projectVersion = "1.8.4";// "1.0";
            final Properties context = properties != null ? properties : new Properties();
            context.setProperty( "nexus.port", String.valueOf( nexus().getPort() ) );
            context.setProperty( "basedir", baseDir.getAbsolutePath() );
            context.setProperty( "build-basedir", baseDir.getAbsolutePath() );
            context.setProperty( "dist", dist );
            context.setProperty( "testId", testId );
            context.setProperty( "itproject.groupId", projectGroupId );
            context.setProperty( "itproject.artifactId", projectArtifactId );
            context.setProperty( "itproject.version", projectVersion );
            filterBuildXmlsIfNeeded( baseDir, context );
        }
        else
        {
            throw new IOException( "no raw-build.xml found!" );
        }

        return new PreparedVerifier( baseDir, "build.xml", projectGroupId, projectArtifactId, projectVersion );
    }

    /**
     * Recurses the baseDir searching for POMs and filters them.
     * 
     * @param baseDir
     * @param properties
     * @throws IOException
     */
    protected void filterBuildXmlsIfNeeded( final File baseDir, final Properties properties )
        throws IOException
    {
        final File pom = new File( baseDir, "build.xml" );
        final File rawPom = new File( baseDir, "raw-build.xml" );
        if ( rawPom.isFile() )
        {
            tasks().copy().file( file( rawPom ) ).filterUsing( properties ).to().file( file( pom ) ).run();
        }
        else if ( !pom.isFile() )
        {
            // error
            throw new IOException( "No raw-build.xml nor proper build.xml found!" );
        }

        final File[] fileList = baseDir.listFiles();
        if ( fileList != null )
        {
            for ( File file : fileList )
            {
                // recurse only non src and target folders (sanity check)
                if ( file.isDirectory() && !"src".equals( file.getName() ) && !"target".equals( file.getName() ) )
                {
                    filterBuildXmlsIfNeeded( file, properties );
                }
            }
        }
    }

    // ==

    /**
     * Returns the list of all staging repositories - whether open or closed - found in all profiles (all staging
     * repositories present instance-wide).
     * 
     * @return
     */
    protected List<StagingRepository> getAllStagingRepositories()
    {
        final ArrayList<StagingRepository> result = new ArrayList<StagingRepository>();
        final StagingWorkflowV2Service stagingWorkflow = getStagingWorkflowV2Service();
        final List<Profile> profiles = stagingWorkflow.listProfiles();
        for ( Profile profile : profiles )
        {
            final List<StagingRepository> stagingRepositories =
                stagingWorkflow.listStagingRepositories( profile.getId() );
            result.addAll( stagingRepositories );
        }
        return result;
    }

    /**
     * Returns the list of all staging repositories - whether open or closed - found in passed in profile.
     * 
     * @return
     */
    protected List<StagingRepository> getProfileStagingRepositories( final Profile profile )
    {
        List<StagingRepository> stagingRepositories =
            getStagingWorkflowV2Service().listStagingRepositories( profile.getId() );
        return stagingRepositories;
    }

    /**
     * Performs a "cautious" search for GAV that is somewhat "shielded" against Nexus Indexer asynchronicity. It will
     * repeat the search 3 times, with 1000 milliseconds pause. The reason to do this, to be "almost sure" it is or it
     * is not found, as Maven Indexer performs commits every second (hence, search might catch the pre-commit state),
     * but also the execution path as for example a deploy "arrives" to index is itself async too
     * (AsynchronousEventInspector). Hence, this method in short does a GAV search, but is "shielded" with some retries
     * and sleeps to make sure that result is correct. For input parameters see
     * {@link MavenIndexer#searchByGAV(String, String, String, String, String, String)} method.
     * 
     * @return
     */
    protected SearchResponse searchThreeTimesForGAV( final String groupId, final String artifactId,
                                                     final String version, final String classifier, final String type,
                                                     final String repositoryId )
    {
        SearchResponse response = null;
        for ( int i = 0; i < 3; i++ )
        {
            response = getMavenIndexer().searchByGAV( groupId, artifactId, version, classifier, type, repositoryId );
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
                Throwables.propagate( e );
            }
        }
        return response;
    }
}
