package ch.rodano.api.epro;

import jakarta.validation.constraints.NotNull;

public record EproRobotDTO(
	@NotNull long scopePk,
	@NotNull String name,
	@NotNull String key
) { }
