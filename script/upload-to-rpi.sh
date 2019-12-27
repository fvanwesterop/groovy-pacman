#!/usr/bin/env sh

appRoot="build/distributions"
appName="groovy-pacman"

remoteUser="pi"
remoteHost="raspberrypi.wlan.local"
remoteDeployDir="Projects"

echo "uploading to'${remoteHost}'..."
ssh ${remoteUser}@${remoteHost} "rm -rf ${remoteDeployDir}/${appName}" && \
scp ${appRoot}/${appName}.tar ${remoteUser}@${remoteHost}:${remoteDeployDir}/

echo "unpacking distribution..."
ssh ${remoteUser}@${remoteHost} "tar xf ${remoteDeployDir}/${appName}.tar -C ${remoteDeployDir}" && \
ssh ${remoteUser}@${remoteHost} "rm -f ${remoteDeployDir}/${appName}.tar" && \
echo "all done."

