package ch.rodano.api.actor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.role.RoleCreationDTO;

@Schema(description = "Create a user")
public record UserCreationDTO(
	@Schema(description = "Name")
	@NotBlank String name,

	@Schema(description = "E-mail")
	@NotBlank @Email String email,

	@Schema(description = "Is the user externally managed")
	@NotNull boolean externallyManaged,

	@Schema(description = "Phone number")
	String phone,

	@Schema(description = "Country ID")
	String countryId,

	@Schema(description = "Language ID")
	@NotBlank String languageId,

	@Schema(description = "Role of the new user")
	@NotNull RoleCreationDTO role
) { }
