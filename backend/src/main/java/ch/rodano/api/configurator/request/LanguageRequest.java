package ch.rodano.api.configurator.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LanguageRequest(
	@NotBlank String languageCode,
	@NotNull Boolean isDefault
) {
}
