Nexus Staging Ant Tasks
=======================

Example build file:

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:staging="antlib:org.sonatype.nexus.ant.staging" name="buildfile1"
  basedir="${basedir}" default="test">

  <staging:nexusStagingInfo id="test-nexus" stagingDirectory="target/local-staging">
    <staging:projectInfo groupId="G" artifactId="A"
      version="V" />
    <staging:connectionInfo baseUrl="http://nexus.mycorp.com">
      <staging:authentication username="nexususer"
        password="nexussecret" />
      <staging:proxy host="proxy.mycorp.com" port="8080">
        <staging:authentication username="proxyUser"
          password="proxySecret" />
      </staging:proxy>
    </staging:connectionInfo>
  </staging:nexusStagingInfo>

  <target name="test" description="Test">
    <staging:stageLocally>
      <staging:nexusStagingInfo refid="test-nexus"/>
      <fileset dir="target/classes" />
    </staging:stageLocally>
  </target>

</project>
```

Steps to use it in short:
* define the `nexusStagingInfo` node, that you will usually "share" across the build (using `refid`)
* invoke some tasks, all of them need to reference the nexus staging info node.

Available tasks
---------------

* stageLocally -- performs "local staging" of the files. This is basically just a copy that copies given fileSet to a `stagingDirectory` (see NexusStagingInfo). You can invoke the task whenever you want, to "gather" the deployable files into one place (needed for atomic deploy of the stageRemotely goal)
* stageRemotely -- performs "remote staging" (to Nexus Staging V2, complete workflow) of the locally staged files, by atomically uploading the locally staged files and eventually closing staging repository (depends on configuration in very same way as Maven plugin is)
* closeStagingRepository -- closes a staging repository
* dropStagingRepository -- drops a staging repository
* releaseStagingRepository -- releases a staging repository
* promoteStagingRepository -- promotes a staging repository to a build profile