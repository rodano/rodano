package ch.rodano.api.configurator;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateProjectRequest(
	@NotBlank String code,
	@NotNull Map<String, String> shortname,
	@NotNull Map<String, String> longname,
	Map<String, String> description,
	String url,
	String color,
	@NotEmpty List<LanguageRequest> languages
) {
}
