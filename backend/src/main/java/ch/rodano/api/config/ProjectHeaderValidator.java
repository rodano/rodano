package ch.rodano.api.config;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import ch.rodano.core.services.project.ProjectIdResolver;

@Component
public class ProjectHeaderValidator {

	private final ProjectIdResolver projectIdResolver;

	public ProjectHeaderValidator(final ProjectIdResolver projectIdResolver) {
		this.projectIdResolver = projectIdResolver;
	}

	public void validate(final UUID headerProjectId) {

		if(headerProjectId == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "X-Project-Id header is required");
		}

		if(!projectIdResolver.hasProject()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No project loaded in session. Please select a project first.");
		}

		final UUID sessionProjectId = projectIdResolver.id();

		if(!headerProjectId.equals(sessionProjectId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Project mismatch: header projectId (%s) does not match session projectId (%s)", headerProjectId, sessionProjectId));
		}
	}
}
