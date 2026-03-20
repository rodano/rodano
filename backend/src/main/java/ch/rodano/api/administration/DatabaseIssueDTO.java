package ch.rodano.api.administration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.services.bll.database.DatabaseIssueStatus;

@Schema(description = "An issue detected during a database consistency check")
public record DatabaseIssueDTO(
	@Schema(description = "Entity type: scope, event or dataset") @NotNull String entity,
	@Schema(description = "Primary key of the affected entity") @NotNull Long pk,
	@Schema(description = "Description of the issue") @NotBlank String error,
	@Schema(description = "FIXABLE: issue detected but not fixed (dry run); FIXED: issue has been fixed; NOT_FIXABLE: issue cannot be automatically fixed") @NotNull DatabaseIssueStatus status
) {
}
