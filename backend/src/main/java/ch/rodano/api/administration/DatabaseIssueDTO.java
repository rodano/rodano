package ch.rodano.api.administration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.services.bll.database.DatabaseIssue;
import ch.rodano.core.services.bll.database.DatabaseIssueEntity;
import ch.rodano.core.services.bll.database.DatabaseIssueStatus;
import ch.rodano.core.services.bll.database.DatabaseIssueType;

@Schema(description = "An issue detected during a database consistency check")
public record DatabaseIssueDTO(
	@Schema(description = "Entity type: SCOPE, EVENT or DATASET") @NotNull DatabaseIssueEntity entity,
	@Schema(description = "Model id of the affected entity") @NotBlank String modelId,
	@Schema(description = "Primary key of the affected entity") @NotNull Long pk,
	@Schema(description = "Type of inconsistency: MISSING_IN_DATABASE or MISSING_IN_CONFIGURATION") @NotNull DatabaseIssueType type,
	@Schema(description = "Description of the issue") @NotBlank String error,
	@Schema(description = "FIXABLE: issue detected but not fixed (dry run); FIXED: issue has been fixed; NOT_FIXABLE: issue cannot be automatically fixed") @NotNull DatabaseIssueStatus status
) {
	public DatabaseIssueDTO(final DatabaseIssue issue) {
		this(issue.entity(), issue.modelId(), issue.pk(), issue.type(), issue.error(), issue.status());
	}
}
