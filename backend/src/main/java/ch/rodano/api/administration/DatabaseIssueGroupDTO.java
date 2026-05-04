package ch.rodano.api.administration;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.services.bll.database.DatabaseIssueEntity;
import ch.rodano.core.services.bll.database.DatabaseIssueGroup;
import ch.rodano.core.services.bll.database.DatabaseIssueStatus;
import ch.rodano.core.services.bll.database.DatabaseIssueType;

@Schema(description = "An issue detected during a database consistency check")
public record DatabaseIssueGroupDTO(
	@Schema(description = "Entity type: SCOPE, EVENT or DATASET") @NotNull DatabaseIssueEntity entity,
	@Schema(description = "Model id of the affected entity") @NotBlank String modelId,
	@Schema(description = "Primary keys of the affected entities") @NotEmpty List<Long> pks,
	@Schema(description = "Number of affected entities") int count,
	@Schema(description = "Type of inconsistency: MISSING_IN_DATABASE or MISSING_IN_CONFIGURATION") @NotNull DatabaseIssueType type,
	@Schema(description = "Id of the entity missing in the database or in the configuration") String missingEntityId,
	@Schema(description = "FIXABLE: issue detected but not fixed (dry run); FIXED: issue has been fixed; NOT_FIXABLE: issue cannot be automatically fixed") @NotNull DatabaseIssueStatus status
) {
	public DatabaseIssueGroupDTO(final DatabaseIssueGroup issueGroup) {
		this(issueGroup.entity(), issueGroup.modelId(), issueGroup.pks(), issueGroup.pks().size(), issueGroup.type(), issueGroup.missingEntityId(), issueGroup.status());
	}
}
