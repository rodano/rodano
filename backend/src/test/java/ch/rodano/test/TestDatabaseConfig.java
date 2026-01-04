package ch.rodano.test;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testcontainers.containers.MariaDBContainer;

@TestConfiguration
public class TestDatabaseConfig {

	private static final MariaDBContainer<?> mariadb =
		new MariaDBContainer<>("mariadb:10.11")
			.withDatabaseName("rodano")
			.withUsername("test")
			.withPassword("test")
			.withInitScript("test-db-init.sql");

	static {
		mariadb.start();
		System.out.println("Testcontainer started: " + mariadb.getJdbcUrl());
	}

	@Bean
	@Primary
	public DataSource dataSource() {
		DataSource ds = DataSourceBuilder.create()
			.url(mariadb.getJdbcUrl())
			.username(mariadb.getUsername())
			.password(mariadb.getPassword())
			.driverClassName("org.mariadb.jdbc.Driver")
			.build();

		createScopeAncestorView(ds);

		return ds;
	}

	private void createScopeAncestorView(DataSource dataSource) {
		var populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("/database_scripts/structure/scope_ancestor.sql"));
		populator.execute(dataSource);
	}
}
