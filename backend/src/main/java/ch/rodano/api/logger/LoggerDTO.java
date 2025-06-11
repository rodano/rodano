package ch.rodano.api.logger;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.logging.LogLevel;

public record LoggerDTO(
	@NotBlank String log,
	@NotNull LogLevel level
) { }
