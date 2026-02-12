package ch.rodano.api.dataset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dataset restoration")
public record DatasetRestorationDTO(
	@Schema(description = "Rationale for restoration")
	@NotBlank
	String rationale,
	@Schema(description = "The dataset to restore")
	@NotNull
	DatasetUpdateDTO dataset
) {

}
