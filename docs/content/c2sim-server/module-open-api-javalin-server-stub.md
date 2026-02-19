# Module open API javalin server stub

The project follows a schema-first (OpenAPI-first) REST API design approach.
This module generates a .jar that is fully compliant with the C2SIM OpenAPI specification.

Based on the C2SIM OpenAPI specification:

```
open-api-spec/c2sim-server-spec.yaml
```

a RESTful [Javalin](https://javalin.io/) 6 server stub is generated automatically.

## Source code Generation

The project uses the[OpenAPI Tools code generator](https://github.com/openapitools/openapi-generator).

The generation process is configured via the `openapi-generator-maven-plugin` in the pom.xml.

Source code generation can be triggered manually with:

```
mvn openapi-generator:generate
```

During a normal Maven build, the generation step is executed automatically if it is bound to the generate-sources phase.

## Server Stub Implementation

The generated server stub contains REST interface definitions only.

The actual implementation of these interfaces is provided in the
[c2sim-server module](module-c2sim-server.md)

## Kotlin

The OpenAPI generator produces Kotlin-based JVM code (compatible with Java).
The generated classes can be used seamlessly within the Java-based server implementation.
