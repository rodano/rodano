package ch.rodano.api.actor;

import jakarta.validation.constraints.NotNull;

public record ChangeEmailDTO(
	@NotNull String email,
	@NotNull String password
) { }
