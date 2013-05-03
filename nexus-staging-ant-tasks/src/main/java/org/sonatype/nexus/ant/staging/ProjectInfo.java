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

/**
 * DTO for project info. These values drives the profile to use (if set) or even the repository to use (if set), those
 * are the "targeted" modes of Nexus Staging V2. In general, you don't use those two fields (you might set the profileId
 * though), but you can let Nexus "match" a profile for you, by supplying G:A:V to match against.
 * 
 * @author cstamas
 */
public class ProjectInfo
{
    /**
     * If set, profile will be selected by the ID supplied by user and no "match" will be done.
     */
    private String stagingProfileId;

    /**
     * If set, repository will not be managed, but task will assume it was created by some other entity (targeted
     * repository mode). In this mode, {@link StageRemotelyTask} will NOT close the staging repository (the one creating
     * it should close it).
     */
    private String stagingRepositoryId;

    /**
     * The groupId to perform profile match against. Have to be set if {@link #stagingProfileId} is not set.
     */
    private String groupId;

    /**
     * The artifactId to perform profile match against. Have to be set if {@link #stagingProfileId} is not set.
     */
    private String artifactId;

    /**
     * The version to perform profile match against. Have to be set if {@link #stagingProfileId} is not set.
     */
    private String version;

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getStagingProfileId()
    {
        return stagingProfileId;
    }

    public void setStagingProfileId( String stagingProfileId )
    {
        this.stagingProfileId = stagingProfileId;
    }

    public String getStagingRepositoryId()
    {
        return stagingRepositoryId;
    }

    public void setStagingRepositoryId( String stagingRepositoryId )
    {
        this.stagingRepositoryId = stagingRepositoryId;
    }
}
