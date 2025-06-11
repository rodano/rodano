package ch.rodano.api.actor.activation;

import jakarta.validation.constraints.NotNull;

public record UserActivationDTO(
	@NotNull Boolean acceptPolicies,
	String password
) {}
