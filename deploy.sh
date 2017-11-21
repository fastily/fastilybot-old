#!/bin/bash

#: Build and deploy BMgr to ~/bin
#:
#: Tested on Raspbian Stretch
#: 
#: Author: Fastily

BINDIR=~/bin
LOGDIR=~/logs

SCPTDIR="${0%/*}"

printf "This script will deploy BMgr\n"
mkdir -p "$BINDIR" "$LOGDIR"

## Build dependency jwiki
cd /tmp
git clone --depth 1 'https://github.com/fastily/jwiki.git'
cd jwiki
./gradlew build publishToMavenLocal -x test

cd "$SCPTDIR"

## Build BMgr
printf "Building BMgr\n"
./gradlew clean build fastilybot-enwp:doDist
mv ./fastilybot-enwp/build/libs/BMgr.jar "${BINDIR}/"

## Generate run script
printf "Generating run script in ${BINDIR}\n"
cat > "${BINDIR}"/bmgr.sh <<- EOM
	#!/bin/bash
	java -Xmx512M -jar "${BINDIR}/BMgr.jar" "-\${1}" \${2} > "${LOGDIR}/\${1}\${2}.txt" 2>&1
EOM
chmod a+x "${BINDIR}/bmgr.sh"

## Deploy Crontab
printf "Deploying new crontab\n"
crontab rp3crontab.txt

printf "Done!\n"