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

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Task;
import org.junit.rules.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.ant.staging.ErrorDumper;
import org.sonatype.nexus.client.core.exception.NexusClientErrorResponseException;

import com.google.common.base.Preconditions;
import com.sonatype.nexus.staging.client.StagingRuleFailuresException;

/**
 * A simple "wrapper" class that carried the {@link Verifier} but also some extra data about the project being built by
 * Verifier that makes easier the post-build assertions.
 * 
 * @author cstamas
 */
public class PreparedVerifier
    extends BuildFileTest
{
    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    private final File baseDir;

    private final String projectGroupId;

    private final String projectArtifactId;

    private final String projectVersion;

    public PreparedVerifier( final File baseDir, final String buildFileName, final String projectGroupId,
                             final String projectArtifactId, final String projectVersion )
    {
        this.baseDir = Preconditions.checkNotNull( baseDir );
        this.projectGroupId = Preconditions.checkNotNull( projectGroupId );
        this.projectArtifactId = Preconditions.checkNotNull( projectArtifactId );
        this.projectVersion = Preconditions.checkNotNull( projectVersion );
        configureProject( new File( baseDir, buildFileName ).getAbsolutePath() );
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public String getProjectGroupId()
    {
        return projectGroupId;
    }

    public String getProjectArtifactId()
    {
        return projectArtifactId;
    }

    public String getProjectVersion()
    {
        return projectVersion;
    }

    @Override
    public void executeTarget( final String target )
    {
        try
        {
            super.executeTarget( target );
        }
        catch ( StagingRuleFailuresException e )
        {
            ErrorDumper.dumpErrors( new Task()
            {
            }, e );
            throw e;
        }
        catch ( NexusClientErrorResponseException e )
        {
            ErrorDumper.dumpErrors( new Task()
            {
            }, e );
            throw e;
        }
        catch ( BuildException e )
        {
            final Throwable c = e.getCause();
            if ( c instanceof StagingRuleFailuresException )
            {
                ErrorDumper.dumpErrors( new Task()
                {
                }, (StagingRuleFailuresException) c );
                throw e;
            }
            else if ( c instanceof NexusClientErrorResponseException )
            {
                ErrorDumper.dumpErrors( new Task()
                {
                }, (NexusClientErrorResponseException) c );
                throw e;
            }
        } 
        finally 
        {
	      logger.info( getFullLog() );
        }
    }
}
