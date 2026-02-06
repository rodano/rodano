package ch.rodano.api.configurator.request;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.rodano.api.configurator.dto.ProjectLanguageDTO;
import ch.rodano.api.configurator.dto.ProjectRuleTagDTO;

public record UpdateProjectRequest(
	String code,
	Map<String, String> shortname,
	Map<String, String> longname,
	Map<String, String> description,

	String url,
	String color,
	String introductionText,
	LocalDate versionDate,

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
