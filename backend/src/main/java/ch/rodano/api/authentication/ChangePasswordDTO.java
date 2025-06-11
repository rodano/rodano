package ch.rodano.api.authentication;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record ChangePasswordDTO(
	@Schema(description = "Current password")
	@NotBlank String currentPassword,
	@Schema(description = "New password")
	@NotBlank String newPassword
) { }
