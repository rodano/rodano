package ch.rodano.api.scope;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.core.model.scope.Scope;

public record ScopeCandidateDTO(
	@NotNull Long parentScopePk,
	@NotBlank String code,
	@NotBlank String shortname,
	@NotBlank UUID modelId,
	@NotNull ZonedDateTime startDate,
	ZonedDateTime stopDate
) {
	public ScopeCandidateDTO(final Long parentScopePk, final Scope scope) {
		this(
			parentScopePk,
			scope.getCode(),
			scope.getShortname(),
			scope.getScopeModelId(),
			scope.getStartDate(),
			scope.getStopDate()
		);
	}
}
