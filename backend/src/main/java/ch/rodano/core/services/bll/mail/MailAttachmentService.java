package ch.rodano.core.services.bll.mail;

import ch.rodano.core.model.mail.MailAttachment;

public interface MailAttachmentService {
	/**
	 * Get a mail attachment by its pk
	 *
	 * @param pk The mail attachment pk
	 * @return A mail attachment
	 */
	MailAttachment getMailAttachmentByPk(Long pk);
}
