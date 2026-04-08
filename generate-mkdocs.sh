#!/bin/bash

# Stop on error
set -e

echo "Test mkdocs in docker container"

# Disable warning for version MKDOCS 2.x, MKDOCS 1.x is active version (stick to version 1)
export NO_MKDOCS_2_WARNING=1

IMAGE_NAME="mkdocs-c2sim-server"

echo "Create docker image to build mkdoc documentation website"
docker build -t $IMAGE_NAME -f docs/Dockerfile-generate-mkdocs .

echo "Starting generate mkdocs website in container"

PORT=${PORT:-1234}
echo "Open web browser on http://localhost:${PORT}"
docker run -p ${PORT}:80 "$IMAGE_NAME"