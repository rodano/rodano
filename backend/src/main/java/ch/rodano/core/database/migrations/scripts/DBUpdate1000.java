package ch.rodano.core.database.migrations.scripts;

import java.util.List;

import ch.rodano.core.database.migrations.AbstractDatabaseMigration;
import ch.rodano.core.database.migrations.MigrationBean;
import ch.rodano.core.database.migrations.MigrationTask;
import ch.rodano.core.services.bll.scope.ScopeService;

@MigrationBean
public class DBUpdate1000 extends AbstractDatabaseMigration {
	private static final String CONTEXT = "Fake migration to test migration process";

	private final ScopeService scopeService;

	public DBUpdate1000(final ScopeService scopeService) {
		this.scopeService = scopeService;
	}

	@Override
	protected Double migrationTaskNumber() {
		return 1000D;
	}

	@Override
	protected String context() {
		return CONTEXT;
	}

	@Override
	protected List<MigrationTask> tasks() {
		return List.of(recalculateExpectedDates());
	}

	private MigrationTask recalculateExpectedDates() {
		return new MigrationTask() {
			@Override
			public boolean run() {
				final var rootScope = scopeService.getRootScope();
				logger.debug("The root scope code is {}", rootScope.getCode());
				return true;
			}

			@Override
			public String description() {
				return CONTEXT;
			}
		};
	}
}
