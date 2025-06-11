package ch.rodano.api.mail;

import java.util.List;

import ch.rodano.core.model.mail.Mail;

public interface MailDTOService {

	MailDTO createDTO(Mail mail);

	List<MailDTO> createDTOs(List<Mail> mails);

}
