<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2007-2015 Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
Nexus Staging Ant Tasks
=======================

Apache Ant tasks that cover Nexus Staging V2 workflow. While these tasks ("staging client") side is open source, you need a Sonatype Nexus Professional 2.1+ on the server side to use these!

Notes:
 * to _build this plugin_ from sources you need access to Sonatype commercial components!
 * to _use this plugin_ in your build, access to Central only is enough!

## Documentation

The Nexus Staging Ant Tasks adds full support for Nexus Staging Suite V2 to be used from Apache Ant.

## Quick tryout

You need to set up and start Nexus Professional 2.1+ instance. Steps to prepare Nexus for this "tryout":

* fire it up (I assume 1st start, with factory default config)
* login as Admininstrator
* create a Staging Profile that accepts "Deploy"
* assign the "Staging Deployer" role belonging to newly created profile to user "deployment"
* make user "deployment" able to perform post-staging steps (release, promote)

Note: this last step is not required, but will prevent last step of the "tryout" Ant build, as
the release will fail, and the build will fail because of that. But the closed staging repository
will be done, and you can Release over UI.

You might need to change few bits in Ant buildfile (if Nexus base URL does not matches in your case).

Check out the sources. There is an Ant [buildfile.xml](https://github.com/sonatype/nexus-ant-tasks/blob/master/nexus-staging-ant-tasks/build.xml) 
file in the module root, that serves demonstration purposes how to use Staging Ant Tasks. Script contains *post build steps* of a theoretical Ant build 
(parts like clean, compile, packaging are *not present*, the purpose of the Maven build you run before Ant build is to "simulate" those parts).

* Checkout the project from https://github.com/sonatype/nexus-ant-tasks
```
git clone git://github.com/sonatype/nexus-ant-tasks.git
```

* dive into nexus-staging-ant-tasks folder
```
cd nexus-ant-tasks/nexus-staging-ant-tasks
```

* build the project using maven (this is needed to have the two JARs appear in the place needed by {{build.xml}}).
```
mvn clean package
```
* execute the Ant build from same spot
```
ant
```

Example (shortened) console transcript:

		[cstamas@marvin sonatype]$ git clone git://github.com/sonatype/nexus-ant-tasks.git
		...
		[cstamas@marvin sonatype]$ cd nexus-ant-tasks/nexus-staging-ant-tasks
		[cstamas@marvin nexus-staging-ant-tasks (master)]$ mvn clean package
		...
		[INFO] Scanning for projects...
		[INFO]                                                                         
		[INFO] ------------------------------------------------------------------------
		[INFO] Building Nexus Ant Tasks : Nexus Staging 2.1-SNAPSHOT
		[INFO] ------------------------------------------------------------------------
		...
		[INFO] Attaching shaded artifact.
		[INFO] ------------------------------------------------------------------------
		[INFO] BUILD SUCCESS
		[INFO] ------------------------------------------------------------------------
		[INFO] Total time: 6.578s
		[INFO] Finished at: Thu Jun 21 17:00:51 CEST 2012
		[INFO] Final Memory: 11M/62M
		[INFO] ------------------------------------------------------------------------
		
		[cstamas@marvin nexus-staging-ant-tasks (master)]$ ant
	
		Buildfile: /Users/cstamas/Worx/sonatype/nexus-ant-tasks/nexus-staging-ant-tasks/build.xml
	
		deploy:
		[staging:stageLocally] Staging locally (stagingDirectory=/Users/cstamas/Worx/sonatype/nexus-ant-tasks/nexus-staging-ant-tasks/target/local-staging)...
		[staging:stageRemotely] Staging remotely (baseUrl=http://localhost:8081/nexus)...
		[staging:stageRemotely] NexusClient created for Nexus instance on URL: http://localhost:8081/nexus/.
		[staging:stageRemotely] Performing staging against Nexus on URL http://localhost:8081/nexus/
		[staging:stageRemotely]  * Remote Nexus reported itself as version 2.1-SNAPSHOT and edition "Professional"
		[staging:stageRemotely]  * Using staging profile ID "12995adeb2046581" (matched by Nexus).
		[staging:stageRemotely]  * Created staging repository with ID "test1-011".
		[staging:stageRemotely]  * Uploading locally staged artifacts to: http://localhost:8081/nexus/service/local/staging/deployByRepositoryId/test1-011
		[staging:stageRemotely] Jun 21, 2012 5:02:03 PM org.sonatype.spice.zapper.internal.transport.AbstractClient upload
		[staging:stageRemotely] INFO: Starting upload transfer ID "5765727c-b7a1-4fb7-9039-aedb06f8bcb7" (using protocol "whole-zfile")
		[staging:stageRemotely] Jun 21, 2012 5:02:03 PM org.sonatype.spice.zapper.internal.transport.AbstractClient upload
		[staging:stageRemotely] INFO: Uploading total of 3889076 bytes (in 20 files) as 20 segments (20 payloads) over 6 tracks.
		[staging:stageRemotely] Jun 21, 2012 5:02:05 PM org.sonatype.spice.zapper.internal.transport.AbstractClient upload
		[staging:stageRemotely] INFO: Upload finished in 1 seconds.
		[staging:stageRemotely]  * Upload of locally staged artifacts done.
		[staging:stageRemotely]  * Closing staging repository with ID "test1-011".
		[staging:stageRemotely] Finished staging against Nexus with success.
		[staging:releaseStagingRepository] NexusClient created for Nexus instance on URL: http://localhost:8081/nexus/.
		[staging:releaseStagingRepository] Releasing staging repository with ID=[test1-011]

		BUILD SUCCESSFUL
		Total time: 5 seconds
		[cstamas@marvin nexus-staging-ant-tasks (master)]$ 

## Quick tryout evaluation

After last step executed, the JAR files you your Maven build produced were actually *staged and released* on your Nexus.
After build, you will not (should not) find any staging repository, as it was created but also released as last step
of Ant build. You can verify the presence of released artifacts in the target release repository that belongs to the
profile you created.

# Configuration

The configuration of the Nexus Staging Ant tasks is done by custom types introduced by this antlib. Here is a full example:

```
	<staging:nexusStagingInfo id="" stagingDirectory="">
		<staging:projectInfo stagingProfileId="" stagingRepositoryId="" groupId="" artifactId="" version="" />
		<staging:connectionInfo baseUrl="">
			<staging:authentication username="" password="" />
			<staging:proxy host="" port="">
				<staging:authentication username="" password="" />
			</staging:proxy>
		</staging:connectionInfo>
	</staging:nexusStagingInfo>
```

You can have multiple of these staging information blocks in your script (with different IDs). 

The meaning of nodes are:

* stagingInfo:id -- mandatory attribute to make it possible later in Ant `build.xml` to reference the needed Staging Info by using `refid`.
* stagingInfo:stagingDirectory -- the local staging directory, a place where local staging will happen. Should be cleaned up with your "clean" tasks (or alike, if any).
* projectInfo -- the project info, see below
* connectionInfo:baseUrl -- valid Nexus base URL.

## Project Info

Similar options exists as for [Nexus Staging Maven Plugin](https://github.com/sonatype/nexus-maven-plugins/tree/master/nexus-staging-maven-plugin).

Basically, you must provide either `stagingProfileId` or the triplet of `groupId`, `artifactId` and `version`.

By providing the GAV triplet only, you will ask Nexus to perform a match and find you a suitable profile to stage to. 
By providing `stagingProfileId`, you enable "Targeted profile" mode (same as with Nexus Staging Maven Plugin).

By providing `stagingRepositoryId` you enable the "Targeted repository" mode, and Nexus Staging Ant tasks 
*will not try to manage the staging repository* (again, same as for Maven plugin).

Order of application is also same: GAV, stagingProfileId, stagingRepositoryId (last wins).

## Available deploy tasks

### `stageLocally`

This task stages locally the deployable file(s). It might be invoked multiple times in same build (nested builds?), and it's 
*main purpose is to "gather" the deployable files that should be handled (uploaded, staged) as logical whole* into one single place. 
Right now, this task does not do much (performs a plain file copy operation), so one might think this task can be circumvented 
by manually assembling the local staging directory (by coding some target using tasks), but 
*this is highly discouraged, as this task might get some extra logic (validation for example) later*.

Attributes:

* none

Input (children elements):

* org.apache.tools.ant.types.FileSet (mandatory) -- the set of files to stage locally.


### `stageRemotely`

This task stages remotely (performs atomic upload) of the locally staged artifacts. Also, it applies the 
Staging V2 workflow: creates a staging repository, uploads to it, closes staging repository. You usually 
invoke this task *once, at the end of the build* (after some tests or conditions are met to stage the binaries).

Attributes:

* "keepStagingRepositoryOnFailure" (optional, default: false) -- to keep the opened staging repository in case of failed atomic upload. By default, task will clean up after itself by dropping the partially uploaded staging repository.
* "skipStagingRepositoryClose"  (optional, default: false) -- to keep the staging repository open, after upload.

Input (children elements):

* none

## Available workflow tasks

All tasks will pick up "context" if no overrides given. See the example build.xml, where as last step, we 
release the staging repository but we do not tell which repository to release, as the staging repository ID is 
sourced from the properties file saved by {{stageRemotely}} task in local staging directory root.

All of these tasks have attribute `stagingRepositoryId`. If this attribute not given, the repository ID will 
be looked up from the properties file. If not found, task will fail the build. Also, the attribute might 
contain comma delimited repository IDs (advanced use, bulk operations).

### closeStagingRepository

To close a staging repository.

Attributes:

* "stagingRepositoryId" (optional) -- the staging repository to act upon

Input (children elements):

* none

### `dropStagingRepository`

To drop a staging repository.

Attributes:

* "stagingRepositoryId" (optional) -- the staging repository to act upon

Input (children elements):

* none

### `releaseStagingRepository`

To release a staging repository.

Attributes:

* "stagingRepositoryId" (optional) -- the staging repository to act upon

Input (children elements):

* none

### `promoteStagingRepository`

To promote a staging repository to a build promotion profile. Needs extra input:

Attributes:

* "stagingRepositoryId" (optional) -- the staging repository to act upon
* "buildPromotionProfileId" (mandatory) -- the profile ID where to promote to

Input (children elements):

* none
