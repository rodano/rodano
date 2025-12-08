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

	private final String code;
	private final DSLContext dslContext;

	private UUID id;

	public ProjectIdResolver(final @Value("${rodano.project.code}") String code, final DSLContext dslContext) {
		if(code == null || code.isBlank()) {
			throw new IllegalStateException("rodano.project.code is missing/blank");
		}
		this.code = code.trim();
		this.dslContext = dslContext;
	}

	public String code() {
		return code;
	}

	public UUID id() {
		if(id != null) {
			return id;
		}

		try {
			id = dslContext.select(PROJECT.PROJECT_ID)
				.from(PROJECT)
				.where(PROJECT.CODE.eq(code))
				.fetchOne(PROJECT.PROJECT_ID);

			if(id == null) {
				LOGGER.error("Project with code {} not found in database", code);
				throw new IllegalStateException("Project " + code + " not found in database");
			}

			LOGGER.info("Resolved project '{}' to UUID {}", code, id);
			return id;
		}
		catch(Exception e) {
			LOGGER.error("Failed to resolve project '{}' to UUID {}", code, id, e);
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
