#!/bin/bash

set -e

# upload docker hub
source erupt-cloud-server-docker/deploy.sh

# deploy to maven center repo
cd ../
mvn deploy -Dmaven.test.skip=true

echo "deploy scripts executed successfully!"