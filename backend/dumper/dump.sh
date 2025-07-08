#!/usr/bin/env bash

#retrieve parameters
DATABASE_HOST=${DATABASE_HOST:-"localhost"}
DATABASE_NAME=${DATABASE_NAME:-"rodano"}
DATABASE_USER=${DATABASE_USER:-"root"}
DATABASE_PASSWORD=${DATABASE_PASSWORD:-"root"}

DUMP_DIR="/dumps"
DUMP_FILE="${DUMP_DIR}/dump.sql.gz"

#fail if a variable is referenced before being set or if a command fails
set -u -e

echo "Starting database dump for database $DATABASE_NAME on host $DATABASE_HOST with user $DATABASE_USER"

mariadb_connection_arguments=("-h$DATABASE_HOST" "-u$DATABASE_USER" "-p$DATABASE_PASSWORD")

#retrieve tables to dump
#the goal is to exclude the views from the dump (they will re-created automatically anyway)
#this is because mariadb-dump hard-codes the name of the database in the SQL code of the views in the resulting file
#when views are in the dump, it's not possible to import the dump in a database with a different name
mariadb_tables_arguments=("${mariadb_connection_arguments[@]}")
mariadb_tables_arguments+=("-s" "-r" "-N" "-e")
mariadb_tables_arguments+=("select table_name from information_schema.tables where table_schema = '$DATABASE_NAME' and table_type != 'VIEW'")
mapfile -t tables < <(mariadb "${mariadb_tables_arguments[@]}")

echo "Dumping tables:" "${tables[@]}"

#build MariaDB dump arguments
mariadb_dump_arguments=("${mariadb_connection_arguments[@]}")
#for information on the different mariadb-dump parameters, check this page https://mariadb.com/kb/en/mariadb-dump/
mariadb_dump_arguments+=("--single-transaction" "--quick" "--hex-blob" "--order-by-primary" "--triggers" "--add-drop-trigger" "--routines")
mariadb_dump_arguments+=("$DATABASE_NAME")
mariadb_dump_arguments+=("${tables[@]}")

#dump database
mkdir -p "$DUMP_DIR"
mariadb-dump "${mariadb_dump_arguments[@]}" | gzip > "$DUMP_FILE"

echo "Dump done in ${DUMP_FILE}"
