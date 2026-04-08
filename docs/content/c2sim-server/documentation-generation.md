# Documentation

## MKDOCS Documentation

The documentation website is generated with [mkdocs](https://www.mkdocs.org/) . The table of content (TOC) is described in `<root>/mkdocs.yml`.  

Build documentation:

* Manual build with `<root>\generate-mkdocs.sh` to view on local machine. Used for validating the `mkdocs` build process.

* Automatic build with GitHub action `.github\workflows\deploy_mkdocs.yml`. This automatically runs when documentation source folder is updated on GitHub (commit). And the GitHub static website `[C2SIM Server](https://tno-mst.github.io/c2sim-server/)`is also automatically updated.

* When the `c2sim-server` docker images is build, the documentation is automatically added to the C2SIM server.

View documentation

| Deployment | How to show documentation website                                                                                                         |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| Online     | https://tno-mst.github.io/c2sim-server/                                                                                                   |
| Local      | Run `<root>\generate-mkdocs.sh`; builds documentation and starts local webserver on port 1234. Open a browser  `http://127.0.0.1:1234/`   |
| Docker     | When the `C2SIM-SERVER` is started in docker container, the documentation is automatically hosted by the C2SIM server (endpoint `/docs`). |

!!! note

    This project uses MkDocs 1.x the official and stable version. During the mkdocs build a warning is shown to switch to `MkDocs 2.x`. There is no official stable release yet for version 2.x.

## Java doc

Each module has `javadoc` enabled. Javadoc is a tool that generates documentation from special comments in Java source code.

During the standard Maven build process, Javadoc is generated automatically.

```
mvn clean verify
```

| Type                                  | Location                                              |
| ------------------------------------- | ----------------------------------------------------- |
| Per-module HTML Javadoc               | `<module>/target/site/apidocs`                        |
| Per-module Javadoc JAR                | `<module>/target/<module-name>-<version>-javadoc.jar` |
| Aggregated HTML Javadoc (all modules) | `<root>\target\reports\apidocs`                       |

### Manual Build Commands

The `javadoc` generation in automatically triggered in the maven build process, but can also be forced with:

```
mvn javadoc:javadoc # per module (in module folder)
mvn javadoc:aggregate # generate one javadoc for all modules (root folder)
```
