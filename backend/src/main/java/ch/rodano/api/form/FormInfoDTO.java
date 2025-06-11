package ch.rodano.api.form;

import jakarta.validation.constraints.NotBlank;

import ch.rodano.api.field.FieldDTO;

public record FormInfoDTO(
	@NotBlank FieldDTO field,
	@NotBlank Long scopePk,
	@NotBlank Long eventPk,
	@NotBlank Long formPk
) { }
