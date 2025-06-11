package ch.rodano.core.configuration.jooq;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderImplicitJoinType;
import org.jooq.conf.Settings;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

@Configuration
public class PersistentContext {

	private final DataSource dataSource;

	public PersistentContext(
		final DataSource dataSource
	) {
		this.dataSource = dataSource;
	}

	@Bean
	public DataSourceTransactionManager transactionManager() {
		return new AuditActionTransactionManager(dataSource);
	}

	@Bean
	public DataSourceConnectionProvider connectionProvider() {
		return new DataSourceConnectionProvider(
			new TransactionAwareDataSourceProxy(dataSource)
		);
	}

	@Bean
	public DefaultConfigurationCustomizer configurationCustomizer() {
		return configuration -> configuration.set(connectionProvider());
	}

	@Bean
	public DSLContext dsl() {
		return new DefaultDSLContext(configuration());
	}

	public DefaultConfiguration configuration() {
		final var config = new DefaultConfiguration();
		config.set(connectionProvider());
		config.set(SQLDialect.MARIADB);
		config.set(new Settings().withRenderImplicitJoinToManyType(RenderImplicitJoinType.INNER_JOIN));
		return config;
	}
}
