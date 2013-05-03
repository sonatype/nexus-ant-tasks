<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2007-2013 Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
# Nexus Staging Ant Tasks IT Suite

ITs for Nexus Staging Ant Tasks.

## Short list of resources (src/test/it-resources)

* preset-nexus - the configuration "spoofed" under Nexus. It contains configuration for staging with two profiles, and security changes to make user `deployment` able to stage to these two profiles and release from them. The profiles have a "usual" set of rules, enforce presence of sources and javadoc jars along the main JAR.
* sample-dist - contains a "correct" sample of dist folder produced by some build. The ITs "simulate" from the point where a staging happens, nothing is compiled, assembled, etc.
* sample-dist-broken - contains a "broken" sample of dist folder produced by some build. It will not pass the rules of the preset profiles, as javadoc is missing.
* simple-project - a simple Ant project that does local then remote staging, and finally releases.
* simple-project-noclose - a simple Ant project that does the "two phase" (if configured) deploy by skipping close step from deploy, uses explicit "close" target that performs the close step and finally releases.