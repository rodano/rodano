import os
import json
import logging
import re
import uuid
import asyncio
import pymysql
import helpers
import aiodocker
import messaging
import tasks
import tornado
from apscheduler.triggers.cron import CronTrigger
from apscheduler.schedulers.asyncio import AsyncIOScheduler

#debug mode allows to run the manager locally
DEBUG = os.getenv("DEBUG", "false").lower() == "true"

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

logger = logging.getLogger(__name__)
regexp_backup_rationale = re.compile(r'^[A-Za-z0-9-_ ]+$')
regexp_backup_id = re.compile(r'^[A-Za-z0-9-_]+\.zip$')

#global status
class ApplicationInfo(helpers.AuthenticatedRequestHandler):
	async def get(self):
		backend = await aiodocker.Docker().containers.get(DOCKER_BACKEND_CONTAINER_NAME)
		container_info = await backend.show()
		if container_info['State']['Status'] != "running":
			self.set_status(503)
			self.write({"error" : "Backend must be started to get application information."})
			return

		try:
			application = {}

			#retrieve study information from backend API
			http = tornado.httpclient.AsyncHTTPClient()
			try:
				response = await http.fetch(
					f"http://{BACKEND_HOSTNAME}:8080/config/public-study",
					request_timeout=5
				)
			except tornado.httpclient.HTTPClientError:
				self.set_status(503)
				self.write({"error" : "Backend is not responding."})
				return
			if response.code != 200:
				self.set_status(503)
				self.write({"error" : "Backend is not responding."})
				return
			study = json.loads(response.body.decode("utf-8"))
			application["name"] = study["id"]
			#override environment to PROD in debug mode to be able to perform backups
			application["environment"] = "PROD" if DEBUG else study["environment"]

			#retrieve environment variables from container config
			env_list = container_info.get('Config', {}).get('Env', [])
			env = {}
			for entry in env_list:
				if "=" in entry:
					key, value = entry.split("=", 1)
					env[key] = value
			application["env"] = env

			self.write(json.dumps(application, cls=helpers.JSONCustomEncoder))
		except Exception:
			logger.exception("Error retrieving application info")
			self.set_status(503)
			self.write({"error" : "Backend is not responding."})

class ApplicationBackups(helpers.AuthenticatedRequestHandler):
	def get(self):
		try:
			#ensure the backup path exists
			if not os.path.exists(BACKUPS_STORAGE_PATH):
				self.set_status(500)
				self.write({"error" : f"Backup path {BACKUPS_STORAGE_PATH} does not exist."})
				return

			#retrieve all backup files from the backup path
			entries = []
			with os.scandir(BACKUPS_STORAGE_PATH) as it:
				for entry in it:
					if entry.is_file():
						try:
							modification_time = entry.stat().st_mtime
						except Exception:
							modification_time = 0
						entries.append((entry.name, modification_time))

			#sort by modification time (newest first)
			entries.sort(key=lambda t: t[1], reverse=True)
			backups = [name for name, _ in entries]

			self.write(json.dumps(backups, cls=helpers.JSONCustomEncoder))
		except Exception as e:
			self.set_status(500)
			self.write({"error" : f"Unable to retrieve backups: {str(e)}."})

class ApplicationBackupDownload(helpers.AuthenticatedRequestHandler):
	def get(self, backup_id):
		try:
			#ensure the backup path exists
			if not os.path.exists(BACKUPS_STORAGE_PATH):
				self.set_status(500)
				self.write({"error" : f"Backup path {BACKUPS_STORAGE_PATH} does not exist."})
				return

			backup_file_path = os.path.join(BACKUPS_STORAGE_PATH, backup_id)
			if not os.path.isfile(backup_file_path):
				self.set_status(404)
				self.write({"error" : f"Backup file {backup_id} does not exist."})
				return

			#send the backup file
			self.set_header("Content-Type", "application/zip")
			self.set_header("Content-Disposition", f'attachment; filename="{backup_id}"')
			with open(backup_file_path, "rb") as backup_file:
				while True:
					data = backup_file.read(4096)
					if not data:
						break
					self.write(data)
			self.finish()
		except Exception as e:
			self.set_status(500)
			self.write({"error" : f"Unable to retrieve backups: {str(e)}."})

