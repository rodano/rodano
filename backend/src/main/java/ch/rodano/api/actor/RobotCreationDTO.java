package ch.rodano.api.actor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.api.role.RoleCreationDTO;

public record RobotCreationDTO(
	@NotBlank String name,
	@NotNull RoleCreationDTO role,
	String key
) { }
