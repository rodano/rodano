package ch.rodano.api.scope;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Used for scope relation creation and transfer")
public record ScopeRelationCreationDTO(

	@Schema(description = "The parent PK")
	@NotNull Long parentPk,
	@Schema(description = "Start date of the relation")
	@NotNull ZonedDateTime startDate,
	@Schema(description = "End date of the relation (optional)")
	ZonedDateTime endDate
) { }
