package org.sonatype.nexus.ant;

import java.io.File;

import org.apache.tools.ant.BuildFileTest;

public class NexusStagingAntTasksTest
    extends BuildFileTest
{
    public void setUp()
    {
        configureProject( "target/test-classes/buildfile1.xml" );
    }

    public void testSimple()
    {
        executeTarget( "test" );
        assertTrue( "Message was not logged but should.", getLog().contains( "local-staging" ) );
        assertTrue( "Local staging directory should exists.", new File( "target/local-staging" ).isDirectory() );
    }
}
