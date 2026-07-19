#!/bin/bash

set -e

# Resolve paths relative to this script (located in sh/) so it can be run from anywhere
ERUPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

#echo "Building frontend..."
#cd "$ERUPT_DIR/erupt-web"
#source build.sh

#echo "Building and pushing Docker image..."
#cd "$ERUPT_DIR/deploy/erupt-docker"
#source deploy.sh

echo "Deploying to Maven Central..."
cd "$ERUPT_DIR"
mvn clean deploy -P release
