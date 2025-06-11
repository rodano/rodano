package ch.rodano.core.services.bll.mail;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.mail.CustomizedTemplatedMail;
import ch.rodano.core.model.mail.DefinedTemplatedMail;
import ch.rodano.core.model.mail.Mail;
import ch.rodano.core.model.mail.MailSearch;
import ch.rodano.core.model.mail.MailStatus;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.user.User;

public interface MailService {

	/**
	 * Check if a mail address appear to be valid
	 * This only checks the syntax of the address, and is by no means a guarantee that e-mails sent to this address will be received
	 * @param email The mail to check
	 */
	void checkEmailAddress(String email);

	/**
	 * Mark mails to be sent or resent
	 *
	 * @param mailsPks The mails pks
	 */
	void markMailsToBeSent(List<Long> mailsPks, DatabaseActionContext context);

	/**
	 * Create a simple mail
	 *
	 * @param recipients The recipients
	 * @param subject The subject
	 * @param body The body
	 * @return The mail to send
	 */
	Mail createMail(Set<String> recipients, String subject, String body, DatabaseActionContext context, String rationale);

	/**
	 * Create a mail
	 *
	 * @param customizedTemplatedMail The templated mail
	 * @param context                 The context
	 * @return The mail to send
	 */
	Mail createMail(CustomizedTemplatedMail customizedTemplatedMail, DatabaseActionContext context, String rationale);

	/**
	 * Create a mail
	 *
	 * @param definedTemplatedMail The templated mail
	 * @param context              The context
	 * @return The mail to send
	 */
	Mail createMail(DefinedTemplatedMail definedTemplatedMail, DatabaseActionContext context, String rationale);

	/**
	 * Create a mail
	 *
	 * @param mail    The mail
	 * @param context The context
	 * @return The mail to send
	 */
	Mail createMail(Mail mail, DatabaseActionContext context, String rationale);

	/**
	 * Build and send an email
	 *
	 * @param mail The mail
	 * @param context The context
	 * @param simulate If true, the e-mail won't actually be sent
	 */
	Mail sendMail(Mail mail, DatabaseActionContext context, boolean simulate);

	MimeMessage createMimeMessage(Mail mail) throws MessagingException;

	/**
	 * Update a mail
	 *
	 * @param mail    The mail
	 * @param context The context
	 * @return The mail to send
	 */
	Mail saveMail(Mail mail, DatabaseActionContext context, String rationale);

	/**
	 * Get all valid mails
	 *
	 * @param search The predicate
	 * @return The mails corresponding to the given predicate
	 */
	PagedResult<Mail> search(MailSearch search);

	/**
	 * Get a mail by its pk
	 *
	 * @param pk The mail pk
	 * @return A mail
	 */
	Mail getMailByPk(Long pk);

	/**
	 * Get mails by their status
	 *
	 * @param status The status
	 * @param limit  The limit of mail to retrieve
	 * @return The mails
	 */
	List<Mail> getMails(MailStatus status, Integer limit);

	void sendUserAccountActivationInvitation(
		User recipient,
		Role role,
		String contextURL,
		DatabaseActionContext context
	);

	void sendUserAccountActivationConfirmation(
		User recipient,
		DatabaseActionContext context
	);

	/**
	 * Send an e-mail verification e-mail to the user (sent to his pending e-mail address)
	 * @param user                      The recipient user
	 * @param pendingEmailExpiryLimit   The pending e-mail expiration time limit (in days)
	 * @param contextURL                The context URL (used to formulate the e-mail activation e-mail)
	 * @param context The context
	 */
	void sendUserEmailVerificationEmail(User user, int pendingEmailExpiryLimit, String contextURL, DatabaseActionContext context);

	/**
	 * Send a password reset e-mail to the user (sent to his e-mail address)
	 * @param recipient                 The recipient user
	 * @param expiryTimeInMinutes
	 * @param contextURL                The context URL (used to formulate the e-mail activation e-mail)
	 * @param context The context
	 */
	void sendPasswordResetEmail(User recipient, int expiryTimeInMinutes, String contextURL, DatabaseActionContext context);

	/**
	 * 	Send an account locked e-mail to the user (sent to his e-mail address)
	 * @param recipient
	 * @param contextURL
	 * @param context
	 */
	void sendAccountLockedEmail(User recipient, String contextURL, DatabaseActionContext context);

	void sendExternalUserCanNotRecoverPasswordNotification(
		User recipient,
		DatabaseActionContext context
	);

	/**
	 * Send an e-mail to the user which notifies him of the e-mail change
	 * @param user                      The recipient user
	 * @param actor                     The actor of the change
	 * @param newEmail                  The new e-mail of the user
	 * @param pendingEmailExpiryLimit   The pending e-mail expiration time limit (in days)
	 * @param context The context
	 */
	void sendEmailChangeNotification(User user, User actor, String newEmail, int pendingEmailExpiryLimit, DatabaseActionContext context);

	void sendRoleCreationConfirmation(
		User recipient,
		Role role,
		DatabaseActionContext context
	);

	/**
	 * Send a confirmation when a user role is enabled
	 * @param user          The recipient user
	 * @param role          The role enabled
	 * @param context       The context
	 */
	void sendRoleEnableConfirmation(User user, Role role, DatabaseActionContext context);

	/**
	 * Send an e-mail notifying privileged users of a user role activation
	 * @param recipient     The recipient user
	 * @param user          The actor of the change
	 * @param role          The role enabled
	 * @param context The context
	 */
	void sendRoleEnableNotificationToPrivilegedUser(User recipient, User user, Role role, DatabaseActionContext context);

	/**
	 * Send mail notification to inform that a resource has been published
	 *
	 * @param user     The user executing the action
	 * @param resource The published resource
	 * @param context The context
	 */
	void sendResourcePublicationNotification(User user, Resource resource, DatabaseActionContext context);

	/**
	 * Export the given mails
	 *
	 * @param out   The output stream where the CSV will be written
	 * @param mails The mails
	 */
	void exportMails(OutputStream out, List<Mail> mails) throws IOException;
}
