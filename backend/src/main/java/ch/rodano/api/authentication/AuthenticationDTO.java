package ch.rodano.api.authentication;

import jakarta.validation.constraints.NotNull;

public record AuthenticationDTO(@NotNull String token) {
	//TODO add tsKey when 2-step authentication is re-enabled;
}
