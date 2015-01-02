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
package org.sonatype.nexus.ant.staging.deploy;

import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Perform local staging.
 *
 * @author cstamas
 */
public class StageLocallyTask
    extends AbstractDeployTask
{
  private FileSet fileSet;

  public FileSet getFileSet() {
    return fileSet;
  }

  public void add(FileSet fileSet) {
    this.fileSet = fileSet;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute()
      throws BuildException
  {
    log("Staging locally (stagingDirectory=" + getStagingDirectory() + ")...");

    Iterator<FileResource> files = getFileSet().iterator();
    while (files.hasNext()) {
      final FileResource file = files.next();
      stageLocally(file.getBaseDir(), file.getName());
    }
  }
}