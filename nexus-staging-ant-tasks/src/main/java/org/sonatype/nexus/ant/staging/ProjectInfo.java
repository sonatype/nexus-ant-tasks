package org.sonatype.nexus.ant.staging;

public class ProjectInfo
{
    private String stagingProfileId;

    private String stagingRepositoryId;
    
    private String groupId;

    private String artifactId;

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
