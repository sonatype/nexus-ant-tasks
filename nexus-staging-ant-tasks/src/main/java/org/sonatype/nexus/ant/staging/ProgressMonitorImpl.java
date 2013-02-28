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

package org.sonatype.nexus.ant.staging;

import com.sonatype.nexus.staging.client.StagingWorkflowV1Service.ProgressMonitor;
import org.apache.tools.ant.Project;

import java.io.PrintStream;

/**
 * Default {@link ProgressMonitor} implementation.
 *
 * @since 1.3
 */
public class ProgressMonitorImpl
    implements ProgressMonitor
{
    protected final Project project;

    protected boolean needsNewline;

    public ProgressMonitorImpl(final Project project) {
        this.project = project;
    }

    protected PrintStream getOut() {
        return System.out;
    }

    protected void maybePrintln() {
        if (needsNewline) {
            getOut().println();
            getOut().flush();
            needsNewline = false;
        }
    }

    protected void debug(final String message) {
        project.log(message, Project.MSG_DEBUG);
    }

    protected void warn(final String message) {
        project.log(message, Project.MSG_WARN);
    }

    @Override
    public void start() {
        debug("START");

        getOut().println();
        getOut().print("Waiting for operation to complete...");
        getOut().flush();
    }

    @Override
    public void tick() {
        debug("TICK");

        needsNewline = true;
        getOut().print(".");
        getOut().flush();
    }

    @Override
    public void pause() {
        debug("PAUSE");
    }

    @Override
    public void info(final String message) {
        debug(message);
    }

    @Override
    public void error(final String message) {
        debug(message);
    }

    @Override
    public void stop() {
        debug("STOP");

        maybePrintln();

    }

    @Override
    public void timeout() {
        maybePrintln();

        warn("TIMEOUT");
    }

    @Override
    public void interrupted() {
        maybePrintln();

        warn("INTERRUPTED");
    }
}
