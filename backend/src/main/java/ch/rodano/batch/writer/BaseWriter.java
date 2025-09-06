package ch.rodano.batch.writer;

import java.sql.Connection;
import java.sql.DriverManager;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.inject.Inject;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseWriter extends AbstractItemWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseWriter.class);

	@Inject
	@BatchProperty(name = "db.url")
	private String dbUrl;

	@Inject
	@BatchProperty(name = "db.user")
	private String dbUser;

	@Inject
	@BatchProperty(name = "db.pass")
	private String dbPass;

	private Connection conn;
	public DSLContext dsl;

	public void initIfNeeded() throws Exception {
		if(dsl != null) {
			return;
		}
		conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
		dsl = DSL.using(conn, SQLDialect.MARIADB);
	}

	@Override
	public void close() throws Exception {
		if(conn != null) {
			try {
				conn.close();
			}
			catch(Exception e) {
				LOGGER.error("Error closing connection", e);
				throw e;
			}
			conn = null;
			dsl = null;
		}
	}
}
