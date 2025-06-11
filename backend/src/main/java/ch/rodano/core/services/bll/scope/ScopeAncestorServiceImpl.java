package ch.rodano.core.services.bll.scope;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

@Service
public class ScopeAncestorServiceImpl {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final static String IS_STRUCTURE_PATH = "/database_scripts/structure/";

	private final DataSource dataSource;

	public ScopeAncestorServiceImpl(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void updateView() {
		logger.info("Updating scope ancestor view");
		final var scopeAncestorScript = new ClassPathResource(IS_STRUCTURE_PATH + "scope_ancestor.sql");
		final var databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.addScript(scopeAncestorScript);
		databasePopulator.execute(dataSource);
	}

}
