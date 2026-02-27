package ch.rodano.api.actor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.role.RoleCreationDTO;

@Schema(description = "Create a user")
public record UserCreationDTO(
	@Schema(description = "Name") @NotBlank String name,
	@Schema(description = "Mail") @NotBlank @Email String email,
	@Schema(description = "Phone number") String phone,
	@Schema(description = "Language ID") @NotBlank String languageId,
	@Schema(description = "Role of the new user") @NotNull RoleCreationDTO role
) {
}
