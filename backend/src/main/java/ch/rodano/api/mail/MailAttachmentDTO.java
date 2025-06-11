package ch.rodano.api.mail;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;

import ch.rodano.core.model.mail.MailAttachment;

public class MailAttachmentDTO {
	@NotNull
	private Long pk;
	@NotNull
	private ZonedDateTime date;
	@NotNull
	private String filename;

	public MailAttachmentDTO() {}

	public MailAttachmentDTO(final MailAttachment attachment) {
		pk = attachment.getPk();
		date = attachment.getCreationTime();
		filename = attachment.getFilename();
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(final ZonedDateTime date) {
		this.date = date;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}
}
