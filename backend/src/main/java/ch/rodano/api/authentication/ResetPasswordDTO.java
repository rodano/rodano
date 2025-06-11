package ch.rodano.api.authentication;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record ResetPasswordDTO(
	@Schema(description = "Reset code")
	@NotBlank String resetCode,
	@Schema(description = "New password")
	@NotBlank String newPassword
) { }
