package ch.rodano.api.project;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import ch.rodano.core.model.jooq.enums.ProjectStatus;

public record ProjectDTO(
	@NotNull UUID projectId,
	@NotNull String code,
	Map<String, String> shortname,
	Map<String, String> longname,
	Map<String, String> description,
	String url,
	String color,
	String introductionText,
	LocalDate versionDate,
	Long configDate,
	ProjectStatus status,
	ZonedDateTime created,
	Long activeConfigVersionFk
) {
}
