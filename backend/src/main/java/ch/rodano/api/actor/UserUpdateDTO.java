package ch.rodano.api.actor;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Update a user")
public record UserUpdateDTO(
	@Schema(description = "Name") @NotBlank String name,
	@Schema(description = "Language ID") String languageId,
	@Schema(description = "Phone number") String phone
) {
}
