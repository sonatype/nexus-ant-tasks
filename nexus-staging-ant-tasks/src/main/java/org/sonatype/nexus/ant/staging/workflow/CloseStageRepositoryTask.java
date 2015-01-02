/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import com.sonatype.nexus.staging.client.StagingRuleFailuresException;
import com.sonatype.nexus.staging.client.StagingWorkflowV2Service;

import org.sonatype.nexus.ant.staging.ErrorDumper;

import org.apache.tools.ant.BuildException;

/**
 * Closes a Nexus staging repository.
 *
 * @author cstamas
 * @since 2.1
 */
public class CloseStageRepositoryTask
    extends AbstractStagingActionTask
{
  @Override
  public void doExecute(final StagingWorkflowV2Service stagingWorkflow)
      throws BuildException
  {
    final String[] stagingRepositoryIds = getStagingRepositoryId();
    try {
      log("Closing staging repositories with IDs " + Arrays.toString(stagingRepositoryIds) + ".");
      stagingWorkflow.finishStagingRepositories(getDescription(), stagingRepositoryIds);
    }
    catch (StagingRuleFailuresException e) {
      // report staging repository failures
      ErrorDumper.dumpErrors(this, e);
      // drop the repository (this will break exception chain if there's new failure, like network)
      if (!isKeepStagingRepositoryOnCloseRuleFailure()) {
        log("Dropping failed staging repositories " + Arrays.toString(stagingRepositoryIds) + ".");
        stagingWorkflow.dropStagingRepositories("Staging rules failed on closing staging repositories: "
            + Arrays.toString(stagingRepositoryIds), stagingRepositoryIds);
      }
      else {
        log("Not dropping failed staging repositories " + Arrays.toString(stagingRepositoryIds) + ".");
      }
      // fail the build
      throw new BuildException("Could not perform action: there are failing staging rules!", e);
    }
  }
}
