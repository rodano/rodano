package ch.rodano.api.dto.widget.overdue;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record OverdueDTO(
	@NotNull UUID parentScopeModelId,
	@NotNull Long parentScopePk,
	@NotNull String parentScopeCode,
	@NotNull UUID scopeModelId,
	@NotNull Long scopePk,
	@NotNull String scopeCode,
	@NotNull Long eventPk,
	@NotNull ZonedDateTime lastDate,
	@NotNull Long daysOverdue
) { }
