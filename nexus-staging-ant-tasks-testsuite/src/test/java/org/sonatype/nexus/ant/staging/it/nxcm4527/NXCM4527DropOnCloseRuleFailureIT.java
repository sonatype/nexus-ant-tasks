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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.tools.ant.BuildException;
import org.junit.Test;
import org.sonatype.nexus.ant.staging.it.PreparedVerifier;

import com.sonatype.nexus.staging.client.StagingRuleFailuresException;

/**
 * See NXCM-4527, this IT implements it's verification part for Nexus Staging Maven Plugin side. Here, we build and
 * deploy/stage a "malformed" project (will lack the javadoc JAR, achieved by passing in "-Dmaven.javadoc.skip=true"
 * during deploy). The project uses default settings for nexus-staging-maven-plugin, so such malformed staged project
 * should have the staging repository dropped upon rule failure. Hence, {@link #postNexusAssertions(PreparedVerifier)}
 * contains checks that there are no staging repositories but also that the artifact built is not released either.
 * 
 * @author cstamas
 */
public class NXCM4527DropOnCloseRuleFailureIT
    extends NXCM4527Support
{

    public NXCM4527DropOnCloseRuleFailureIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    /**
     * Validates nexus side of affairs post maven invocations.
     */
    @Override
    protected void postNexusAssertions( final PreparedVerifier verifier )
    {
        assertDefaults( verifier );
    }

    @Override
    protected void invokeAnt( final PreparedVerifier verifier )
    {
        try
        {
            verifier.executeTarget( "deploy" );
            // if no exception, fail the test
            Assert.fail( "We should end up with failed remote staging!" );
        }
        catch ( BuildException e )
        {
            // good
            Assert.assertTrue( e.getCause() instanceof StagingRuleFailuresException );
        }
    }

    // ==

    /**
     * Project set up in simple way.
     */
    @Test
    public void roundtripWithSimpleProject()
        throws IOException
    {
        final Properties properties = new Properties();
        properties.setProperty( "skipStagingRepositoryClose", "false" );
        properties.setProperty( "keepStagingRepositoryOnCloseRuleFailure", "false" );
        final PreparedVerifier verifier =
            createPreparedVerifier( getClass().getSimpleName(), new File( getBasedir(),
                "target/test-classes/simple-project-noclose" ), "sample-dist-broken", properties );
        roundtrip( verifier );
    }
}