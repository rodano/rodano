package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.configuration.model.reports.Report;

@Schema(description = "Defines a report that is presented to users")
public record ReportDTO(
	@Schema(description = "Report UUID")
	@NotNull
	UUID reportId,
	@Schema(description = "Report ID")
	@NotBlank
	String id,
	@NotNull
	SortedMap<String, String> shortname,
	SortedMap<String, String> longname,
	SortedMap<String, String> description,

	@NotNull
	UUID workflowId,
	@NotNull
	UUID datasetModelId,

	@Schema(description = "Field models affected by the report")
	@NotEmpty
	List<UUID> fieldModelIds
) {
	public ReportDTO(final Report report) {
		this(
			report.getReportId(),
			report.getId(),
			report.getShortname(),
			report.getLongname(),
			report.getDescription(),
			report.getWorkflowUuid(),
			report.getDatasetModelUuid(),
			report.getFieldModelUuids()
		);
	}
}
