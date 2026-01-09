package ch.rodano.api.configurator;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import ch.rodano.core.model.jooq.enums.ProjectStatus;

public record ConfiguratorProjectDTO(
	UUID projectId,
	String code,
	Map<String, String> shortname,
	Map<String, String> longname,
	Map<String, String> description,
	String url,
	String color,
	String introductionText,
	LocalDate versionDate,
	ProjectStatus status,
	ZonedDateTime created,

	Long activeConfigVersionId,
	Integer activeVersionNumber,
	Long draftConfigVersionId,
	Integer draftVersionNumber
) {
}
