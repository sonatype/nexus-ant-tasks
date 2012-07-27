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
package org.sonatype.nexus.ant.staging.it.simplev2roundtrip;

import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.ant.staging.it.PreparedVerifier;
import org.sonatype.nexus.ant.staging.it.StagingAntPluginITSupport;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.mindexer.client.SearchResponse;
import org.sonatype.sisu.filetasks.FileTaskBuilder;

import com.sonatype.nexus.staging.client.StagingRepository;

/**
 * IT that "implements" the Staging V2 testing guide's "One Shot" scenario followed by the "release" Post Staging Steps
 * section. It also "verifies" that a "matrix" of projects (set up in m2 or m3 way) and maven runtimes (m2 and m3) all
 * work as expected.
 * 
 * @author cstamas
 * @see https://docs.sonatype.com/display/Nexus/Staging+V2+Testing
 */
public class SimpleV2RoundtripIT
    extends StagingAntPluginITSupport
{
    @Inject
    private FileTaskBuilder fileTaskBuilder;

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        // TODO: (cstamas) I promised to Alin to change this "old way of doing things" to use of REST API that would
        // configure Nexus properly once the Security and Staging Management Nexus Client subsystems are done.
        return super.configureNexus( configuration ).addOverlays(
            fileTaskBuilder.copy().directory( file( resolveTestFile( "preset-nexus" ) ) ).to().directory(
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
     * Cleans up Nexus side if needed.
     */
    protected void cleanupNexus( final PreparedVerifier verifier )
    {
        // TODO: see #configureNexus above
        // once "staging management" and "security management" clients are done, we should stop "spoofing" config and
        // do the preparation from here
    }

    /**
     * Nothing to validate before hand.
     */
    protected void preNexusAssertions( final PreparedVerifier verifier )
    {
    }

    /**
     * Validates nexus side of affairs post maven invocations.
     */
    protected void postNexusAssertions( final PreparedVerifier verifier )
    {
        // there are no staging repositories
        final List<StagingRepository> stagingRepositories = getAllStagingRepositories();
        if ( !stagingRepositories.isEmpty() )
        {
            Assert.fail( "Nexus should not have staging repositories, but it has: " + stagingRepositories );
        }

        // stuff we staged are released and found by indexer
        final SearchResponse searchResponse =
            searchThreeTimesForGAV( verifier.getProjectGroupId(), verifier.getProjectArtifactId(),
                verifier.getProjectVersion(), null, null, "releases" );
        if ( searchResponse.getHits().isEmpty() )
        {
            Assert.fail( String.format(
                "Nexus should have staged artifact in releases repository with GAV=%s:%s:%s but those are not found on index!",
                verifier.getProjectGroupId(), verifier.getProjectArtifactId(), verifier.getProjectVersion() ) );
        }
    }

    /**
     * Simulates separate invocation of commands. Deploy then release.
     * 
     * @param verifier
     * @throws VerificationException
     */
    protected void roundtrip( final PreparedVerifier verifier )
    {
        // prepare nexus
        prepareNexus( verifier );
        // check pre-state
        preNexusAssertions( verifier );
        // invoke maven
        verifier.executeTarget( "deploy" );
        verifier.executeTarget( "release" );
        // check post-state
        postNexusAssertions( verifier );
        // cleanup nexus
        cleanupNexus( verifier );
    }

    // ==

    /**
     * Project set up in simple way.
     * 
     * @throws VerificationException
     * @throws IOException
     */
    @Test
    public void roundtripWithSimpleProject()
        throws IOException
    {
        final PreparedVerifier verifier =
            createPreparedVerifier( getClass().getSimpleName(), new File( getBasedir(),
                "target/test-classes/simple-project" ), "sample-dist", null );
        roundtrip( verifier );
    }
}
