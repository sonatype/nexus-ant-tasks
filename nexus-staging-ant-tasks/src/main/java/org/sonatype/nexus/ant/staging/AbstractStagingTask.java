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

package org.sonatype.nexus.ant.staging;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.sonatype.nexus.staging.client.StagingWorkflowV2Service;
import com.sonatype.nexus.staging.client.StagingWorkflowV3Service;
import com.sonatype.nexus.staging.client.rest.JerseyStagingWorkflowV2SubsystemFactory;
import com.sonatype.nexus.staging.client.rest.JerseyStagingWorkflowV3SubsystemFactory;

import org.sonatype.nexus.ant.staging.deploy.StageRemotelyTask;
import org.sonatype.nexus.ant.staging.workflow.CloseStageRepositoryTask;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.rest.BaseUrl;
import org.sonatype.nexus.client.rest.Protocol;
import org.sonatype.nexus.client.rest.ProxyInfo;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClientFactory;

import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public abstract class AbstractStagingTask
    extends Task
{
  /**
   * The connection and staging details for Nexus.
   */
  private NexusStagingInfo nexusStagingInfo;

  /**
   * The repository "description" to pass to Nexus when repository staging workflow step is made. If none passed a
   * server side defaults are applied.
   */
  private String actionMessage = "Performed by nexus-staging-ant-task";

  /**
   * Controls whether the staging repository is kept or not (it will be dropped) in case of staging rule failure when
   * "close" action is performed against it. This is applied in both cases, {@link StageRemotelyTask} and
   * {@link CloseStageRepositoryTask} invocations.
   */
  private boolean keepStagingRepositoryOnCloseRuleFailure = false;

  private int stagingProgressTimeoutMinutes = 5;

  private int stagingProgressPauseDurationSeconds = 3;

  // attributes

  public String getActionMessage() {
    return actionMessage;
  }

  public void setActionMessage(String actionMessage) {
    this.actionMessage = actionMessage;
  }

  public boolean isKeepStagingRepositoryOnCloseRuleFailure() {
    return keepStagingRepositoryOnCloseRuleFailure;
  }

  public void setKeepStagingRepositoryOnCloseRuleFailure(boolean keepStagingRepositoryOnCloseRuleFailure) {
    this.keepStagingRepositoryOnCloseRuleFailure = keepStagingRepositoryOnCloseRuleFailure;
  }

  public int getStagingProgressTimeoutMinutes() {
    return stagingProgressTimeoutMinutes;
  }

  public void setStagingProgressTimeoutMinutes(final int stagingProgressTimeoutMinutes) {
    this.stagingProgressTimeoutMinutes = stagingProgressTimeoutMinutes;
  }

  public int getStagingProgressPauseDurationSeconds() {
    return stagingProgressPauseDurationSeconds;
  }

  public void setStagingProgressPauseDurationSeconds(final int stagingProgressPauseDurationSeconds) {
    this.stagingProgressPauseDurationSeconds = stagingProgressPauseDurationSeconds;
  }

  // children/refs

  public NexusStagingInfo getNexusStagingInfo()
      throws BuildException
  {
    if (nexusStagingInfo.getRefid() != null) {
      final NexusStagingInfo referencedStagingInfo =
          (NexusStagingInfo) getProject().getReference(nexusStagingInfo.getRefid());
      if (referencedStagingInfo == null) {
        throw new BuildException("Unable to locate NexusStagingInfo reference: \""
            + nexusStagingInfo.getRefid() + "\"");
      }
      this.nexusStagingInfo = referencedStagingInfo;
    }
    return nexusStagingInfo;
  }

  public void add(NexusStagingInfo info) {
    this.nexusStagingInfo = info;
  }

  // other

  protected ConnectionInfo getConnectionInfo() {
    return getNexusStagingInfo().getConnectionInfo();
  }

  protected File getStagingDirectory() {
    return getNexusStagingInfo().getStagingDirectory();
  }

  protected ProjectInfo getProjectInfo() {
    return getNexusStagingInfo().getProjectInfo();
  }

  protected String getTargetedStagingProfileId() {
    return getProjectInfo().getStagingProfileId();
  }

  protected String getTargetedStagingRepositoryId() {
    return getProjectInfo().getStagingRepositoryId();
  }

  // ==

  /**
   * Initialized stuff needed for transport.
   */
  protected void createTransport()
      throws BuildException
  {
    if (getConnectionInfo() == null) {
      throw new BuildException(
          "The Nexus connection against which transport should be established is not defined!");
    }

    createNexusClient();
  }

  /**
   * The NexusClient instance.
   */
  private NexusClient nexusClient;

  /**
   * Creates an instance of Nexus Client using generally set server and proxy.
   */
  private void createNexusClient()
      throws BuildException
  {
    try {
      final ConnectionInfo connection = getConnectionInfo();
      final BaseUrl baseUrl = BaseUrl.baseUrlFrom(connection.getBaseUrl());
      final UsernamePasswordAuthenticationInfo authenticationInfo;
      final Map<Protocol, ProxyInfo> proxyInfos = new HashMap<Protocol, ProxyInfo>(1);

      if (connection.getAuthentication() != null) {
        authenticationInfo =
            new UsernamePasswordAuthenticationInfo(connection.getAuthentication().getUsername(),
                connection.getAuthentication().getPassword());
      }
      else {
        authenticationInfo = null;
      }

      if (connection.getProxy() != null) {
        final UsernamePasswordAuthenticationInfo proxyAuthentication;
        if (connection.getProxy().getAuthentication() != null) {
          proxyAuthentication =
              new UsernamePasswordAuthenticationInfo(
                  connection.getProxy().getAuthentication().getUsername(),
                  connection.getProxy().getAuthentication().getPassword());
        }
        else {
          proxyAuthentication = null;
        }
        final ProxyInfo zProxy =
            new ProxyInfo(baseUrl.getProtocol(), connection.getProxy().getHost(),
                connection.getProxy().getPort(), proxyAuthentication);
        proxyInfos.put(zProxy.getProxyProtocol(), zProxy);
      }

      final org.sonatype.nexus.client.rest.ConnectionInfo connectionInfo =
          new org.sonatype.nexus.client.rest.ConnectionInfo(baseUrl, authenticationInfo, proxyInfos);

      this.nexusClient = new JerseyNexusClientFactory(
          // support v2 and v3
          new JerseyStagingWorkflowV2SubsystemFactory(),
          new JerseyStagingWorkflowV3SubsystemFactory()
      )
          .createFor(connectionInfo);

      log("NexusClient created for Nexus instance on URL: " + baseUrl.toString());
    }
    catch (MalformedURLException e) {
      throw new BuildException("Malformed Nexus base URL!", e);
    }
    catch (UniformInterfaceException e) {
      throw new BuildException("Nexus base URL does not point to a valid Nexus location: " + e.getMessage(), e);
    }
    catch (Exception e) {
      throw new BuildException("Nexus connection problem: " + e.getMessage(), e);
    }
  }

  protected NexusClient getNexusClient() {
    return nexusClient;
  }

  protected BaseUrl getNexusUrl() {
    return getNexusClient().getConnectionInfo().getBaseUrl();
  }

  private StagingWorkflowV2Service workflowService;

  public StagingWorkflowV2Service getStagingWorkflowService()
      throws BuildException
  {
    NexusClient nexusClient = getNexusClient();

    if (workflowService == null) {
      // First try v3
      try {
        StagingWorkflowV3Service service = nexusClient.getSubsystem(StagingWorkflowV3Service.class);
        log("Using staging v3 service", Project.MSG_VERBOSE);

        // Install progress monitor
        service.setProgressMonitor(new ProgressMonitorImpl(getProject()));
        service.setProgressTimeoutMinutes(getStagingProgressTimeoutMinutes());
        service.setProgressPauseDurationSeconds(getStagingProgressPauseDurationSeconds());

        workflowService = service;
      }
      catch (Exception e) {
        log("Unable to resolve staging v3 service; falling back to v2: " + e, e, Project.MSG_VERBOSE);
      }

      if (workflowService == null) {
        // fallback to v2 if v3 not available
        try {
          workflowService = nexusClient.getSubsystem(StagingWorkflowV2Service.class);
          log("Using staging v2 service", Project.MSG_VERBOSE);
        }
        catch (IllegalArgumentException e) {
          throw new BuildException(
              String.format("Nexus instance at base URL %s does not support staging v2; reported status: %s, reason:%s",
                  nexusClient.getConnectionInfo().getBaseUrl(),
                  nexusClient.getNexusStatus(),
                  e.getMessage()),
              e);
        }
      }
    }

    return workflowService;
  }
}
