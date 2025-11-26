import os
import socket
import configparser
import logging
import aiodocker

#configure logging
logging.basicConfig(
	level=logging.INFO,
	format="%(asctime)s [%(levelname)s] %(message)s",
	datefmt="%Y-%m-%d %H:%M:%S",
	handlers=[
		logging.StreamHandler()
	]
)

logger = logging.getLogger(__name__)

config = configparser.ConfigParser()
config.read(os.path.join(os.path.dirname(__file__), "config.ini"))

DEBUG = os.getenv("DEBUG", "false").lower() == "true"
DEBUG_PATH = os.path.join(os.path.dirname(__file__), "../scripts")

def get_script_path(script_name):
	base = DEBUG_PATH if DEBUG else config.get("DEFAULT", "base")
	return f"{base}/{config.get("scripts", script_name)}"

MAGIC_TOKEN = os.getenv("MAGIC_TOKEN")

BACKEND_HOSTNAME = "localhost" if DEBUG else "backend"

DATABASE_HOST = os.getenv("DATABASE_HOST", "localhost")
DATABASE_PORT = int(os.getenv("DATABASE_PORT", "3307" if DEBUG else "3306"))
DATABASE_NAME = os.getenv("DATABASE_NAME", "rodano")
DATABASE_USER = os.getenv("DATABASE_USER", "root")
DATABASE_PASSWORD = os.getenv("DATABASE_PASSWORD", "root")

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

DOCKER_COMPOSE_PREFIX = "rodano"
DOCKER_BACKEND_SERVICE_NAME = "backend"
DOCKER_BACKEND_CONTAINER_NAME = DOCKER_COMPOSE_PREFIX + "-" + DOCKER_BACKEND_SERVICE_NAME + "-1"

async def discover_compose_environment():
	global DEBUG
	global DOCKER_COMPOSE_PREFIX
	global DOCKER_BACKEND_SERVICE_NAME
	global DOCKER_BACKEND_CONTAINER_NAME
	if not DEBUG:
		docker_client = aiodocker.Docker()
		current_hostname = socket.gethostname()

		current_container = await docker_client.containers.get(current_hostname)
		info = await current_container.show()
		labels = info.get("Config", {}).get("Labels", {})
		DOCKER_COMPOSE_PREFIX = labels.get("com.docker.compose.project")
		logger.info(f"Discovered Docker Compose project: {DOCKER_COMPOSE_PREFIX}")

		#find all running containers
		containers = await docker_client.containers.list()

		container_name = None
		for container in containers:
			info = await container.show()
			labels = info.get("Config", {}).get("Labels", {})
			if labels.get("com.docker.compose.service") == DOCKER_BACKEND_SERVICE_NAME and labels.get("com.docker.compose.project") == DOCKER_COMPOSE_PREFIX:
				container_name = info.get("Name", "").lstrip("/")
				break

		if container_name:
			DOCKER_BACKEND_CONTAINER_NAME = container_name
			logger.info(f"Discovered backend container: {DOCKER_BACKEND_CONTAINER_NAME}")
		else:
			logger.error(f"Unable to find backend container, using default name: {DOCKER_BACKEND_CONTAINER_NAME}")

		await docker_client.close()
