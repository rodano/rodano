package ch.rodano.core.model.mail;

import java.time.ZonedDateTime;

import ch.rodano.core.model.common.IdentifiableObject;
import ch.rodano.core.model.common.TimestampableObject;

public class MailAttachment implements IdentifiableObject, TimestampableObject {

	private Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	protected Long mailFk;
	protected String filename;
	protected byte[] content;

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

	public Long getMailFk() {
		return mailFk;
	}

	public void setMailFk(final Long mailFk) {
		this.mailFk = mailFk;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(final byte[] content) {
		this.content = content;
	}
}
