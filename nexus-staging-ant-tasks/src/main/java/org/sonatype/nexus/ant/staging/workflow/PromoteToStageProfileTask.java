/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ant.staging.workflow;

import java.util.Arrays;

import org.apache.tools.ant.BuildException;

import com.sonatype.nexus.staging.client.StagingWorkflowV2Service;

/**
 * Promotes a closed Nexus staging repository into a Nexus Build Promotion Profile.
 * 
 * @author cstamas
 * @since 2.1
 */
public class PromoteToStageProfileTask
    extends AbstractStagingActionTask
{
    /**
     * Specifies the staging build promotion profile ID on remote Nexus where to promotion happens. If not specified,
     * goal will fail.
     */
    private String buildPromotionProfileId;

    public void setBuildPromotionProfileId( String buildPromotionProfileId )
    {
        this.buildPromotionProfileId = buildPromotionProfileId;
    }

    protected String getBuildPromotionProfileId()
        throws BuildException
    {
        if ( buildPromotionProfileId == null )
        {
            throw new BuildException( "The staging staging build promotion profile ID to promote to is not defined!" );
        }

        return buildPromotionProfileId;
    }

    @Override
    public void doExecute( final StagingWorkflowV2Service stagingWorkflow )
        throws BuildException
    {
        log( "Promoting staging repository with ID=" + Arrays.toString( getStagingRepositoryId() )
            + " to build profile ID=" + getBuildPromotionProfileId() );
        stagingWorkflow.promoteStagingRepositories( getDescription(), getBuildPromotionProfileId(),
            getStagingRepositoryId() );
    }
}
