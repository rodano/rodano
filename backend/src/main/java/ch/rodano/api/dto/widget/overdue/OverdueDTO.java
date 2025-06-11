package ch.rodano.api.dto.widget.overdue;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;

public record OverdueDTO(
	@NotNull String parentScopeModelId,
	@NotNull Long parentScopePk,
	@NotNull String parentScopeCode,
	@NotNull String scopeModelId,
	@NotNull Long scopePk,
	@NotNull String scopeCode,
	@NotNull Long eventPk,
	@NotNull ZonedDateTime lastDate,
	@NotNull Long daysOverdue
) { }
