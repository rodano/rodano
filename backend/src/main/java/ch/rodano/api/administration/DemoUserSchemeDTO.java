package ch.rodano.api.administration;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Scheme for demo users")
public record DemoUserSchemeDTO(
	@Schema(description = "Base e-mail") @NotBlank String baseEmail,
	@Schema(description = "Password of the users") @NotBlank String password
) {
}
