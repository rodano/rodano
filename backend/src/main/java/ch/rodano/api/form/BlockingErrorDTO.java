package ch.rodano.api.form;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A blocking error in the CRF")
public record BlockingErrorDTO(
	@Schema(description = "Dataset ID")
	@NotBlank String datasetId,
	@Schema(description = "Field model ID")
	@NotNull UUID fieldModelId,
	@Schema(description = "Error message")
	@NotBlank String message
) { }
