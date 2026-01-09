package ch.rodano.api.configurator;

import java.time.ZonedDateTime;
import java.util.UUID;

import ch.rodano.core.model.jooq.enums.ProjectConfigVersionStatus;

public record ProjectConfigVersionDTO(
	Long pk,
	UUID projectId,
	Integer versionNumber,
	ProjectConfigVersionStatus status,
	ZonedDateTime createdAt,
	String createdByName,
	ZonedDateTime publishedAt,
	String publishedByName,
	String changeSummary
) {
}
