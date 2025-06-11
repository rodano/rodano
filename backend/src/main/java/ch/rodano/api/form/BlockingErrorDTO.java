package ch.rodano.api.form;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A blocking error in the CRF")
public record BlockingErrorDTO(
	@Schema(description = "Dataset ID")
	@NotBlank String datasetId,
	@Schema(description = "Field model ID")
	@NotBlank String fieldModelId,
	@Schema(description = "Error message")
	@NotBlank String message
) { }
