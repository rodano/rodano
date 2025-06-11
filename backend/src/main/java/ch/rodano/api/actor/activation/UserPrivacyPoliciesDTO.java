package ch.rodano.api.actor.activation;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import ch.rodano.api.config.PrivacyPolicyDTO;

public record UserPrivacyPoliciesDTO(
	@NotNull String email,
	@NotNull List<PrivacyPolicyDTO> policies
) {}
