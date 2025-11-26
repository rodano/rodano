import os
import configparser
import logging

#configure logging
logging.basicConfig(
	level=logging.INFO,
	format="%(asctime)s [%(levelname)s] %(message)s",
	datefmt="%Y-%m-%d %H:%M:%S",
	handlers=[
		logging.StreamHandler()
	]
)

config = configparser.ConfigParser()
config.read(os.path.join(os.path.dirname(__file__), "config.ini"))

DEBUG = os.getenv("DEBUG", "false").lower() == "true"
DEBUG_PATH = os.path.join(os.path.dirname(__file__), "../scripts")

def get_script_path(script_name):
	base = DEBUG_PATH if DEBUG else config.get("DEFAULT", "base")
	return f"{base}/{config.get('scripts', script_name)}"

MAGIC_TOKEN = os.getenv("MAGIC_TOKEN")

BACKEND_HOSTNAME = "localhost" if DEBUG else "backend"

DATABASE_HOST = os.getenv("DATABASE_HOST", "localhost")
DATABASE_PORT = int(os.getenv("DATABASE_PORT", "3307" if DEBUG else "3306"))
DATABASE_NAME = os.getenv("DATABASE_NAME", "rodano")
DATABASE_USER = os.getenv("DATABASE_USER", "root")
DATABASE_PASSWORD = os.getenv("DATABASE_PASSWORD", "root")

DOCKER_COMPOSE_PREFIX = "rodano"
DOCKER_BACKEND_CONTAINER_NAME = DOCKER_COMPOSE_PREFIX + "-backend-1"

BACKUP_CRON_EXPRESSION = os.getenv("BACKUP_CRON_EXPRESSION", "0 3 * * *")
BACKUPS_STORAGE_PATH = os.getenv("BACKUPS_PATH", "/backups")
TEMPORARY_BACKUP_FILE_PATH = "/tmp/restore.zip"

RESTORE_STEPS = [
	"Deleting current database...",
	"Creating new database...",
	"Importing database...",
	"Deleting current data...",
	"Importing data...",
	"Deleting temporary file...",
	"Application restored successfully"
]

RESET_STEPS = [
	"Deleting current database...",
	"Retrieving last version of initialization script...",
	"Compiling initialization script...",
	"Executing initialization script...",
	"Application reset successfully"
]

CONTROL_STEPS = {
	"stop" : [
		"Stopping application...",
		"Application stopped successfully"
	],
	"start" : [
		"Starting application...",
		"Application started successfully"
	],
	"restart" : [
		"Stopping application...",
		"Starting application...",
		"Application restarted successfully"
	]
}

BACKUP_STEPS = [
	"Creating data backup...",
	"Creating database backup...",
	"Creating backup description...",
	"Creating backup package...",
	"Moving backup package...",
	"Application backed up successfully"
]
