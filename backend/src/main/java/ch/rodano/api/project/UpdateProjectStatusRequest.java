package ch.rodano.api.project;

import ch.rodano.core.model.jooq.enums.ProjectStatus;

public record UpdateProjectStatusRequest(
	ProjectStatus status
) {
}
