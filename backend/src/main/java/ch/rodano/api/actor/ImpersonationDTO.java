package ch.rodano.api.actor;

import jakarta.validation.constraints.NotBlank;

public record ImpersonationDTO(@NotBlank String profileId) {

}
