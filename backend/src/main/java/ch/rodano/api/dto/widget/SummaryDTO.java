package ch.rodano.api.dto.widget;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SummaryDTO(
	@NotNull UUID workflowSummaryId,
	@NotEmpty String id,
	@NotEmpty Map<String, String> title,
	@NotEmpty UUID leafScopeModelId,
	@NotEmpty List<SummaryColumnDTO> columns,
	@NotEmpty List<SummaryRowDTO> rows
) {
}