class ApplicationUsers(helpers.AuthenticatedRequestHandler):
	def get(self):
		try:
			database = DATABASE_NAME
			#create connection
			connection = pymysql.connect(
				host=DATABASE_HOST,
				port=DATABASE_PORT,
				user=DATABASE_USER,
				password=DATABASE_PASSWORD,
				database=database)

			try:
				#set appropriate time zone
				with connection.cursor() as cursor:
					cursor.execute("SET time_zone = '+00:00';")
				connection.commit()

				#retrieve connected users
				with connection.cursor() as cursor:
					cursor.execute("SELECT NAME, EMAIL, LOGIN_DATE FROM user WHERE LOGIN_DATE IS NOT NULL AND LOGIN_DATE > DATE_ADD(NOW(), INTERVAL -2 HOUR) AND LOGOUT_DATE IS NULL ORDER BY LOGIN_DATE DESC")
					users = []
					for row in cursor.fetchall():
						users.append({"name" : row[0], "email" : row[1], "login_date" : row[2]})
					self.write(json.dumps(users, cls=helpers.JSONCustomEncoder))
			finally:
				connection.close()
		except pymysql.ProgrammingError:
			self.set_status(500)
			self.write({"error" : f"No database with name {database}."})

#application reset
class ApplicationReset(helpers.AuthenticatedRequestHandler):
	def post(self):
		#retrieve parameters
		id = self.get_argument("id", str(uuid.uuid1()))
		reference = self.get_argument("reference")
		demo_users = self.get_argument("demo_users")
		demo_users_password = self.get_argument("demo_users_password")
		demo_data = self.get_argument("demo_data")

		if tasks.tasks["reset"] is None:
			#create task
			tasks.tasks["reset"] = {"id" : id}
			#terminate http request
			self.write({"message" : "Reset started. Watch tasks to get progression of reset.", "id" : tasks.tasks["reset"]["id"], "steps" : len(RESET_STEPS)})
			self.finish()
			#run task
			asyncio.create_task(
				tasks.do_task(
					"reset",
					RESET_STEPS,
					reference or "origin/master",
					demo_data or "no",
					demo_users or "no",
					demo_users_password or ""
				)
			)
		else:
			self.set_status(403)
			self.write({"error" : "There is already one application database being reset right now. Please retry in a moment."})
			self.finish()

#application restore
class ApplicationRestore(helpers.AuthenticatedRequestHandler):
	def post(self):
		#retrieve parameters
		id = self.get_argument("id", str(uuid.uuid1()))
		restore_type = self.get_argument("type", "")
		restore_file = self.get_argument("file", "")

		if tasks.tasks["restore"] is None:
			#create task
			tasks.tasks["restore"] = {"id" : id}
			#optional backup data sha1
			with open(TEMPORARY_BACKUP_FILE_PATH, "wb") as backup_file:
				#for database file, store file in temporary folder
				if restore_type == "file":
					backup_file.write(self.request.files["file"][0]["body"])
				#for backup file, retrieve file on backup server
				if restore_type == "backup":
					if not regexp_backup_id.match(restore_file):
						self.set_status(400)
						self.write({"error" : "Invalid characters in backup file name"})
						self.finish()
						return
					with open(f"{BACKUPS_STORAGE_PATH}/{restore_file}", "rb") as source_backup:
						backup_file.write(source_backup.read())

				#terminate http request
				self.write({"message" : "Restoration started. Watch tasks to get progression of restoration.", "id" : tasks.tasks["restore"]["id"], "steps" : len(RESTORE_STEPS)})
				self.finish()
				#run task
				asyncio.create_task(
					tasks.do_task(
						"restore",
						RESTORE_STEPS,
						TEMPORARY_BACKUP_FILE_PATH
					)
				)
		else:
			self.set_status(403)
			self.write({"error" : "There is already one application database being restored right now. Please retry in a moment."})
			self.finish()

#application control
async def do_control(action):
	await tasks.do_task("control", CONTROL_STEPS[action], action)

