package ch.rodano.api.mail;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.core.model.mail.MailOrigin;
import ch.rodano.core.model.mail.MailStatus;

public class MailDTO extends MailSubmissionDTO {
	@NotNull
	Long pk;

	@NotNull
	ZonedDateTime creationTime;
	@NotNull
	ZonedDateTime lastUpdateTime;

	MailOrigin origin;
	@NotNull
	MailStatus status;
	String error;
	ZonedDateTime sentTime;
	String intent;
	@NotBlank
	String sender;
	String htmlBody;
	@NotNull
	Integer attempts;
	String replyTo;
	@NotNull
	List<MailAttachmentDTO> attachments;

	public MailDTO() {}

	public MailDTO(
		final Set<String> recipients,
		final String subject,
		final String textBody,
		final Long pk,
		final ZonedDateTime creationTime,
		final ZonedDateTime lastUpdateTime,
		final MailOrigin origin,
		final MailStatus status,
		final String error,
		final ZonedDateTime sentTime,
		final String intent,
		final String sender,
		final String htmlBody,
		final Integer attempts,
		final String replyTo,
		final List<MailAttachmentDTO> attachments
	) {
		super(recipients, subject, textBody);
		this.pk = pk;
		this.creationTime = creationTime;
		this.lastUpdateTime = lastUpdateTime;
		this.origin = origin;
		this.status = status;
		this.error = error;
		this.sentTime = sentTime;
		this.intent = intent;
		this.sender = sender;
		this.htmlBody = htmlBody;
		this.attempts = attempts;
		this.replyTo = replyTo;
		this.attachments = attachments;
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public MailStatus getStatus() {
		return status;
	}

	public void setStatus(final MailStatus status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(final String error) {
		this.error = error;
	}

	public ZonedDateTime getSentTime() {
		return sentTime;
	}

	public void setSentTime(final ZonedDateTime sentTime) {
		this.sentTime = sentTime;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(final String sender) {
		this.sender = sender;
	}

	public MailOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(final MailOrigin origin) {
		this.origin = origin;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(final String intent) {
		this.intent = intent;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(final String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(final Integer attempts) {
		this.attempts = attempts;
	}

	public List<MailAttachmentDTO> getAttachments() {
		return attachments;
	}

	public void setAttachments(final List<MailAttachmentDTO> attachments) {
		this.attachments = attachments;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(final String replyTo) {
		this.replyTo = replyTo;
	}

}
