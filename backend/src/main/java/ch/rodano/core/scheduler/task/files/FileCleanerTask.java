package ch.rodano.core.scheduler.task.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ch.rodano.core.scheduler.task.ScheduledTask;
import ch.rodano.core.services.bll.file.FileService;

@Component
@ConditionalOnProperty(value = "rodano.schedule.file-cleaner", havingValue = "true")
public class FileCleanerTask implements ScheduledTask, DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final PlatformTransactionManager transactionManager;
	private final FileService fileService;

	public FileCleanerTask(
		final PlatformTransactionManager transactionManager,
		final FileService fileService
	) {
		this.transactionManager = transactionManager;
		this.fileService = fileService;
		logger.info("File cleaner started");
	}

	@Scheduled(zone = "UTC", cron = "${rodano.schedule.file-cleaner.cron}")
	@Override
	public void run() {
		logger.debug("Cleaning un-submitted files...");

		final var transactionTemplate = new TransactionTemplate(transactionManager);

		transactionTemplate.execute(_ -> {
			logger.debug("Deleting un-submitted user files...");
			fileService.deleteUnsubmittedFiles();

			return true;
		});

		logger.debug("File clean-up done.");
	}

	@Override
	public void destroy() {
		logger.info("File cleaner stopped");
	}
}
