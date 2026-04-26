# Maven central deployment

The packages are published under the `nl.tno` namespace on the on [maven central repository](https://central.sonatype.com/). 

## GPG package signing

All artifacts must be signed before they can be published to the Maven Central Repository. This is handled by the `maven-gpg-plugin`. For this plugin to work, the `gpg` signing tool must be installed (on the local system).

* Windows
  Install [gpg 4 win](https://gpg4win.org) on windows

* Linux
  
  ```
  sudo apt update  
  sudo apt install gnupg 
  ```
  
  Check if `gpg` is installed:
  
  ```
  gpg --version
  ```

!!! note

    The GPG key for the namespace `nl.tno` is managed by TNO. The `nl.tno` private PGP key is needed to sign the package. So this can only be done when you have access to the key.

The C2SIM packages are published under the `nl.tno` namespace with GPG fingerprint `EAC3E26C38C67B9AE45CE77B1DF104E197BD2BE9`. The corresponding public key can be found [here](https://keyserver.ubuntu.com/pks/lookup?search=TNO%20Mission&fingerprint=on&op=index). 

When you have the GPG private key, load this key:

```
gpg --import private.key
```

To show the keys:

```
gpg --list-secret-keys --keyid-format=long 
```

!!!note

    When you don't have the GPG private key, the signing can be skipped with the option `-Dgpg.skip=true`

The GPG private key has an `pass phrase`.

!!! note

```
Set the ENV variable `MAVEN_GPG_PASSPHRASE` to the GPG pass phrase. Otherwise the pass phrase will be asked.
```

## Maven release plugin

Initially the [maven release plugin](https://maven.apache.org/maven-release/maven-release-plugin/) was used. The main reasons why switch to manually publishing:

* Not all modules need to be published to`maven repository`. The can be excluded from maven release with `maven.deploy.skip`, however then the version of the modules are not updated by the maven release plugin then anymore.

* When an error occurs in the `mvn release:prepare`, the revert can be painful because parts are already pushed to GIT.

!!! note

    Maven release plugin is not used anymore!

## Plugin 'central-publishing-maven-plugin'

The maven plugin [central-publishing-maven-plugin](https://central.sonatype.org/publish/publish-portal-maven/) is used to deploy the packages to the maven repository.  The initial idea was to only publish a subsection of the POM with the option  `<maven.deploy.skip>true</maven.deploy.skip>` in `properties` (pom.xml). This caused problems, therefor all packages are published.



To be able to deploy to Maven Central, the `<users>/.m2/settings.xml` the 'central' entry must contain an `sonatype access token`. An access token can be created in the [sonatype website (Maven Central Repo)](https://central.sonatype.com/usertoken). 

```
<settings>
  <servers>
     <server>
        <id>central</id>
        <username>See https://central.sonatype.com/usertoken</username>
        <password>See https://central.sonatype.com/usertoken</password>
     </server>
  </servers>
</settings>
```

## Setting POM module versions

To show the current version used in the POM modules use:

```
mvn help:evaluate -Dexpression=project.version -q -DforceStdout
```

This maven command will automatically updated all POM modules to the supplied version number.

```
mvn versions:set -DnewVersion="<version>" -DprocessAllModules=true -DgenerateBackupPoms=false
```

With `mvn versions:set` the all version related operations can be done, there are some helper methods also possible:

**Strip "-SNAPSHOT"** from version

```
mvn versions:set -DremoveSnapshot=true versions:commit -DprocessAllModules=true -DgenerateBackupPoms=false
```

**Create new SNAPSHOT version**

There is also `maven plugin` to automatically increase the version number. 

```
mvn build-helper:parse-version versions:set -DnewVersion="${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}-SNAPSHOT" -DgenerateBackupPoms=false
```

!!! note

    This project has the approach that all POM modules should use the same version!

## Create  Central Maven Repo package release

This is the same process as the [central-publishing-maven-plugin](https://central.sonatype.org/publish/publish-portal-maven/) does.

**1.) Check out latest version**

**2.) Build project**

```
cd <root>/server
mvn clean verify
```

If the `verify`fails on style errors, some can be fixed automatically with `mvn spotless:apply`.

```
mvn package
git status
```

The project should build, and there should not be any GIT changes.  

All POM modules (for maven packages mandatory) should have in the `target` folder:

* <module name>.jar

* <module name>-sources.jar

* <module name>-javadoc.jar

**3.) Do clean checkout (optional)**

**4.) Remove `-SNAPSHOT` from version in POM modules**

```
mvn versions:set -DremoveSnapshot=true versions:commit -DprocessAllModules=true -DgenerateBackupPoms=false
```

Check with GIT  diff changes if this is the desired version.

**5.) Build the project**

The version change should not have an impact on the code, make sure:

```
mvn clean package
```

**6.) Verify package signing**

All `maven central repo` must be signed, check if the signing process is configured well:

```
mvn verify -P release
```

The PGP pass phrase will be asked during build. 

**7.) Commit the release version to GitHub and create a release tag**

```bash
git commit -am "release <version>"
git tag c2sim-<version>

# push
git push 
git push --tags
```

**8.) Deploy the release build**

This step upload the packages to the `central maven repo`.

```
mvn clean deploy -P release
```

[Confirm the release in Maven Central]([Maven Central: Publishing](https://central.sonatype.com/publishing/deployments))

**9.) Create a new working version (SNAPSHOT)**

```
mvn build-helper:parse-version versions:set -DnewVersion="${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}-SNAPSHOT" -DgenerateBackupPoms=false
```

# 

## Revert release change

```
mvn release:clean
If GIT tag is created:
git tag -d c2sim-<version>
git push origin :refs/tags/c2sim-<version>
```

See also [Publish to maven central repository (sonatype)](https://central.sonatype.org/publish/publish-portal-maven/#wait-for-publishing)
