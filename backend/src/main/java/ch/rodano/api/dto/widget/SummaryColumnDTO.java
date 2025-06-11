package ch.rodano.api.dto.widget;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SummaryColumnDTO(
	@NotEmpty String id,
	@NotEmpty Map<String, String> label,
	@NotNull boolean percent,
	@NotNull boolean total
) {
}
