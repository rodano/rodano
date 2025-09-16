#!/usr/bin/env bash

#fail if a variable is referenced before being set or if a command fails
set -u -e

echo "Starting compilation of plugins"

if [ ! -f pom.xml ]; then
	echo "No pom.xml found in the study directory, nothing to compile"
	exit 0
fi

mvn compile -o -DskipTests
if [ $? -ne 0 ]; then
	echo "Compilation of plugins failed, remember that the only available dependencies are the one available in the Rodano project"
	exit 1
fi
echo "Plugins compiled successfully"
