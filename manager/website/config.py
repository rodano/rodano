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

def update_docker_containers_names():
	global DOCKER_BACKEND_CONTAINER_NAME, DOCKER_FRONTEND_CONTAINER_NAME, DOCKER_MANAGER_CONTAINER_NAME
	DOCKER_BACKEND_CONTAINER_NAME = DOCKER_COMPOSE_PREFIX + "-backend-1"
	DOCKER_FRONTEND_CONTAINER_NAME = DOCKER_COMPOSE_PREFIX + "-frontend-1"
	DOCKER_MANAGER_CONTAINER_NAME = DOCKER_COMPOSE_PREFIX + "-manager-1"

update_docker_containers_names()

async def discover_compose_environment():
	global DOCKER_COMPOSE_PREFIX

	async with aiodocker.Docker() as docker_client:
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
			update_docker_containers_names()
		except aiodocker.exceptions.DockerError:
			#if not running inside a Docker container, the current hostname will not match any container name
			#this will trigger the error
			logger.warning(f"Not running inside a Docker container")

		logger.info(f"Assuming Docker containers names: backend={DOCKER_BACKEND_CONTAINER_NAME}, frontend={DOCKER_FRONTEND_CONTAINER_NAME}, manager={DOCKER_MANAGER_CONTAINER_NAME}")
