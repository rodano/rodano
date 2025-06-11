package ch.rodano.core.model.mail;

import java.time.ZonedDateTime;
import java.util.Set;

import ch.rodano.core.model.common.IdentifiableObject;

public class Mail implements IdentifiableObject, TimestampedMail {

	private Long pk;
	private ZonedDateTime creationTime;
	private ZonedDateTime lastUpdateTime;

	private int attempts;
	private MailStatus status;
	private String error;
	private ZonedDateTime sentTime;
	private MailOrigin origin;
	private String intent;
	private String sender;
	private Set<String> recipients;
	private String replyTo;

	private String subject;
	private String textBody;
	private String htmlBody;

	public Mail() {
		status = MailStatus.PENDING;
	}

	public Mail(final AbstractTemplatedMail mail) {
		this();
		attempts = mail.getAttempts();
		status = mail.getStatus();
		origin = mail.getOrigin();
		intent = mail.getIntent();
		sender = mail.getSender();
		replyTo = mail.getReplyTo();
		setRecipients(mail.getRecipients());
	}

	@Override
	public Long getPk() {
		return pk;
	}

	@Override
	public void setPk(final Long pk) {
		this.pk = pk;
	}

	@Override
	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	@Override
	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	@Override
	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	@Override
	public int getAttempts() {
		return attempts;
	}

	@Override
	public void setAttempts(final int attempts) {
		this.attempts = attempts;
	}

	@Override
	public MailStatus getStatus() {
		return status;
	}

	@Override
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

	@Override
	public MailOrigin getOrigin() {
		return origin;
	}

	@Override
	public void setOrigin(final MailOrigin origin) {
		this.origin = origin;
	}

	@Override
	public String getIntent() {
		return intent;
	}

	@Override
	public void setIntent(final String intent) {
		this.intent = intent;
	}

	@Override
	public String getSender() {
		return sender;
	}

	@Override
	public void setSender(final String sender) {
		this.sender = sender;
	}

	@Override
	public Set<String> getRecipients() {
		return recipients;
	}

	@Override
	public void setRecipients(final Set<String> recipients) {
		this.recipients = recipients;
	}

	@Override
	public String getReplyTo() {
		return replyTo;
	}

	@Override
	public void setReplyTo(final String replyTo) {
		this.replyTo = replyTo;
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

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(final String htmlBody) {
		this.htmlBody = htmlBody;
	}
}
