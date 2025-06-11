package ch.rodano.core.scheduler.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Profile("api")
@Configuration
@EnableScheduling
public class TaskSchedulerConfigurer {
	@Bean
	public TaskScheduler taskScheduler() {
		final var threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setThreadNamePrefix("scheduler-pool-");

		return threadPoolTaskScheduler;
	}
}
