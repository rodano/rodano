package ch.rodano.core.services.bll.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailAttachment;
import ch.rodano.core.model.mail.MailOrigin;
import ch.rodano.core.services.dao.mail.MailAttachmentDAOService;
import ch.rodano.core.services.dao.mail.MailDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringTestConfiguration
@Transactional
public class MailDAOServiceTest extends DatabaseTest {
	@Autowired
	private MailDAOService mailDAOService;

	@Autowired
	private MailAttachmentDAOService mailAttachmentDAOService;

	private static Mail mailStub;
	private static MailAttachment mailAtt;

	@BeforeAll
	public void prepare() throws IOException {
		// Same mail stub is used across test methods, so we only build it once
		mailStub = new Mail();
		mailStub.setSender("spok@enterprise.com");
		mailStub.setRecipients(Collections.singleton("marf@enterprise.com"));
		mailStub.setSubject("About Interferometron");
		mailStub.setTextBody("Live long and prosper (tonight).");
		mailStub.setHtmlBody("<p>Live long and prosper (tonight).</p>");
		mailStub.setOrigin(MailOrigin.SYSTEM);
		mailStub.setIntent("WishLuck");

		mailAtt = new MailAttachment();
		mailAtt.setFilename("attachment.txt");
		try(var is = MailDAOServiceTest.class.getResourceAsStream("/services/mail/attachment.txt")) {
			mailAtt.setContent(is.readAllBytes());
		}
	}

	@Test
	@DisplayName("Can save mail with attachments")
	public void canSavetMailWithAttachment() {
		final List<MailAttachment> attachments = new ArrayList<>();
		attachments.add(mailAtt);

		mailDAOService.saveMail(mailStub, attachments, context, TEST_RATIONALE);

		final var mail = mailDAOService.getMailByPk(mailStub.getPk());
		final var attFromDB = mailAttachmentDAOService.getMailAttachmentByMailPk(mailStub.getPk());

		assertAll(
			"Check the attachment",
			() -> assertEquals(mailStub.getPk(), mail.getPk()),
			() -> assertEquals(mailStub.getSubject(), mail.getSubject()),
			() -> assertEquals(attachments.size(), attFromDB.size())
		);
	}
}
