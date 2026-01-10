package ch.rodano.api.configurator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LanguageRequest(
	@NotBlank String languageCode,
	@NotNull Boolean isDefault
) {
}
