package ch.rodano.api.administration;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of the database consistency check")
public record ConsistencyCheckResultDTO(
	@Schema(description = "Whether the database is consistent with the configuration") boolean consistent,
	@Schema(description = "List of detected issues; empty when consistent is true") @NotNull List<DatabaseIssueDTO> issues
) {

	public ConsistencyCheckResultDTO(final List<DatabaseIssueDTO> issues) {
		this(issues.isEmpty(), issues);
	}
}
