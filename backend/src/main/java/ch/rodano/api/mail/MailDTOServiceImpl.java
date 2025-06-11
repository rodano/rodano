package ch.rodano.api.mail;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailAttachment;
import ch.rodano.core.services.dao.mail.MailAttachmentDAOService;

@Service
public class MailDTOServiceImpl implements MailDTOService {

	private final MailAttachmentDAOService mailAttachmentDAOService;

	public MailDTOServiceImpl(final MailAttachmentDAOService mailAttachmentDAOService) {
		this.mailAttachmentDAOService = mailAttachmentDAOService;
	}

	@Override
	public List<MailDTO> createDTOs(final List<Mail> mails) {
		final var mailPks = mails.stream().map(Mail::getPk).toList();
		final var attachments = mailAttachmentDAOService.getMailAttachmentByMailPks(mailPks).stream().collect(Collectors.groupingBy(MailAttachment::getMailFk));
		return mails.stream()
			.map(m -> createDTO(m, attachments.getOrDefault(m.getPk(), Collections.emptyList())))
			.toList();
	}

	@Override
	public MailDTO createDTO(final Mail mail) {
		final var attachments = mailAttachmentDAOService.getMailAttachmentByMailPk(mail.getPk());
		return createDTO(mail, attachments);
	}

	private MailDTO createDTO(final Mail mail, final List<MailAttachment> attachments) {
		final var attachmentDTOs = attachments.stream().map(MailAttachmentDTO::new).toList();

		return new MailDTO(
			mail.getRecipients(),
			mail.getSubject(),
			mail.getTextBody(),
			mail.getPk(),
			mail.getCreationTime(),
			mail.getLastUpdateTime(),
			mail.getOrigin(),
			mail.getStatus(),
			mail.getError(),
			mail.getSentTime(),
			mail.getIntent(),
			mail.getSender(),
			mail.getHtmlBody(),
			mail.getAttempts(),
			mail.getReplyTo(),
			attachmentDTOs
		);
	}

}
