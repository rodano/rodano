package ch.rodano.api.mail;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class MailSubmissionDTO {
	@NotEmpty private Set<String> recipients;
	@NotBlank private String subject;
	@NotBlank private String textBody;

	public MailSubmissionDTO() {
	}

	public MailSubmissionDTO(final Set<String> recipients, final String subject, final String textBody) {
		this.recipients = recipients;
		this.subject = subject;
		this.textBody = textBody;
	}

	public Set<String> getRecipients() {
		return recipients;
	}

	public void setRecipients(final Set<String> recipients) {
		this.recipients = recipients;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public String getTextBody() {
		return textBody;
	}

	public void setTextBody(final String textBody) {
		this.textBody = textBody;
	}
}
