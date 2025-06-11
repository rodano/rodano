package ch.rodano.api.mail;

import jakarta.validation.constraints.NotBlank;

public record MailCreationDTO(
	@NotBlank String recipient, @NotBlank String subject, @NotBlank String body
) {
}
