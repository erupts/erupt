#!/bin/bash
#
# deploy.sh — Publish erupt artifacts to Maven Central.
#
# Usage:
#   ./scripts/deploy.sh                      # upload only, publish by hand in the Central Portal
#   AUTO_PUBLISH=1 ./scripts/deploy.sh       # publish automatically once validated (used by release.sh)
#   SKIP_TESTS=1 ./scripts/deploy.sh         # skip the test suite

set -e

# Resolve paths relative to this script (located in scripts/) so it can be run from anywhere
ERUPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

FLAGS=(-P release)
if [ "${AUTO_PUBLISH:-0}" = "1" ]; then
    FLAGS+=(-Dcentral.autoPublish=true -Dcentral.waitUntil=validated)
fi
if [ "${SKIP_TESTS:-0}" = "1" ]; then
    FLAGS+=(-DskipTests)
fi

echo "Deploying to Maven Central... (mvn clean deploy ${FLAGS[*]})"
cd "$ERUPT_DIR"
mvn clean deploy "${FLAGS[@]}"

echo "Deployments: https://central.sonatype.com/publishing/deployments"
