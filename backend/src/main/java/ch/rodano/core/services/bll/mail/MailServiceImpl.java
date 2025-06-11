package ch.rodano.core.services.bll.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.ui.freemarker.SpringTemplateLoader;

import com.opencsv.CSVWriter;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import ch.rodano.api.controller.user.exception.InvalidEmailException;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.mail.CustomizedTemplatedMail;
import ch.rodano.core.model.mail.DefinedTemplatedMail;
import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailOrigin;
import ch.rodano.core.model.mail.MailSearch;
import ch.rodano.core.model.mail.MailStatus;
import ch.rodano.core.model.mail.MailTemplate;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.ScopeExtension;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.user.UserSearch;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.mail.MailAttachmentDAOService;
import ch.rodano.core.services.dao.mail.MailDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.utils.UtilsService;

@Service
public class MailServiceImpl implements MailService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final JavaMailSender sender;
	private final ResourceLoader resourceLoader;
	private final Configuration freemarkerConfiguration;
	private final StudyService studyService;
	private final MailDAOService mailDAOService;
	private final MailAttachmentDAOService mailAttachmentDAOService;
	private final ScopeDAOService scopeDAOService;
	private final UserDAOService userDAOService;

	public MailServiceImpl(
		final JavaMailSender sender,
		final ResourceLoader resourceLoader,
		final Configuration freemarkerConfiguration,
		final StudyService studyService,
		final MailDAOService mailDAOService,
		final MailAttachmentDAOService mailAttachmentDAOService,
		final ScopeDAOService scopeDAOService,
		final UserDAOService userDAOService
	) {
		this.sender = sender;
		this.resourceLoader = resourceLoader;
		this.freemarkerConfiguration = freemarkerConfiguration;
		this.studyService = studyService;
		this.mailDAOService = mailDAOService;
		this.mailAttachmentDAOService = mailAttachmentDAOService;
		this.scopeDAOService = scopeDAOService;
		this.userDAOService = userDAOService;

		freemarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	@Override
	public void checkEmailAddress(final String email) {
		//check that e-mail does not contains any space
		//unfortunately the Java validator does not check white spaces
		if(email.contains(" ")) {
			throw new InvalidEmailException("Spaces are not allowed in e-mails");
		}
		//use the validator provided by Java
		try {
			new InternetAddress(email, true).validate();
		}
		catch(final AddressException e) {
			throw new InvalidEmailException(e.getLocalizedMessage());
		}
	}

	@Override
	public void markMailsToBeSent(final List<Long> mailsPks, final DatabaseActionContext context) {
		final var mails = mailDAOService.getMailsByPks(mailsPks);
		mails.forEach(mail -> {
			mail.setStatus(MailStatus.PENDING);
			mailDAOService.saveMail(mail, context, "Mark mail to be send or resend");
		});
	}

	@Override
	public Mail createMail(final Set<String> recipients, final String subject, final String body, final DatabaseActionContext context, final String rationale) {
		final var mail = new Mail();
		mail.setRecipients(recipients);
		mail.setOrigin(MailOrigin.USER);
		mail.setIntent("Test");
		mail.setSubject(subject);
		mail.setTextBody(body);

		return createMail(mail, context, rationale);
	}

	@Override
	public Mail createMail(final CustomizedTemplatedMail customizedTemplatedMail, final DatabaseActionContext context, final String rationale) {
		// prepare loader
		final var loader = new StringTemplateLoader();
		loader.putTemplate("subject", customizedTemplatedMail.getSubject());
		loader.putTemplate("text", StringUtils.defaultString(customizedTemplatedMail.getTextBody()));
		loader.putTemplate("html", StringUtils.defaultString(customizedTemplatedMail.getHtmlBody()));

		final var templateParameters = customizedTemplatedMail.getTemplateParameters();

		final var mail = new Mail(customizedTemplatedMail);
		mail.setSubject(readTemplate("subject", templateParameters, loader));
		mail.setTextBody(readTemplate("text", templateParameters, loader));
		mail.setHtmlBody(readTemplate("html", templateParameters, loader));

		return createMail(mail, context, rationale);
	}

	@Override
	public Mail createMail(final DefinedTemplatedMail definedTemplatedMail, final DatabaseActionContext context, final String rationale) {
		final var mail = new Mail(definedTemplatedMail);

		final var template = definedTemplatedMail.getTemplate();
		final var templateParameters = definedTemplatedMail.getTemplateParameters();

		//prepare loader
		final var stl = new SpringTemplateLoader(resourceLoader, "classpath:emails/");
		final var sl = new StringTemplateLoader();
		sl.putTemplate("subject", template.getSubject());

		final var loader = new MultiTemplateLoader(new TemplateLoader[] { stl, sl });

		// Set the subject
		// Create a specific template loader as the subject is a string and not a folder
		// Put the custom template into the loader

		// Process the template
		mail.setSubject(readTemplate("subject", templateParameters, loader));
		// Set the mail body
		mail.setTextBody(readTemplate(template.getBodyTextFilename(), templateParameters, loader));
		mail.setHtmlBody(readTemplate(template.getBodyHTMLFilename(), templateParameters, loader));

		return createMail(mail, context, rationale);
	}

	@Override
	public Mail createMail(final Mail mail, final DatabaseActionContext context, final String rationale) {
		mail.setStatus(MailStatus.PENDING);

		// The sender and replyTo are always defined by the study configuration
		final var study = studyService.getStudy();
		mail.setSender(study.getEmail());
		mail.setReplyTo(study.getEmail());

		return mailDAOService.saveMail(mail, context, rationale);
	}

	@Override
	public Mail sendMail(final Mail mail, final DatabaseActionContext context, final boolean simulate) {
		if(mail.getRecipients().isEmpty()) {
			logger.warn("Discarding e-mail [pk={}, suject={}] without recipient", mail.getPk(), mail.getSubject());
			mail.setStatus(MailStatus.CANCELED);
			mail.setError("E-mail has no recipients");
			saveMail(mail, context, "Mail cancelled");
			return mail;
		}

		mail.increaseAttempts();
		mail.setError(null);
		mail.setSentTime(null);

		if(simulate) {
			logger.info("Simulate sending e-mail [pk={}, recipients={}, subject={}]", mail.getPk(), mail.getRecipients(), mail.getSubject());
			mail.setStatus(MailStatus.SIMULATED);
			mail.setSentTime(ZonedDateTime.now());
		}
		else {
			try {
				final var mailMessage = createMimeMessage(mail);
				sender.send(mailMessage);
				mail.setStatus(MailStatus.SENT);
				mail.setSentTime(ZonedDateTime.now());
				logger.info("Sent e-mail [pk={}, recipients={}, subject={}]", mail.getPk(), mail.getRecipients(), mail.getSubject());
			}
			catch(MailException | MessagingException e) {
				e.printStackTrace();
				logger.error("Fail to send e-mail [pk={}, recipients={}, subject={}]", mail.getPk(), mail.getRecipients(), mail.getSubject());
				mail.setStatus(MailStatus.FAILED);
				mail.setError(e.getLocalizedMessage());
			}
		}
		saveMail(mail, context, "Mail sent by the sender task");
		return mail;
	}

	/**
	 * Create a mail message from a mail
	 * A mail message is a ready-to-be-sent mail
	 *
	 * @param mail The mail
	 * @return A mail message
	 * @throws MessagingException Thrown if an error occurred while creating the message
	 */
	@Override
	public MimeMessage createMimeMessage(final Mail mail) throws MessagingException {
		final var message = sender.createMimeMessage();

		final var helper = new MimeMessageHelper(message, true);

		helper.setFrom(mail.getSender());
		helper.setReplyTo(mail.getReplyTo());

		//if email has only one recipient, send the e-mail to him directly
		if(mail.getRecipients().size() == 1) {
			helper.setTo(mail.getRecipients().iterator().next());
		}
		//if email has more than one recipient, send the e-mail to the study mailbox and add recipients in BCC (in order not to disclose recipients emails)
		else {
			helper.setTo(studyService.getStudy().getEmail());
			helper.setBcc(mail.getRecipients().toArray(new String[0]));
		}

		helper.setSubject(mail.getSubject());
		if(StringUtils.isNotBlank(mail.getHtmlBody())) {
			helper.setText(mail.getTextBody(), mail.getHtmlBody());
		}
		else {
			helper.setText(mail.getTextBody());
		}

		for(final var attachment : mailAttachmentDAOService.getMailAttachmentByMailPk(mail.getPk())) {
			helper.addAttachment(attachment.getFilename(), new InputStreamResource(new ByteArrayInputStream(attachment.getContent())));
		}

		return message;
	}

	/**
	 * Read a template and render it to a string
	 *
	 * @param name       The template name
	 * @param parameters The template parameters
	 * @param loader     The template loader which will be set and unset automatically
	 * @return The rendered template with the given parameters
	 */
	private String readTemplate(final String name, final Map<String, Object> parameters, final TemplateLoader loader) {
		// Set the template loader
		freemarkerConfiguration.setTemplateLoader(loader);

		try {
			return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(name), parameters);
		}
		catch(IOException | TemplateException e) {
			throw new MailPreparationException(e);
		}
		finally {
			// Unset the template loader
			freemarkerConfiguration.unsetTemplateLoader();
		}
	}

	@Override
	public Mail saveMail(final Mail mail, final DatabaseActionContext context, final String rationale) {
		return mailDAOService.saveMail(mail, context, rationale);
	}

	@Override
	public PagedResult<Mail> search(final MailSearch search) {
		return mailDAOService.search(search);
	}

	@Override
	public Mail getMailByPk(final Long pk) {
		return mailDAOService.getMailByPk(pk);
	}

	@Override
	public List<Mail> getMails(final MailStatus status, final Integer limit) {
		return mailDAOService.getMailsByStatus(status, limit);
	}

	@Override
	public void sendUserAccountActivationInvitation(
		final User recipient,
		final Role role,
		final String contextURL,
		final DatabaseActionContext context
	) {
		final var url = String.format("%s/register/%s", contextURL, recipient.getActivationCode());

		final Map<String, Object> vars = Map.of(
			"user_activation_url", url,
			"role", role
		);

		final var mail = prepareMail(
			recipient,
			MailTemplate.USER_ACTIVATION_INVITATION,
			vars,
			Collections.singleton(recipient.getEmail()),
			"Send user activation e-mail"
		);

		createMail(mail, context, "Send user activation invitation e-mail");
	}

	@Override
	public void sendUserAccountActivationConfirmation(
		final User recipient,
		final DatabaseActionContext context
	) {
		final var mail = prepareMail(
			recipient,
			MailTemplate.USER_ACTIVATION_CONFIRMATION,
			Collections.emptyMap(),
			Collections.singleton(recipient.getEmail()),
			"Send user activation confirmation e-mail"
		);

		createMail(mail, context, "Send user activation confirmation e-mail");
	}

	@Override
	public void sendUserEmailVerificationEmail(final User recipient, final int pendingEmailExpiryLimit, final String contextURL, final DatabaseActionContext context) {
		final var url = String.format("%s/email-verification/%s", contextURL, recipient.getEmailVerificationCode());

		final Map<String, Object> templateVars = Map.ofEntries(
			Map.entry("verification_url", url),
			Map.entry("time_limit_in_days", pendingEmailExpiryLimit)
		);

		final var mail = prepareMail(
			recipient,
			MailTemplate.EMAIL_VERIFICATION,
			templateVars,
			Collections.singleton(recipient.getPendingEmail()),
			"Verify user e-mail"
		);

		createMail(mail, context, "Verify user e-mail");
	}

	@Override
	public void sendPasswordResetEmail(final User recipient, final int expiryTimeInMinutes, final String contextURL, final DatabaseActionContext context) {
		final var url = String.format("%s/change-password/%s", contextURL, recipient.getPasswordResetCode());

		final Map<String, Object> templateVars = Map.ofEntries(
			Map.entry("password_reset_url", url),
			Map.entry("expiry_limit_in_minutes", expiryTimeInMinutes)
		);

		final var mail = prepareMail(
			recipient,
			MailTemplate.PASSWORD_RESET,
			templateVars,
			Collections.singleton(recipient.getEmail()),
			"Send password reset instructions"
		);

		createMail(mail, context, "Send password reset instructions");
	}

	@Override
	public void sendAccountLockedEmail(final User recipient, final String contextURL, final DatabaseActionContext context) {
		final var url = String.format("%s/recover-account/%s", contextURL, recipient.getRecoveryCode());

		final Map<String, Object> templateVars = Map.ofEntries(
			Map.entry("recovery_url", url)
		);

		final var mail = prepareMail(
			recipient,
			MailTemplate.ACCOUNT_LOCKED,
			templateVars,
			Collections.singleton(recipient.getEmail()),
			"Send account unlocking instructions"
		);

		createMail(mail, context, "Send account recovery instructions");
	}

	@Override
	public void sendExternalUserCanNotRecoverPasswordNotification(
		final User recipient,
		final DatabaseActionContext context
	) {
		final var mail = prepareMail(
			recipient,
			MailTemplate.EXTERNAL_USER_CANNOT_RECOVER_PASSWORD,
			Collections.emptyMap(),
			Collections.singleton(recipient.getEmail()),
			"Externally managed user cannot recover their password"
		);

		createMail(mail, context, "Externally managed user cannot recover their password");
	}

	@Override
	public void sendEmailChangeNotification(
		final User recipient,
		final User actor,
		final String newEmail,
		final int pendingEmailExpiryLimit,
		final DatabaseActionContext context
	) {
		final Map<String, Object> templateVars = Map.ofEntries(
			Map.entry("actor_email", actor.getEmail()),
			Map.entry("new_email", newEmail),
			Map.entry("time_limit_in_days", pendingEmailExpiryLimit)
		);

		final var mail = prepareMail(
			recipient,
			MailTemplate.EMAIL_CHANGE,
			templateVars,
			Collections.singleton(recipient.getEmail()),
			"Your e-mail has been changed"
		);

		createMail(mail, context, "User e-mail has been changed");
	}

	@Override
	public void sendRoleCreationConfirmation(
		final User recipient,
		final Role role,
		final DatabaseActionContext context
	) {
		final Map<String, Object> templateVars = Map.of("role", role);

		final var mail = prepareMail(
			recipient,
			MailTemplate.ROLE_CREATION_CONFIRMATION,
			templateVars,
			Collections.singleton(recipient.getEmail()),
			"Confirm role creation"
		);

		createMail(mail, context, "Confirm role creation");
	}

	@Override
	public void sendRoleEnableConfirmation(
		final User recipient,
		final Role role,
		final DatabaseActionContext context
	) {
		final Map<String, Object> templateVars = Map.of("role", role);

		final var mail = prepareMail(
			recipient,
			MailTemplate.ROLE_ENABLE_CONFIRMATION,
			templateVars,
			Collections.singleton(recipient.getEmail()),
			"Confirm role enable"
		);

		createMail(mail, context, "Confirm role enable");
	}

	@Override
	public void sendRoleEnableNotificationToPrivilegedUser(
		final User recipient,
		final User actor,
		final Role role,
		final DatabaseActionContext context
	) {
		final var todayDate = ZonedDateTime.now().format(UtilsService.HUMAN_READABLE_DATE);

		final Map<String, Object> templateVars = Map.of(
			"role", role,
			"currentUser", actor,
			"date", todayDate
		);

		final var mail = prepareMail(
			recipient,
			MailTemplate.ROLE_ENABLE_CONFIRMATION_FOR_PRIVILEGED_USERS,
			templateVars,
			Collections.singleton(recipient.getEmail()),
			"Notify privileged users of new role activation"
		);

		createMail(mail, context, "Notify privileged users of new role activation");
	}

	@Override
	public void sendResourcePublicationNotification(
		final User user,
		final Resource resource,
		final DatabaseActionContext context
	) {
		final var study = studyService.getStudy();
		final var scope = scopeDAOService.getScopeByPk(resource.getScopeFk());

		final var featureId = FeatureStatic.NOTIFY_RESOURCE_PUBLISHED.getId();
		final var predicate = new UserSearch()
			.enforceScopePks(Collections.singleton(scope.getPk()))
			.enforceExtension(ScopeExtension.ANCESTORS)
			.enforceFeatureId(featureId);
		final var users = userDAOService.search(predicate).getObjects();
		final var recipients = users.stream().map(User::getEmail).filter(StringUtils::isNotBlank).collect(Collectors.toSet());

		if(!recipients.isEmpty()) {
			final var mail = new DefinedTemplatedMail(
				MailTemplate.DOCUMENT_PUBLISHED, Map.ofEntries(
					Map.entry("study", study),
					Map.entry("user", user),
					Map.entry("resource", resource),
					Map.entry("resource_category_name", study.getResourceCategory(resource.getCategoryId()).getDefaultLocalizedShortname()),
					Map.entry("scope", scope)
				)
			);

			mail.setSender(study.getEmail());
			mail.setReplyTo(study.getEmail());
			mail.setRecipients(recipients);
			mail.setOrigin(MailOrigin.SYSTEM);
			mail.setIntent("Notify users of resource file update");

			createMail(mail, context, "Notify users of resource file publication");
		}
	}

	/**
	 * Auxiliary method used to construct an e-mail from a template
	 * @param recipientUser The recipient user
	 * @param template      The e-mail template
	 * @param templateVars  The e-mail template variables
	 * @param recipients    The e-mail addresses of the recipients
	 * @param intent        The e-mail intent
	 * @return              Filled-out e-mail template
	 */
	private DefinedTemplatedMail prepareMail(
		final User recipientUser,
		final MailTemplate template,
		final Map<String, Object> templateVars,
		final Set<String> recipients,
		final String intent
	) {
		final var study = studyService.getStudy();

		final Map<String, Object> baseVars = Map.ofEntries(
			Map.entry("study", study),
			Map.entry("user", recipientUser)
		);

		final var finalTemplateVars = Stream.concat(baseVars.entrySet().stream(), templateVars.entrySet().stream())
			.collect(
				Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue,
					(_, v2) -> v2
				)
			);

		final var mail = new DefinedTemplatedMail(template, finalTemplateVars);
		mail.setSender(study.getEmail());
		mail.setReplyTo(study.getEmail());
		mail.setRecipients(recipients);
		mail.setOrigin(MailOrigin.SYSTEM);
		mail.setIntent(intent);

		return mail;
	}

	@Override
	public void exportMails(final OutputStream out, final List<Mail> mails) throws IOException {
		try(var writer = new CSVWriter(new OutputStreamWriter(out))) {

			final var header = new String[] {
				"Id",
				"Date",
				"Sender",
				"Recipients",
				"Status",
				"Attempt",
				"Origin",
				"Subject",
				"Body",
			};
			writer.writeNext(header);

			for(final var mail : mails) {
				final var line = new String[header.length];
				var i = 0;
				line[i++] = mail.getPk().toString();
				line[i++] = mail.getLastUpdateTime().format(UtilsService.HUMAN_READABLE_DATE_TIME);
				line[i++] = mail.getSender();

				var recipients = mail.getRecipients().toString();
				recipients = recipients.substring(1, recipients.length() - 1);
				line[i++] = recipients;

				line[i++] = mail.getStatus().getStatus();
				line[i++] = Integer.toString(mail.getAttempts());
				line[i++] = mail.getOrigin().getOrigin();

				line[i++] = mail.getSubject();
				line[i++] = mail.getTextBody();
				writer.writeNext(line);
			}
		}
	}
}
