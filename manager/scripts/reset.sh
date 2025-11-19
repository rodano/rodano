#!/bin/bash

#retrieve parameters
REFERENCE=$1
DEMO_DATA=$2
DEMO_USERS=$3
DEMO_USERS_PASSWORD=$4

#check parameters
if [[ -z $REFERENCE ]]
then
	echo "Usage: reference, [demo_data], [demo_users], [demo_users_password]"
	exit 1
fi

#fail if a variable is referenced before being set or if a command fails
set -u -e -x

#retrieve environment variables
DATABASE_HOST=${DATABASE_HOST:-"localhost"}
DATABASE_PORT=${DATABASE_PORT:-3306}
DATABASE_USER=${DATABASE_USER:-"root"}
DATABASE_PASSWORD=${DATABASE_PASSWORD:-"root"}
DATABASE_NAME=${DATABASE_NAME:-"rodano"}

mariadb_connection_arguments=("-h$DATABASE_HOST" "-u$DATABASE_USER" "-p$DATABASE_PASSWORD")

echo "Deleting current database..."
mariadb_command_arguments=("${mariadb_connection_arguments[@]}")
mariadb_command_arguments+=("-e" "DROP DATABASE IF EXISTS $DATABASE_NAME")
mariadb "${mariadb_command_arguments[@]}"

echo "Creating new database..."
mariadb_command_arguments=("${mariadb_connection_arguments[@]}")
mariadb_command_arguments+=("-e" "CREATE DATABASE $DATABASE_NAME")
mariadb "${mariadb_command_arguments[@]}"

echo "Executing initialization script..."
#build JVM parameters
jvm_arguments=("-Drodano.config=/app/study/config.json" "-Drodano.path.data=/tmp" "-Drodano.database.name=$DATABASE_NAME")

#add demo users if specified
if [[ $DEMO_USERS == "yes" ]]
then
	jvm_arguments+=("-Drodano.init.with-users=true")
	#add demo users custom password if specified
	if [[ -n $DEMO_USERS_PASSWORD ]]
	then
		jvm_arguments+=("-Drodano.init.users-password=$DEMO_USERS_PASSWORD")
	fi
fi
#add demo data if specified
if [[ $DEMO_DATA == "yes" ]]
then
	jvm_arguments+=("-Drodano.init.with-data=true")
fi

#build maven parameters
#mvn_arguments=("-f" "/app/rodano.jar")
#mvn_arguments+=("spring-boot:run" "--batch-mode")
#mvn_arguments+=("-Dspring-boot.run.profiles=database")
#mvn_arguments+=("-Dspring-boot.run.jvmArguments=${jvm_arguments[*]}")

#launching command
#mvn "${mvn_arguments[@]}"

#this is the end
echo "Application reset successfully."
exit 0
