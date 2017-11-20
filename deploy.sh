#!/bin/bash

#: Build and deploy BMgr to ~/bin
#:
#: Tested on Raspbian Jessie
#: 
#: Author: Fastily

BINDIR=~/bin
LOGDIR=~/logs

printf "Deploying BMgr\n"

mkdir -p "$BINDIR" $"LOGDIR"

## Build program
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