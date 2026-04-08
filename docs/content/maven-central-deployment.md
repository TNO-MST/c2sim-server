# Maven central deployment

The packages are published under the `nl.tno` namespace on the` on [maven central repository](https://central.sonatype.com/). 

## Maven profile

In the parent `pom.xml`, a dedicated `release` profile is defined. This profile prevents accidental publishing to the Maven Central Repository. Once a package is published, it cannot be deleted or modified.

As an additional safeguard, the `-DperformRelease=true` flag must be explicitly set to activate the release process.

!!! tip

    Publishing only occurs during the deploy phase of Maven and when the release profile is explicitly activated.## Signing

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

## Maven namespace nl.tno

The C2SIM packages are published under the `nl.tno` namespace with GPG fingerprint `EAC3E26C38C67B9AE45CE77B1DF104E197BD2BE9`. The corresponding public key can be found [here](https://keyserver.ubuntu.com/pks/lookup?search=TNO%20Mission&fingerprint=on&op=index). The publishing GPG key is owned by TNO.

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
Set the ENV variable `MAVEN_GPG_PASSPHRASE` to the GPG pass phrase
```

## Dry-run publish packages to maven (deploy phase)

With the maven option `-Dmaven.deploy.skip=true` the package will not be published on Maven Central.



```
mvn clean deploy -P release -DperformRelease=true -Dmaven.deploy.skip=true  
```

This tests: 

- build

- packaging

- GPG signing

- plugin wiring

With the maven option  `-X` the logging level can be increased (verbose).

## Dry-run publish packages (install phase)

In the `install` phase of maven the packages are pushed to the local repository `~/.m2/repository/nl/tno/artifact/version/`

```
mvn clean install -P release -DperformRelease=true
```
