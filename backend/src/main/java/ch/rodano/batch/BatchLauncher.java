package ch.rodano.batch;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.StepExecution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BatchLauncher implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchLauncher.class);

	@Override
	public void run(final String... args) throws Exception {
		final var params = getProperties(args);
		if(!params.containsKey("job")) {
			LOGGER.info("No --job specified. Skipping batch launch.");
			return;
		}

		final JobOperator op = BatchRuntime.getJobOperator();
		final long execId = op.start(params.getProperty("job"), params);
		LOGGER.info("Batch job '{}' started. execId={}", params.getProperty("job"), execId);

		BatchStatus status;
		while(true) {
			final JobExecution je = op.getJobExecution(execId);
			status = je.getBatchStatus();
			LOGGER.debug("Job status: {}", status);
			if(status == BatchStatus.COMPLETED || status == BatchStatus.FAILED
				|| status == BatchStatus.STOPPED || status == BatchStatus.ABANDONED) {
				final List<StepExecution> steps = op.getStepExecutions(execId);
				for(StepExecution se : steps) {
					LOGGER.info("Step '{}' metrics={}", se.getStepName(), se.getMetrics());
				}
				LOGGER.info("Job finished with status={} exitStatus={}", status, op.getJobExecution(execId).getExitStatus());
				break;
			}
			Thread.sleep(Duration.ofSeconds(1).toMillis());
		}
	}

	private static Properties getProperties(final String[] args) {
		final var params = new Properties();
		for(String a : args) {
			if(a.startsWith("--job=")) {
				params.put("job", a.substring(6));
			}
			if(a.startsWith("--config=")) {
				params.put("config", a.substring(9));
			}
			if(a.startsWith("--projectId=")) {
				params.put("projectId", a.substring(12));
			}
			if(a.startsWith("--db.url=")) {
				params.put("db.url", a.substring(9));
			}
			if(a.startsWith("--db.user=")) {
				params.put("db.user", a.substring(10));
			}
			if(a.startsWith("--db.pass=")) {
				params.put("db.pass", a.substring(10));
			}
		}
		return params;
	}
}
