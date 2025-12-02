import logging
import uuid
import asyncio
import tasks
import config
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
	if config.BACKUP_CRON_EXPRESSION:
		scheduler = AsyncIOScheduler()
		trigger = CronTrigger.from_crontab(config.BACKUP_CRON_EXPRESSION)

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
		logger.info(f"Periodic backup scheduler started with cron expression {config.BACKUP_CRON_EXPRESSION}")
