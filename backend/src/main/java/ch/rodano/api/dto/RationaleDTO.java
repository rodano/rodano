package ch.rodano.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RationaleDTO(@NotBlank String message) {

}
