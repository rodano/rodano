package ch.rodano.api.dataset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
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
