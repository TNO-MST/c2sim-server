# Maven central deployment

The packages are published under the `nl.tno` namespace on the` on [maven central repository](https://central.sonatype.com/). 

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

## Dry-run publish packages to maven (deploy phase)

**1.) Build project**

Verify with a normal build if the project is valid.

```
mvn clean verify
```

If the `verify`fails on style errors, some can be fixed automatically with `mvn spotless:apply`.

Validates:

- build works
- unit tests pass
- javadoc + sources generated
- GPG signing phase is reachable

!!! note

    Each maven package must have <module name>.jar, <module name>-javadoc.jar and <module name>-sources.jar in the `/target`folder of the module.

**2.) Verify PGP signing**

Validate if the .jar can be signed with the PGP key:

```
mvn verify -P release 
```

**3.) Do dry-run**

A dry-run can be done:

```
mvn release:prepare -DdryRun=true
mvn release:perform -DdryRun=true
```

Validates:

- calculates versions
- checks SCM
- simulates commits + tags
- does NOT change anything

## Release maven package

This section explains how to deploy the packages to maven.

!!! warning

    Once published in maven reposiotry the packages are immutable. 

**What does the maven release plugin?**

First phase `mvn release:prepare`

1.) Pre-checks on complete project :

* No uncommitted changes (GIT)

* The POM modules use `-SNAPSHOT` in version

2.) Ask release version, and new SNAPSHOT version

Based on the current version in the POM.xml it will propose a release version, by default this is the current version without the `-SNAPSHOT`. The new SNAPSHOT version is by default the old SNAPSHOT version increased with one. The default values are good.

3.) Update the `POM.XML` in all modules to use the `release version`

4.) Commit the `POM.XML`  changes (release version) from all modules to GitHub (GIT)

5.) Create a `GIT TAG` for the committed release version. 

For example `git tag c2sim-1.0.3`.

6.) Update `POM.XML` in all modules to use new `SNAPSHOT version`

7.) .) Commit the `POM.XML` changes (SNAPSHOT version) from all modules to GitHub (GIT)

This is the text `[release] prepare for next development iteration` in GIT

Second phase `release:perform`

1.) Clean checkout of release version

2.) Then run `mvn clean deploy`

!!! note

    All POM modules share the same version number, indicating that the modules belong to each other.

To deploy the packages to maven:

```
mvn release:prepare
mvn release:perform 
```

[Confirm the release in Maven Central](https://central.sonatype.com/publishing/deployments)



## Revert release change

```
mvn release:clean
If GIT tag is created:
git tag -d c2sim-<version>
git push origin :refs/tags/c2sim-<version>
```

See also [Publish to maven central repository (sonatype)](https://central.sonatype.org/publish/publish-portal-maven/#wait-for-publishing)

## Manual release (without maven release plugin)

What the `maven release plugin` does automatically, can also be done manually (more control):

```
# release version
mvn versions:set -DnewVersion=<version> -DprocessAllModules=true -DgenerateBackupPoms=false

# verify build
mvn clean verify

# commit + tag
git commit -am "release <version>"
git tag c2sim-<version>

# push
git push && git push --tags

# deploy
mvn clean deploy

# next snapshot
mvn build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion}-SNAPSHOT \
  -DgenerateBackupPoms=false

git commit -am "[release] prepare for next development iteration"
git push
```

## Central maven

In the `<users>/.m2/settings.xml` there must be an access token for maven central (sonatype). It must be named `central`.

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
