package ch.rodano.api.administration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bootstrap the database")
public record BootstrapDTO(
	@Schema(description = "Name of the root scope") @NotBlank String rootScopeName,
	@Schema(description = "E-mail of the user") @NotBlank @Email String userEmail,
	@Schema(description = "Password of the user") @NotBlank String userPassword,
	@Schema(description = "Name of the user") @NotBlank String userName
) {
}
