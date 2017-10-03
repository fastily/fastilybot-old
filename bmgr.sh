#!/bin/bash

#: Runs fastilybot bots and reports
#:
#: PRECONDITIONS: 
#:		1) /home/pi/scripts exists
#:      2) BMgr exists and WGen has already been run once
#: ARGUMENTS:
#: 		$1 - the type of task to run; -r for reports and -b for bots.
#: 		$2 - the task number (as an integer) to execute
#:
#: Tested on Raspbian Jessie
#: 
#: Author: Fastily

BOTHOME="/home/pi/scripts"
LOGDIR="${BOTHOME}/logs"

if [ ! -d "$BOTHOME" ]; then
  printf "ERROR: ${BOTHOME} does not exist, exiting\n"
  exit 1
fi

if [ $# -ne 2 ]; then
	printf "ERROR: Missing one or two arguments, exiting\n"
    exit 1
fi

mkdir -p "$LOGDIR"
java -jar "${BOTHOME}"/BMgr.jar $1 $2 > "${LOGDIR}/log${1}${2}.txt" 2>&1