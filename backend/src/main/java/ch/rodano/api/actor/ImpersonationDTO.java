package ch.rodano.api.actor;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ImpersonationDTO(@NotNull UUID profileId) {

}
