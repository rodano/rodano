package ch.rodano.api.user.management;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
	@NotBlank String name,
	String languageId,
	boolean isSuperuser,
	String phone
) {
}