class ApplicationControl(helpers.AuthenticatedRequestHandler):
	def post(self):
		#retrieve parameters
		action = self.get_argument("action")
		id = self.get_argument("id", str(uuid.uuid1()))

		if tasks.tasks["control"] is None:
			#create task
			tasks.tasks["control"] = {"id" : id}
			#terminate http request
			self.write({"message" : f"Action [{action}] launched. Watch tasks to get progression of action.", "id" : tasks.tasks["control"]["id"], "steps" : len(CONTROL_STEPS[action])})
			self.finish()
			#run task
			asyncio.create_task(
				do_control(action)
			)
		else:
			self.set_status(403)
			self.write({"error" : "There is already one action launched on the application right now. Please retry in a moment."})
			self.finish()

#application backup
class ApplicationBackup(helpers.AuthenticatedRequestHandler):
	def post(self):
		#retrieve and check parameters
		id = self.get_argument("id", str(uuid.uuid1()))
		rationale = self.get_argument("rationale")
		if not regexp_backup_rationale.match(rationale):
			raise Exception("Invalid characters in rationale")

		if tasks.tasks["backup"] is None:
			#create task
			tasks.tasks["backup"] = {"id" : id}
			#terminate http request
			self.write({"message" : "Backup launched. Watch tasks to get progression of backup.", "id" : tasks.tasks["backup"]["id"], "steps" : len(BACKUP_STEPS)})
			self.finish()
			#run task
			asyncio.create_task(
				tasks.do_task(
					"backup",
					BACKUP_STEPS,
					"manual",
					rationale
				)
			)
		else:
			self.set_status(403)
			self.write({"error" : "There is already one backup launched on one application right now. Please retry in a moment."})
			self.finish()


#send log in web socket
async def stream_logs():
	while True:
		try:
			backend = await aiodocker.Docker().containers.get(DOCKER_BACKEND_CONTAINER_NAME)
			container_info = await backend.show()
			#wait until the backend container is running if necessary
			if container_info['State']['Status'] != "running":
				logger.info(f"Backend container {DOCKER_BACKEND_CONTAINER_NAME} is not running")
				await asyncio.sleep(10)
				continue

			#stream logs
			async for line in backend.log(stdout=True, stderr=True, follow=True):
				if line:
					messaging.broadcast_message({"type": messaging.MessageType.LOG.value, "data": line})

		except Exception as e:
			logger.error(f"Error streaming logs: {e}")
			await asyncio.sleep(5)

#start the log streaming task
def start_log_streaming():
	asyncio.create_task(stream_logs())
	logger.info(f"Log streaming started for container {DOCKER_BACKEND_CONTAINER_NAME}")

#perform a backup
async def backup():
	#check if a backup task is already running
	if tasks.tasks["backup"] is not None:
		logger.warning("Another backup task is already running, skipping scheduled backup")
		return

	task_id = str(uuid.uuid1())

	#create task
	tasks.tasks["backup"] = {"id": task_id}

	#run task
	asyncio.create_task(
		tasks.do_task(
			"backup",
			BACKUP_STEPS,
			"automatic",
			"Scheduled backup"
		)
	)

#start the periodic backup scheduler
def start_periodic_backups():
	if BACKUP_CRON_EXPRESSION:
		scheduler = AsyncIOScheduler()
		trigger = CronTrigger.from_crontab(BACKUP_CRON_EXPRESSION)

		#add the job to scheduler
		scheduler.add_job(
			func=backup,
			trigger=trigger,
			id='backup_cron_job',
			name='Periodic backup task',
			replace_existing=False,
			executor='default'
		)

		#start the scheduler
		scheduler.start()
		logger.info(f"Periodic backup scheduler started with cron expression {BACKUP_CRON_EXPRESSION}")

url_pattern = [
	("/api/info", ApplicationInfo),
	("/api/backups", ApplicationBackups),
	(f"/api/backups/([A-Za-z0-9-_.]+)", ApplicationBackupDownload),
	("/api/users", ApplicationUsers),
	("/api/control", ApplicationControl),
	("/api/backup", ApplicationBackup),
	("/api/reset", ApplicationReset),
	("/api/restore", ApplicationRestore)
]
