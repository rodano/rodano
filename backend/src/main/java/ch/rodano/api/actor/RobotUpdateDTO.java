package ch.rodano.api.actor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RobotUpdateDTO(
	@NotBlank String name, @NotNull String key
) {
}
