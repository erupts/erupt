#!/bin/sh

yarn install
yarn upgrade-interactive --latest
rm -rf src/main/resources/static/amis
cp -r node_modules/amis/sdk src/main/resources/static/amis