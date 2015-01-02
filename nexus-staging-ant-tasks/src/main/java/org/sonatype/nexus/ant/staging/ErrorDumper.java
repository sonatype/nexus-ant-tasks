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
package org.sonatype.nexus.ant.staging;

import com.sonatype.nexus.staging.client.StagingRuleFailures;
import com.sonatype.nexus.staging.client.StagingRuleFailures.RuleFailure;
import com.sonatype.nexus.staging.client.StagingRuleFailuresException;

import org.sonatype.nexus.client.core.exception.NexusClientErrorResponseException;

import org.apache.tools.ant.Task;

public class ErrorDumper
{

  public static void dumpErrors(final Task task, final StagingRuleFailuresException e) {
    task.log("");
    task.log("Nexus Staging Rules Failure Report");
    task.log("==================================");
    task.log("");
    for (StagingRuleFailures failure : e.getFailures()) {
      task.log(String.format("Repository \"%s\" failures", failure.getRepositoryId()));

      for (RuleFailure ruleFailure : failure.getFailures()) {
        task.log(String.format("  Rule \"%s\" failures", ruleFailure.getRuleName()));
        for (String message : ruleFailure.getMessages()) {
          task.log(String.format("    * %s", unfick(message)));
        }
      }
      task.log("");
    }
    task.log("");
  }

  public static void dumpErrors(final Task task, final NexusClientErrorResponseException e) {
    task.log("");
    task.log(String.format("Nexus Error Response: %s - %s", e.getResponseCode(), e.getReasonPhrase()));
    for (NexusClientErrorResponseException.ErrorMessage error : e.errors()) {
      task.log(String.format("  %s - %s", unfick(error.getId()), unfick(error.getMessage())));
    }
    task.log("");
  }

  // ==

  protected static String unfick(final String str) {
    if (str != null) {
      return str.replace("&quot;", "").replace("&lt;b&gt;", "").replace("&lt;/b&gt;", "");
    }
    return str;
  }
}
