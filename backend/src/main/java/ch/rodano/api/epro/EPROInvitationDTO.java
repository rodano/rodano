package ch.rodano.api.epro;

import jakarta.validation.constraints.NotNull;

import ch.rodano.api.scope.ScopeDTO;

public record EPROInvitationDTO(
	@NotNull ScopeDTO scope,
	@NotNull String url,
	@NotNull String key
) { }
