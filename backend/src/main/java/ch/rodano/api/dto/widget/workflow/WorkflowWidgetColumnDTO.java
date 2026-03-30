package ch.rodano.api.dto.widget.workflow;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.configuration.model.reports.WorkflowWidgetColumn;
import ch.rodano.configuration.model.reports.WorkflowWidgetColumnType;

@Schema(description = "Workflow widget column configuration")
public record WorkflowWidgetColumnDTO(
	@Schema(description = "Column label") @NotNull Map<String, String> label,
	@Schema(description = "Type of column") @NotNull WorkflowWidgetColumnType type
) {
	public WorkflowWidgetColumnDTO(final WorkflowWidgetColumn workflowWidgetColumn) {
		this(workflowWidgetColumn.getLabel(), workflowWidgetColumn.getType());
	}
}
