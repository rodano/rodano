package ch.rodano.application;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ch.rodano.core.services.migration.MigrationService;

@Profile("migration")
@Component
public class MigrationRunner implements CommandLineRunner {

	private final MigrationService migrationService;
	private final ConfigurableApplicationContext context;

	public MigrationRunner(
		final MigrationService migrationService,
		final ConfigurableApplicationContext context
	) {
		this.migrationService = migrationService;
		this.context = context;
	}

	@Override
	public void run(final String... args) {
		final var success = migrationService.run();

		final var exitCode = SpringApplication.exit(context, () -> success ? 0 : 1);

		if(exitCode != 0) {
			System.exit(exitCode);
		}
	}
}
