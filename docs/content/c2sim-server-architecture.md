# C2SIM Server

## Multi-Module Structure

The project is organized as a Maven multi-module build:


## Build

The project uses [maven](https://maven.apache.org/) build system. Install maven and execute in `.\server`:

```
mvn clean package
```

## Docker

In the folder `server\c2sim-server\docker`:

```
docker compose build
```

## Frameworks

- Javalin

- Logback

## Open API

This project uses spec first approach. The server API interface is generated based on the OpenAPI spec.

## 
