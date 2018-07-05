#!/bin/bash

#: Build and deploy BMgr to ~/bin
#:
#: Tested on Raspbian Stretch
#: 
#: Author: Fastily

SCPTDIR="${0%/*}"

printf "Deploying BMgr...\n"

## Build dependency jwiki
cd /tmp
git clone --depth 1 'https://github.com/fastily/jwiki.git'
cd jwiki
./gradlew build publishToMavenLocal -x test

cd "$SCPTDIR"

## Build BMgr
printf "Building BMgr\n"
./gradlew clean build fastilybot-enwp:doDist
mv -f ./fastilybot-enwp/build/libs/BMgr.jar ~

## install crontab
crontab rp3crontab.txt

printf "Done!\n"