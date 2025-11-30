package ch.rodano.api.dto.widget;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SummaryColumnDTO(
	@NotNull UUID summaryColumnId,
	@NotEmpty String id,
	@NotEmpty Map<String, String> label,
	@NotNull boolean percent,
	@NotNull boolean total
) {
}
