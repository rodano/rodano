package ch.rodano.api.epro;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.scope.ScopeMiniDTO;

public record EproScopeDTO(
	@NotNull ScopeMiniDTO scope,
	@Schema(description = "Has the scope been invited to use ePRO (an active robot exists)?") @NotNull boolean invited
) { }
