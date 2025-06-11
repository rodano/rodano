package ch.rodano.api.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A container object for blocking errors")
public record BlockingErrorsDTO(
	@Schema(description = "A global error message")
	@NotBlank String message,
	@Schema(description = "A list of blocking errors")
	@NotEmpty List<BlockingErrorDTO> errors
) { }
