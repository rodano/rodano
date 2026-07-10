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

def get_script_path(script_name):
	base = config.get("DEFAULT", "base")
	if not os.path.isabs(base):
		base = os.path.join(os.path.dirname(__file__), base)
	return f"{base}/{config.get('scripts', script_name)}"

MAGIC_TOKEN = os.getenv("MAGIC_TOKEN")

BACKEND_HOST = os.getenv("BACKEND_HOST", "localhost")
BACKEND_PORT = int(os.getenv("BACKEND_PORT", "8080"))

DATABASE_HOST = os.getenv("DATABASE_HOST", "localhost")
DATABASE_PORT = int(os.getenv("DATABASE_PORT", "3306"))
DATABASE_NAME = os.getenv("DATABASE_NAME", "rodano")
DATABASE_USER = os.getenv("DATABASE_USER", "root")
DATABASE_PASSWORD = os.getenv("DATABASE_PASSWORD", "root")

BACKUPS_STORAGE_PATH = os.getenv("BACKUPS_STORAGE_PATH", "/tmp/backups")
BACKUP_CRON_EXPRESSION = os.getenv("BACKUP_CRON_EXPRESSION", "0 3 * * *")
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
DOCKER_BACKEND_CONTAINER_NAME = DOCKER_COMPOSE_PREFIX + "-backend-1"

async def discover_compose_environment():
	global DOCKER_COMPOSE_PREFIX
	global DOCKER_BACKEND_CONTAINER_NAME

	docker_client = aiodocker.Docker()
	current_hostname = socket.gethostname()

	#discover if the app is run from inside the Docker Compose environment
	#this is the standard mode of this application
	#in that case, it's possible to deduce the Docker Compose project name
	try:
		current_container = await docker_client.containers.get(current_hostname)
		info = await current_container.show()
		labels = info.get("Config", {}).get("Labels", {})

		DOCKER_COMPOSE_PREFIX = labels.get("com.docker.compose.project")
		logger.info(f"Discovered Docker Compose project: {DOCKER_COMPOSE_PREFIX}")
	except aiodocker.exceptions.DockerError:
		#if not running inside a Docker container, the current hostname will not match any container name
		#this will trigger the error
		logger.warning(f"Not running inside a Docker container")

	#find the backend container
	#this must be done even if the app is not running inside the Docker Compose environment
	#having access to the backend container allows to start/stop the container and to fetch the logs
	containers = await docker_client.containers.list()

	container_name = None
	for container in containers:
		info = await container.show()
		labels = info.get("Config", {}).get("Labels", {})
		if labels.get("com.docker.compose.project") == DOCKER_COMPOSE_PREFIX and labels.get("com.docker.compose.service") == BACKEND_HOST:
			container_name = info.get("Name", "").lstrip("/")
			break

	if container_name:
		DOCKER_BACKEND_CONTAINER_NAME = container_name
		logger.info(f"Found backend container: {DOCKER_BACKEND_CONTAINER_NAME}")
	else:
		logger.warning(f"Unable to find backend container, using default name: {DOCKER_BACKEND_CONTAINER_NAME}")

	await docker_client.close()
