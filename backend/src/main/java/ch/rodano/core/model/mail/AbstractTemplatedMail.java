package ch.rodano.core.model.mail;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

import ch.rodano.core.model.common.IdentifiableObject;

public class AbstractTemplatedMail implements IdentifiableObject, TimestampedMail {

	protected Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	protected Integer attempts;
	protected MailStatus status;
	protected MailOrigin origin;
	protected String intent;
	protected String sender;
	protected Set<String> recipients;
	protected String replyTo;

	protected Map<String, Object> templateParameters;

	public AbstractTemplatedMail(final Map<String, Object> templateParameters) {
		attempts = 0;
		status = MailStatus.PENDING;
		this.templateParameters = templateParameters;
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

	public Map<String, Object> getTemplateParameters() {
		return templateParameters;
	}

	public void setTemplateParameters(final Map<String, Object> templateParameters) {
		this.templateParameters = templateParameters;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(", templateParameters=").append(templateParameters);
		return sb.toString();
	}
}
