# Module open API javalin server stub

The project follows a contract-first approach using OpenAPI. The `C2SIM Open Specificatio` serves as the leading definition of the API. Based on the YAML specification, a **Javalin server stub** is automatically generated.

The generated module exposes a set of interfaces that are implemented in the `C2SIM-server`. In addition, the module contains the data model for the RESTful API. The resulting module is packaged as a `.jar` and remains fully compliant with the `C2SIM OpenAPI specification`.

The RESTful Javalin 6 server stub (also compatible with Javalin 7) is generated automatically from the following specification file:

```
open-api-spec/c2sim-server-spec.yaml
```

a RESTful [Javalin](https://javalin.io/) 6 server stub is generated automatically (also compatible with latest javalin 7 release).

## Source code Generation

The project uses the [OpenAPI Tools code generator](https://github.com/openapitools/openapi-generator).

The generation process is configured via the `openapi-generator-maven-plugin` in the pom.xml.

Source code generation can be triggered manually with:

```
mvn openapi-generator:generate
```

During a normal Maven build, the generation step is executed automatically if it is bound to the `generate-sources` phase.

## Server Stub Implementation

The generated server stub contains REST interface definitions only.

The actual implementation of these interfaces is provided in the [c2sim-server module](module-c2sim-server.md)

## Kotlin

The OpenAPI generator produces Kotlin-based JVM code (compatible with Java).
The generated classes can be used seamlessly within the Java-based server implementation.
