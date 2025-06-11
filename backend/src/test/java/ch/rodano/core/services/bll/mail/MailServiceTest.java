package ch.rodano.core.services.bll.mail;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.model.mail.CustomizedTemplatedMail;
import ch.rodano.core.model.mail.DefinedTemplatedMail;
import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailOrigin;
import ch.rodano.core.model.mail.MailStatus;
import ch.rodano.core.model.mail.MailTemplate;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.dao.mail.MailDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class MailServiceTest extends DatabaseTest {
	@Autowired
	private MailService mailService;

	@Autowired
	private MailDAOService mailDAOService;

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Test
	@DisplayName("Mail creation works")
	public void assertMailCreated() {
		final var newMail = generateMockMail();
		mailService.createMail(newMail, context, "Test");

		assertNotNull(mailDAOService.getMailByPk(newMail.getPk()));
	}

	@Test
	@DisplayName("Mail creation from a defined template works")
	public void assertDefinedTemplatedMailCreated() {
		final var user = userService.getUserByPk(1L);
		final var roles = roleService.getActiveRoles(user);
		final var templatedMail = new DefinedTemplatedMail(
			MailTemplate.ROLE_ENABLE_CONFIRMATION, Map.ofEntries(
				Map.entry("study", studyService.getStudy()),
				Map.entry("test", 4),
				Map.entry("user", user),
				Map.entry("role", roles.get(0))
			)
		);
		final var mailStub = generateMockMail();
		templatedMail.setSender(mailStub.getSender());
		templatedMail.setRecipients(mailStub.getRecipients());
		templatedMail.setOrigin(mailStub.getOrigin());
		templatedMail.setIntent(mailStub.getIntent());

		final var createdMail = mailService.createMail(templatedMail, context, "Test");

		assertNotNull(mailDAOService.getMailByPk(createdMail.getPk()));
		assertNotNull(createdMail);
		assertTrue(createdMail.getHtmlBody().contains(studyService.getStudy().getDefaultLocalizedLongname()));
	}

	@Test
	@DisplayName("Mail creation using a customized template works")
	public void assertCustomizedTemplatedMailCreated() {
		final var templateParameter = "Tasty McTest";
		final var templatedMail = new CustomizedTemplatedMail("${test}", "${test}", "${test}", Map.of("test", templateParameter));
		final var mailStub = generateMockMail();
		templatedMail.setSender(mailStub.getSender());
		templatedMail.setRecipients(mailStub.getRecipients());
		templatedMail.setOrigin(mailStub.getOrigin());
		templatedMail.setIntent(mailStub.getIntent());

		final var createdMail = mailService.createMail(templatedMail, context, "Test");

		assertNotNull(mailDAOService.getMailByPk(createdMail.getPk()));
		assertNotNull(createdMail);
		assertEquals(templateParameter, createdMail.getSubject());
	}

	@Test
	@DisplayName("Already sent mails can be marked for re-sending")
	public void assertMailCanBeResentAsPending() {
		final var mailStub = generateMockMail();
		mailService.createMail(mailStub, context, "Test");

		assertEquals(MailStatus.PENDING, mailDAOService.getMailByPk(mailStub.getPk()).getStatus());

		mailStub.setStatus(MailStatus.SENT);
		mailService.saveMail(mailStub, context, "Test");

		assertEquals(MailStatus.SENT, mailDAOService.getMailByPk(mailStub.getPk()).getStatus());

		mailService.markMailsToBeSent(Collections.singletonList(mailStub.getPk()), context);

		assertEquals(MailStatus.PENDING, mailDAOService.getMailByPk(mailStub.getPk()).getStatus());
	}

	private Mail generateMockMail() {
		final var mailStub = new Mail();
		mailStub.setSender("spock@enterprise.com");
		mailStub.setRecipients(Collections.singleton("marf@enterprise.com"));
		mailStub.setSubject("About Interferometron");
		mailStub.setTextBody("Live long and prosper (tonight).");
		mailStub.setHtmlBody("<p>Live long and prosper (tonight).</p>");
		mailStub.setOrigin(MailOrigin.SYSTEM);
		mailStub.setIntent("WishLuck");
		return mailStub;
	}
}
