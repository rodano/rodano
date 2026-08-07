package ch.rodano.core.scheduler.task.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.scheduler.task.ScheduledTask;
import ch.rodano.core.services.bll.file.FileService;

@Component
@ConditionalOnProperty(value = "rodano.schedule.file-cleaner", havingValue = "true")
public class FileCleanerTask implements ScheduledTask, DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final FileService fileService;

	public FileCleanerTask(
		final FileService fileService
	) {
		this.fileService = fileService;
		logger.info("File cleaner started");
	}

	@Scheduled(zone = "UTC", cron = "${rodano.schedule.file-cleaner.cron}")
	@Transactional
	@Override
	public void run() {
		fileService.deleteUnsubmittedFiles();
		logger.debug("Unsubmitted files cleaned up done.");
	}

	@Override
	public void destroy() {
		logger.info("File cleaner stopped");
	}
}
