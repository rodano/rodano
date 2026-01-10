package ch.rodano.api.configurator;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import ch.rodano.core.model.jooq.enums.ProjectConfigVersionStatus;
import ch.rodano.core.model.jooq.enums.ProjectStatus;

public record ConfiguratorProjectDTO(
	@NotNull UUID projectId,
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
	ProjectConfigVersionStatus activeConfigVersionStatus,

	Long draftConfigVersionId,
	Integer draftVersionNumber,
	ProjectConfigVersionStatus draftConfigVersionStatus,

	boolean hasArchivedVersions,

	String email,
	Boolean smtpTls,
	Boolean passwordStrong,
	Integer passwordLength,
	Integer passwordValidityDuration,
	Boolean passwordUnique,
	Boolean eproEnabled,
	UUID eproProfileId,
	String clientName,
	String clientEmail,
	String protocolNo,
	String versionNumber,

	List<ProjectLanguageDTO> languages,
	List<ProjectRuleTagDTO> ruleTags
) {
}
