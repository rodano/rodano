package ch.rodano.core.services.dao.mail;

import java.util.List;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.mail.MailAttachment;

public interface MailAttachmentDAOService {

	/**
	 * Get a mail attachment by its pk
	 *
	 * @param pk The mail attachment pk
	 * @return A mail attachment
	 */
	MailAttachment getMailAttachmentByPk(Long pk);

	/**
	 * Get all attachments from a mail pk
	 *
	 * @param mailPk The mail pk
	 * @return Mail attachments
	 */
	List<MailAttachment> getMailAttachmentByMailPk(Long mailPk);

	/**
	 * Get all attachments from a list of mail pk
	 *
	 * @param mailPks The mail pks
	 * @return Mail attachments
	 */
	List<MailAttachment> getMailAttachmentByMailPks(List<Long> mailPks);

	/**
	 * Create or update a mail attachment
	 *
	 * @param mailAttachment The mail attachment
	 * @param context        The context
	 * @param rationale      The rationale for the operation
	 */
	void saveMailAttachment(MailAttachment mailAttachment, DatabaseActionContext context, String rationale);

}
