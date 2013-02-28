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

import org.junit.Test;
import org.sonatype.nexus.ant.staging.it.PreparedVerifier;

/**
 * See NXCM-4527, this IT implements it's verification part for Nexus Staging Maven Plugin side when the defaults are
 * overridden. Similar to {@link NXCM4527DropOnCloseRuleFailureIT} IT, but here we assert that staging repository is NOT
 * dropped, it should still exists.
 * 
 * @author cstamas
 */
public class NXCM4527DropOnCloseRuleFailureOverrideIT
    extends NXCM4527DropOnCloseRuleFailureIT
{

    public NXCM4527DropOnCloseRuleFailureOverrideIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    /**
     * Validates nexus side of affairs post maven invocations.
     */
    @Override
    protected void postNexusAssertions( final PreparedVerifier verifier )
    {
        assertOverrides( verifier );
    }

    // ==

    /**
     * Project set up in simple way.
     */
    @Test
    @Override
    public void roundtripWithSimpleProject()
        throws IOException
    {
        final Properties properties = new Properties();
        properties.setProperty( "skipStagingRepositoryClose", "false" );
        properties.setProperty( "keepStagingRepositoryOnCloseRuleFailure", "true" );
        final PreparedVerifier verifier =
            createPreparedVerifier( getClass().getSimpleName(), new File( getBasedir(),
                "target/test-classes/simple-project-noclose" ), "sample-dist-broken", properties );
        roundtrip( verifier );
    }
}