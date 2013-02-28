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
package org.sonatype.nexus.ant.staging.it.nxcm4527;

import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

import java.util.List;

import javax.inject.Inject;

import junit.framework.Assert;

import org.sonatype.nexus.ant.staging.it.PreparedVerifier;
import org.sonatype.nexus.ant.staging.it.StagingAntPluginITSupport;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.mindexer.client.SearchResponse;
import org.sonatype.sisu.filetasks.FileTaskBuilder;

import com.sonatype.nexus.staging.client.StagingRepository;
import com.sonatype.nexus.staging.client.StagingWorkflowV2Service;

/**
 * Parent for NXCM-4527 ITs as they share a lot of common things.
 * 
 * @author cstamas
 */
public abstract class NXCM4527Support
    extends StagingAntPluginITSupport
{
    @Inject
    private FileTaskBuilder fileTaskBuilder;

    public NXCM4527Support( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        // TODO: (cstamas) I promised to Alin to change this "old way of doing things" to use of REST API that would
        // configure Nexus properly once the Security and Staging Management Nexus Client subsystems are done.
        return super.configureNexus( configuration ).addOverlays(
            fileTaskBuilder.copy().directory( file( testData().resolveFile( "preset-nexus" ) ) ).to().directory(
                path( "sonatype-work/nexus/conf" ) ) );
    }

    /**
     * Configures Nexus side if needed.
     */
    protected void prepareNexus( final PreparedVerifier verifier )
    {
        // nexus lives
        // create profile
        // adapt permissions
        // TODO: see #configureNexus above
        // once "staging management" and "security management" clients are done, we should stop "spoofing" config and
        // do the preparation from here
    }

    /**
     * Drop left-behind staging repositories to interfere with subsequent assertions (we share nexus instance, it is
     * "rebooted" per class).
     */
    protected void cleanupNexus( final PreparedVerifier verifier )
    {
        final StagingWorkflowV2Service stagingWorkflow = getStagingWorkflowV2Service();
        List<StagingRepository> stagingRepositories = getAllStagingRepositories();
        for ( StagingRepository stagingRepository : stagingRepositories )
        {
            stagingWorkflow.dropStagingRepositories( "cleanupNexus()", stagingRepository.getId() );
        }
    }

    /**
     * no pre-invocation assertions
     */
    protected void preNexusAssertions( final PreparedVerifier verifier )
    {
    }

    /**
     * Validates nexus side of affairs post maven invocations.
     */
    protected abstract void postNexusAssertions( final PreparedVerifier verifier );

    /**
     * Validates the defaults: staging repository created during staging is dropped and it's contents is not released.
     */
    protected void assertDefaults( final PreparedVerifier verifier )
    {
        // there are no staging repositories as we dropped them (on rule failure)
        final List<StagingRepository> stagingRepositories = getAllStagingRepositories();
        if ( !stagingRepositories.isEmpty() )
        {
            Assert.fail( "Nexus should not have staging repositories, but it has: " + stagingRepositories );
        }

        // stuff we staged should not be released and not found by indexer
        final SearchResponse searchResponse =
            searchThreeTimesForGAV( verifier.getProjectGroupId(), verifier.getProjectArtifactId(),
                verifier.getProjectVersion(), null, null, "releases" );
        if ( !searchResponse.getHits().isEmpty() )
        {
            Assert.fail( String.format(
                "Nexus should NOT have staged artifact in releases repository with GAV=%s:%s:%s but those are not found on index!",
                verifier.getProjectGroupId(), verifier.getProjectArtifactId(), verifier.getProjectVersion() ) );
        }
    }

    /**
     * Validates the overridden defaults: staging repository is left dangling as open and it's contents is not released.
     */
    protected void assertOverrides( final PreparedVerifier verifier )
    {
        // there are staging repositories as we did not drop them (on rule failure), we override defaults
        final List<StagingRepository> stagingRepositories = getAllStagingRepositories();
        if ( stagingRepositories.isEmpty() )
        {
            Assert.fail( "Nexus should have staging repositories, but it has none!" );
        }
        Assert.assertEquals( "Nexus should have 1 staging repository, the one of the current build", 1,
            stagingRepositories.size() );
        Assert.assertEquals( "Staging repository should be left open!", StagingRepository.State.OPEN,
            stagingRepositories.get( 0 ).getState() );

        // stuff we staged should not be released and not found by indexer
        final SearchResponse searchResponse =
            searchThreeTimesForGAV( verifier.getProjectGroupId(), verifier.getProjectArtifactId(),
                verifier.getProjectVersion(), null, null, "releases" );
        if ( !searchResponse.getHits().isEmpty() )
        {
            Assert.fail( String.format(
                "Nexus should NOT have staged artifact in releases repository with GAV=%s:%s:%s but those are not found on index!",
                verifier.getProjectGroupId(), verifier.getProjectArtifactId(), verifier.getProjectVersion() ) );
        }
    }

    /**
     * Simulates separate invocation of commands. Deploy then release.
     */
    protected void roundtrip( final PreparedVerifier verifier )
    {
        // prepare nexus
        prepareNexus( verifier );
        // check pre-state
        preNexusAssertions( verifier );
        // invoke ant
        invokeAnt( verifier );
        // check post-state
        postNexusAssertions( verifier );
        // cleanup nexus
        cleanupNexus( verifier );
    }

    /**
     * Perform Ant invocation(s).
     */
    protected abstract void invokeAnt( final PreparedVerifier verifier );
}