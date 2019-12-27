#!/usr/bin/env sh

appRoot="build/distributions"
appName="groovy-pacman"

## build distro
./gradlew clean distTar

## unpack distro
tar xf ${appRoot}/${appName}.tar -C ${appRoot}

## run app
eval ${appRoot}/${appName}/bin/${appName}
