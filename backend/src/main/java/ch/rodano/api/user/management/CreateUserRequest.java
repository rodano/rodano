package ch.rodano.api.user.management;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
	@NotBlank String name,
	@Email @NotBlank String email,
	boolean isSuperuser,
	String languageId,
	boolean sendActivationEmail,
	String phone
) {
}
