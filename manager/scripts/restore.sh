#!/usr/bin/env bash

#retrieve parameters
RESTORE_FILE=$1

#check parameters
if [[ -z $RESTORE_FILE ]]
then
	echo "Usage: restore_file"
	exit 1
fi

#fail if a variable is referenced before being set or if a command fails
set -u -e -x

#retrieve parameters
DATABASE_HOST=${DATABASE_HOST:-"localhost"}
DATABASE_PORT=${DATABASE_PORT:-3306}
DATABASE_USER=${DATABASE_USER:-"root"}
DATABASE_PASSWORD=${DATABASE_PASSWORD:-"root"}
DATABASE_NAME=${DATABASE_NAME:-"rodano"}

USER_CONTENT=${USER_CONTENT:-"/user_content"}

#prepare a dedicated directory to build the backup
backup_build_dir="/tmp/restore"
rm -rf "$backup_build_dir"
mkdir -p "$backup_build_dir"

#extract restore file
unzip "$RESTORE_FILE" -d "$backup_build_dir"

#restore database
mariadb_connection_arguments=("-h$DATABASE_HOST" "-u$DATABASE_USER" "-p$DATABASE_PASSWORD")

echo "Deleting current database..."
mariadb_command_arguments=("${mariadb_connection_arguments[@]}")
mariadb_command_arguments+=("-e" "DROP DATABASE IF EXISTS $DATABASE_NAME")
mariadb "${mariadb_command_arguments[@]}"

echo "Creating new database..."
mariadb_command_arguments=("${mariadb_connection_arguments[@]}")
mariadb_command_arguments+=("-e" "CREATE DATABASE $DATABASE_NAME")
mariadb "${mariadb_command_arguments[@]}"

echo "Importing database..."
mariadb_command_arguments=("${mariadb_connection_arguments[@]}")
mariadb_command_arguments+=("$DATABASE_NAME")
backup_db="$backup_build_dir/database.sql.gz"
gunzip < "$backup_db" | mariadb "${mariadb_command_arguments[@]}"

#restore data
echo "Deleting current data..."
rm -r "${USER_CONTENT:?}"/*

echo "Importing data..."
backup_data="$backup_build_dir/data.tar.gz"
tar -zxvf "$backup_data" -C "$USER_CONTENT"

echo "Deleting temporary file..."
rm "$RESTORE_FILE"

#this is the end
echo "Application restored successfully."
exit 0
