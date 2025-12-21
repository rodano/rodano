package ch.rodano.core.services.project;

import java.util.UUID;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import static ch.rodano.core.model.jooq.tables.Project.PROJECT;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProjectIdResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectIdResolver.class);

	private final String defaultCode;
	private final DSLContext dslContext;

	private UUID id;

	public ProjectIdResolver(final @Value("${rodano.project.code:}") String defaultCode, final DSLContext dslContext) {
		this.defaultCode = defaultCode != null ? defaultCode.trim() : "";
		this.dslContext = dslContext;

		if(this.defaultCode.isBlank()) {
			LOGGER.info("No default project code configured. Multi-project mode enabled.");
		}
		else {
			LOGGER.info("Default project code configured: {}", this.defaultCode);
		}
	}

	public String code() {
		return defaultCode;
	}

	public UUID id() {
		if(id != null) {
			return id;
		}

		if (defaultCode.isBlank()) {
			LOGGER.debug("No project selected and no default code configured");
			return null;
		}

		try {
			id = dslContext.select(PROJECT.PROJECT_ID)
				.from(PROJECT)
				.where(PROJECT.CODE.eq(defaultCode))
				.fetchOne(PROJECT.PROJECT_ID);

			if(id == null) {
				LOGGER.error("Default project with code {} not found in database", defaultCode);
				throw new IllegalStateException("Project " + defaultCode + " not found in database");
			}

			LOGGER.info("Resolved default project '{}' to UUID {}", defaultCode, id);
			return id;
		}
		catch(Exception e) {
			LOGGER.error("Failed to resolve default project '{}' to UUID {}", defaultCode, id, e);
			return null;
		}
	}

	public UUID resolveCode(final String code) {
		return dslContext
			.select(PROJECT.PROJECT_ID)
			.from(PROJECT)
			.where(PROJECT.CODE.eq(code))
			.fetchOne(PROJECT.PROJECT_ID);
	}

	public void setProjectId(final UUID projectId) {
		this.id = projectId;
	}

	public boolean hasProject() {
		return id != null;
	}

	public void clearProject() {
		this.id = null;
	}
}
