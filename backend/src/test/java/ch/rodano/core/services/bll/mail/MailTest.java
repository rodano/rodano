package ch.rodano.core.services.bll.mail;

import java.util.Collections;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailOrigin;
import ch.rodano.core.model.mail.MailStatus;
import ch.rodano.core.scheduler.task.mail.MailSenderTask;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.StatelessDatabaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringTestConfiguration
@Transactional
public class MailTest extends StatelessDatabaseTest {

	@RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
		.withConfiguration(GreenMailConfiguration.aConfig())
		.withPerMethodLifecycle(true);

	@Autowired
	private JavaMailSender sender;

	@Autowired
	private MailSenderTask mailSenderTask;

	@Autowired
	private MailService mailService;

	@Test
	@DisplayName("The Java mail sender works correctly")
	public void javaMailSenderWorks() throws MessagingException {
		final var mimeMail = mailService.createMimeMessage(createTestMail());
		sender.send(mimeMail);

		Assertions.assertTrue(greenMail.waitForIncomingEmail(10000, 1));

		final var receivedMessage = greenMail.getReceivedMessages()[0];
		assertEquals(1, receivedMessage.getAllRecipients().length);
		assertEquals("spok@enterprise.com", receivedMessage.getFrom()[0].toString());
	}

	@Disabled
	@Test
	@DisplayName("MailTaskSender works correctly")
	public void mailSenderTaskWorks() throws MessagingException {
		// Clear out all the pending e-mails
		final var pendingMails = mailService.getMails(MailStatus.PENDING, 10);
		for(final var pendingEmail : pendingMails) {
			pendingEmail.setStatus(MailStatus.CANCELED);
			mailService.saveMail(pendingEmail, context, "E-mail test");
		}

		// Create a new dummy mail
		final var mailStub = createTestMail();
		mailService.createMail(mailStub, context, "E-mail test");

		// Verify that only the newly created mail is present
		final var mails = mailService.getMails(MailStatus.PENDING, 10);
		assertEquals(1, mails.size());

		// Run the Mail sender
		mailSenderTask.run();

		// Assert that the mail is received correctly
		Assertions.assertTrue(greenMail.waitForIncomingEmail(5000, 1));

		final var receivedMessage = greenMail.getReceivedMessages()[0];
		assertEquals(1, receivedMessage.getAllRecipients().length);
		assertEquals(studyService.getStudy().getEmail(), receivedMessage.getFrom()[0].toString());
	}

	private Mail createTestMail() {
		final var mailStub = new Mail();
		mailStub.setReplyTo("info@rodano.ch");
		mailStub.setSender("spok@enterprise.com");
		mailStub.setRecipients(Collections.singleton("marf@enterprise.com"));
		mailStub.setSubject("About Interferometron");
		mailStub.setTextBody("Live long and prosper (tonight).");
		mailStub.setHtmlBody("<p>Live long and prosper (tonight).</p>");
		mailStub.setOrigin(MailOrigin.SYSTEM);
		mailStub.setIntent("WishLuck");
		return mailStub;
	}
}
