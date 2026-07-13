#!/usr/bin/env bash

#retrieve parameters
TYPE=$1
RATIONALE=$2

#check parameters
if [[ -z $TYPE ]]
then
	echo "Usage: type, [rationale]"
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

USER_CONTENT=${USER_CONTENT:-"/user_content"}
BACKUPS_PATH=${BACKUPS_PATH:-"/backups"}

#generate backup id
date_file=$(date -u +"%Y-%m-%d-%H-%M-%S")
backup_id="backup_"$date_file

#prepare a dedicated directory to build the backup
backup_build_dir="/tmp/$backup_id"
rm -rf "$backup_build_dir"
mkdir -p "$backup_build_dir"

#generate data backup file
echo "Creating data backup..."
backup_data="$backup_build_dir/data.tar.gz"

#compress files
tar -zcvf "$backup_data" -C "${USER_CONTENT}" .

#generate database backup sha1sum
backup_data_sha1=$(sha1sum "$backup_data" | awk '{print $1}')

#generate database backup file
echo "Creating database backup..."
backup_db="$backup_build_dir/database.sql"

#build database connection parameters
mariadb_connection_arguments=("-h$DATABASE_HOST" "-P$DATABASE_PORT" "-u$DATABASE_USER" "-p$DATABASE_PASSWORD")

#retrieve tables to dump
#the goal is to exclude the views from the dump (they will re-created by kv anyway)
#this is because mariadb-dump hard-codes the name of the database in the SQL code of the views in the resulting file
#when views are in the dump, it's not possible to import the dump in a database with a different name
mariadb_tables_arguments=("${mariadb_connection_arguments[@]}")
mariadb_tables_arguments+=("-s" "-r" "-N" "-e")
mariadb_tables_arguments+=("select table_name from information_schema.tables where table_schema = '$DATABASE_NAME' and table_type != 'VIEW'")
mapfile -t tables < <(mariadb "${mariadb_tables_arguments[@]}")

#build mariadb dump arguments
mariadb_dump_arguments=("${mariadb_connection_arguments[@]}")
mariadb_dump_arguments+=("--single-transaction" "--quick" "--hex-blob" "--order-by-primary" "--triggers" "--add-drop-trigger" "--routines")
mariadb_dump_arguments+=("$DATABASE_NAME")
mariadb_dump_arguments+=("${tables[@]}")

#dump database
mariadb-dump "${mariadb_dump_arguments[@]}" > "$backup_db"

#generate database backup sha1sum
backup_db_sha1=$(sha1sum "$backup_db" | awk '{print $1}')

#compress backup
gzip --best "$backup_db"

#generate backup description file
echo "Creating backup description..."
backup_description="$backup_build_dir/description.ini"

#build backup properties file
{
	echo "backup.id=$backup_id"
	echo "backup.db.sha1=$backup_db_sha1"
	echo "backup.data.sha1=$backup_data_sha1"
	echo "backup.date=$(date -u +%Y-%m-%dT%H:%M:%S%z)"
	echo "backup.type=$TYPE"
} > "$backup_description"
[[ -n $RATIONALE ]] && echo "backup.rationale=\"$RATIONALE\"" >> "$backup_description"


#build final package
echo "Creating backup package..."
backup_package_basename=$backup_id".zip"
backup_package=$backup_build_dir"/"$backup_package_basename
zip -j "$backup_package" "$backup_build_dir/description.ini" "$backup_build_dir/database.sql.gz" "$backup_build_dir/data.tar.gz"

#store the backup to the backup storage
echo "Moving backup package..."
mv "$backup_package" "$BACKUPS_PATH"

#remove temporary files
rm -r "$backup_build_dir"

#this is the end
echo "Application backed up successfully."
exit 0
