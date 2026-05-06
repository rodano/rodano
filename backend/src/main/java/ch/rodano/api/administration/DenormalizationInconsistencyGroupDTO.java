package ch.rodano.api.administration;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.services.bll.database.DenormalizationInconsistencyGroup;
import ch.rodano.core.services.bll.database.InconsistencyStatus;
import ch.rodano.core.services.bll.database.InconsistentEntity;

@Schema(description = "A group of column inconsistencies detected during a database consistency check")
public record DenormalizationInconsistencyGroupDTO(
	@Schema(description = "Entity type") @NotNull InconsistentEntity entity,
	@Schema(description = "Model id of the affected entity") @NotBlank String modelId,
	@Schema(description = "Primary keys of the affected records") @NotEmpty List<Long> pks,
	@Schema(description = "Number of affected records") int count,
	@Schema(description = "FIXABLE: issue detected but not fixed (dry run); FIXED: issue has been fixed") @NotNull InconsistencyStatus status
) {
	public DenormalizationInconsistencyGroupDTO(final DenormalizationInconsistencyGroup issueGroup) {
		this(issueGroup.entity(), issueGroup.modelId(), issueGroup.pks(), issueGroup.pks().size(), issueGroup.status());
	}
}
