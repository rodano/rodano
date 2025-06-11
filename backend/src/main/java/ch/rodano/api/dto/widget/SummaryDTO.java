package ch.rodano.api.dto.widget;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotEmpty;

public record SummaryDTO(
	@NotEmpty String id,
	@NotEmpty Map<String, String> title,
	@NotEmpty String leafScopeModelId,
	@NotEmpty List<SummaryColumnDTO> columns,
	@NotEmpty List<SummaryRowDTO> rows
) {
}
