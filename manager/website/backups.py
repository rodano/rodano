import logging
import uuid
import asyncio
import tasks
import config
import os
import time
import datetime
import configparser
import zipfile
from apscheduler.triggers.cron import CronTrigger
from apscheduler.schedulers.asyncio import AsyncIOScheduler

logger = logging.getLogger(__name__)

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
			config.BACKUP_STEPS,
			"automatic",
			"Scheduled backup"
		)
	)

#start the periodic backup scheduler
def start_periodic_backups():
	cron = config.BACKUP_CRON_EXPRESSION
	if cron:
		scheduler = AsyncIOScheduler()
		trigger = CronTrigger.from_crontab(cron)

		#add the job to scheduler
		scheduler.add_job(
			func=backup,
			trigger=trigger,
			id="backup_cron_job",
			name="Periodic backup task",
			replace_existing=False,
			executor="default"
		)

		#start the scheduler
		scheduler.start()
		logger.info(f"Periodic backup scheduler started with cron expression {cron}")

#cleanup old backups
async def cleanup():
	now = time.time()
	max_age = int(config.config.get("backups", "max_age"))
	rollover_age = int(config.config.get("backups", "rollover_age"))
	rollover_day = config.config.get("backups", "rollover_day")

	backup_storage_path = config.BACKUPS_STORAGE_PATH
	backups = [f for f in os.listdir(backup_storage_path) if os.path.isfile(os.path.join(backup_storage_path, f))]

	for backup in backups:
		path = os.path.join(backup_storage_path, backup)
		_, extension = os.path.splitext(backup)
		if extension == ".zip":
			#open backup archive
			with zipfile.ZipFile(path, "r") as zip_file:
				#look for the description file in the archive
				if "description.ini" in zip_file.namelist():
					#read the description file from zip
					with zip_file.open("description.ini") as description_file:
						description_content = description_file.read().decode("utf-8")

						#parse ini content
						description = configparser.ConfigParser()
						#add a fake DEFAULT section header
						description.read_string("[DEFAULT]\n" + description_content)

						#handle only automatic backups
						backup_type = description.get("DEFAULT", "backup.type")
						backup_date = description.get("DEFAULT", "backup.date")

						if backup_type == "automatic" and backup_date:
							date = datetime.datetime.fromisoformat(backup_date)
							timestamp = date.timestamp()
							age = now - timestamp
							valid = True
							#remove backups too old
							if age >= max_age:
								valid = False
							#remove backups that have been rolled over
							day = datetime.datetime.fromtimestamp(timestamp).strftime("%a")
							if age >= rollover_age and day != rollover_day:
								valid = False
							if not valid:
								#remove backup file
								logger.info(f"Removing old backup file {backup}")
								#os.unlink(os.path.join(backup_path, backup))

#start the backup cleanup scheduler
def start_backups_cleanup():
	cron = config.config.get("backups", "cleanup_cron_expression")
	scheduler = AsyncIOScheduler()
	trigger = CronTrigger.from_crontab(cron)

	#add the job to scheduler
	scheduler.add_job(
		func=cleanup,
		trigger=trigger,
		id="cleanup_cron_job",
		name="Cleanup backup task",
		replace_existing=False,
		executor="default"
	)

	#start the scheduler
	scheduler.start()
	logger.info(f"Cleanup backup scheduler started with cron expression {cron}")
