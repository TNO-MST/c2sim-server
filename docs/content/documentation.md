# Documentation

## MKDOCS Documentation

The documentation website is generated with [mkdocs](https://www.mkdocs.org/) . The table of content is described in `/mkdocs.yml`. 

Build documentation:

* Manual build with `c2sim-server\docs\generate-mkdocs.bat` to view on local machine.

* Automatic build with GitHub action `.github\workflows\deploy.yml` on commit to GitHub.

* Documentation is automatic build  in docker image

View documentation

| Deployment | How to show documentation website                                                                                                          |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| Online     | https://tno-mst.github.io/c2sim-server/                                                                                                    |
| Local      | Run `generate-mkdocs.bat`; builds documentation and view documentation on local webserver on `http://127.0.0.1:1234/TNO-MST/c2sim-server/` |
| Docker     | When the `C2SIM-SERVER` is started in docker container, the documentation is automatically hosted by the C2SIM server.                     |

## Java doc

The project includes javadoc.

```
mvn javadoc:javadoc
```

To generate a `HTML` documentation website for all modules:

```
mvn javadoc:aggregate
```
