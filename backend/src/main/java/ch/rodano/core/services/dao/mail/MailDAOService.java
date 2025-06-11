package ch.rodano.core.services.dao.mail;

import java.util.List;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailAttachment;
import ch.rodano.core.model.mail.MailSearch;
import ch.rodano.core.model.mail.MailStatus;

public interface MailDAOService {

	Mail getMailByPk(Long pk);

	List<Mail> getMailsByPks(List<Long> pks);

	/**
	 * Get mails by their status
	 *
	 * @param status The status
	 * @param limit  The limit of mails to retrieve
	 * @return The mails
	 */
	List<Mail> getMailsByStatus(MailStatus status, Integer limit);

	Mail saveMail(Mail mail, DatabaseActionContext context, String rationale);

	Mail saveMail(Mail mail, List<MailAttachment> mailAttachments, DatabaseActionContext context, String rationale);

	PagedResult<Mail> search(MailSearch search);
}
