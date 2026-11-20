import logging
import uuid
import asyncio
import tasks
import config
import os
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

#read the description file embedded in a backup archive
#return None if the archive does not contain a valid description
def read_description(path):
	with zipfile.ZipFile(path, "r") as zip_file:
		if "description.ini" not in zip_file.namelist():
			return None
		with zip_file.open("description.ini") as description_file:
			description_content = description_file.read().decode("utf-8")

	#parse ini content, adding a fake DEFAULT section header
	description = configparser.ConfigParser(interpolation=None)
	description.read_string("[DEFAULT]\n" + description_content)
	values = description["DEFAULT"]

	backup_date = values.get("backup.date")
	return {
		"type": values.get("backup.type"),
		"date": datetime.datetime.fromisoformat(backup_date) if backup_date else None,
		"rationale": values.get("backup.rationale", "").strip('"') or None
	}

#return the date after which a backup will be deleted by the cleanup task, or None if it is kept forever
#only automatic backups expire, manual backups are kept forever
def get_expiration_date(backup_type, backup_date):
	if backup_type != "automatic" or backup_date is None:
		return None
	max_age = int(config.config.get("backups", "max_age"))
	rollover_age = int(config.config.get("backups", "rollover_age"))
	rollover_day = config.config.get("backups", "rollover_day")
	#backups made on the rollover day are kept until they reach the maximum age, others until they reach the rollover age
	#the day is computed on the date recorded in the backup (in UTC) so the result does not depend on the server timezone
	age = max_age if backup_date.strftime("%a") == rollover_day else min(rollover_age, max_age)
	return backup_date + datetime.timedelta(seconds=age)

#list all backups with their information
def list_backups():
	backups = []
	with os.scandir(config.BACKUPS_STORAGE_PATH) as it:
		for entry in it:
			if not entry.is_file():
				continue
			stat = entry.stat()
			backup = {
				"id": entry.name,
				"size": stat.st_size,
				"date": datetime.datetime.fromtimestamp(stat.st_mtime, datetime.timezone.utc),
				"type": None,
				"rationale": None,
				"expiration_date": None
			}
			if entry.name.endswith(".zip"):
				try:
					description = read_description(entry.path)
				except Exception as e:
					logger.warning(f"Unable to read description of backup {entry.name}: {e}")
					description = None
				if description:
					backup["type"] = description["type"]
					backup["rationale"] = description["rationale"]
					if description["date"]:
						backup["date"] = description["date"]
					backup["expiration_date"] = get_expiration_date(description["type"], description["date"])
			backups.append(backup)
	backups.sort(key=lambda b: b["date"], reverse=True)
	return backups

#cleanup expired backups
async def cleanup():
	now = datetime.datetime.now(datetime.timezone.utc)
	for backup in list_backups():
		expiration_date = backup["expiration_date"]
		if expiration_date is not None and expiration_date <= now:
			logger.info(f"Removing expired backup file {backup['id']}")
			try:
				os.unlink(os.path.join(config.BACKUPS_STORAGE_PATH, backup["id"]))
			except Exception:
				logger.exception(f"Unable to remove expired backup file {backup['id']}")

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
