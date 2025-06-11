package ch.rodano.core.services.bll.mail;

import org.springframework.stereotype.Service;

import ch.rodano.core.model.mail.MailAttachment;
import ch.rodano.core.services.dao.mail.MailAttachmentDAOService;

@Service
public class MailAttachmentServiceImpl implements MailAttachmentService {
	private final MailAttachmentDAOService mailAttachmentDAOService;

	public MailAttachmentServiceImpl(final MailAttachmentDAOService mailAttachmentDAOService) {
		this.mailAttachmentDAOService = mailAttachmentDAOService;
	}

	/**
	 * Get a mail attachment by its pk
	 *
	 * @param pk The mail attachment pk
	 * @return A mail attachment
	 */
	@Override
	public MailAttachment getMailAttachmentByPk(final Long pk) {
		return mailAttachmentDAOService.getMailAttachmentByPk(pk);
	}
}
