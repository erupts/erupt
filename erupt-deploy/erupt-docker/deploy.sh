#!/bin/bash
#
# deploy.sh — Build the all-in-one erupt image and push it to Docker Hub.
# Tag = parent erupt version from pom.xml; also pushed as :latest.
# `docker login` is only prompted when no Docker Hub credentials are stored.

set -e

cd "$(dirname "${BASH_SOURCE[0]}")"

DOCKER_TAG=$(sed -n 's|.*<version>\([^<]*\)</version>.*|\1|p' pom.xml | head -n 1)

IMG_NAME="${DOCKER_IMAGE:-erupts/erupt}"

echo "Using Docker tag: $DOCKER_TAG"

echo "Running Maven clean package..."
mvn clean package -DskipTests

echo "Building Docker image..."
docker build -t "$IMG_NAME:$DOCKER_TAG" -t "$IMG_NAME:latest" .

if ! python3 -c 'import json,sys;d=json.load(open(sys.argv[1]));sys.exit(0 if "https://index.docker.io/v1/" in d.get("auths",{}) else 1)' \
        "$HOME/.docker/config.json" 2>/dev/null; then
    docker login
fi

echo "Pushing Docker image to Docker Hub..."
docker push "$IMG_NAME:$DOCKER_TAG"
docker push "$IMG_NAME:latest"

echo "Docker hub push complete: $IMG_NAME:$DOCKER_TAG, $IMG_NAME:latest"
