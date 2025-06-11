package ch.rodano.api.config;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MenuActionDTO(
	@NotEmpty String id,
	Map<String, String> labels,
	@NotEmpty String page,
	@NotNull List<String> context,
	@NotNull Map<String, String> parameters,
	@Deprecated String section
) { }
