package ch.rodano.api.scope;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.core.model.scope.Scope;

public record ScopeTinyDTO(
	@NotNull
	Long pk,
	@NotNull
	UUID modelId,
	@NotBlank
	String code,
	@NotBlank
	String shortname,
	@NotBlank
	String longname
) {

	public ScopeTinyDTO(final Scope scope) {
		this(
			scope.getPk(),
			scope.getScopeModelId(),
			scope.getCode(),
			scope.getShortname(),
			scope.getLongname()
		);
	}
}
