package ch.rodano.core.model.mail;

public enum MailTemplate {
	SCOPE_TRANSFER(
		"${study.defaultLocalizedLongname} patient transfer for patient ${scope.code} ${scope.shortname}",
		"scope_transferred.txt",
		"scope_transferred.html"
	),
	USER_ACTIVATION_INVITATION(
		"${study.defaultLocalizedLongname} account activation",
		"user_activation_invitation.txt",
		"user_activation_invitation.html"
	),
	USER_ACTIVATION_CONFIRMATION(
		"${study.defaultLocalizedLongname} account activation confirmation",
		"user_activation_confirmation.txt",
		"user_activation_confirmation.html"
	),
	EMAIL_CHANGE(
		"Your e-mail address has been changed",
		"email_change.txt",
		"email_change.html"
	),
	EMAIL_VERIFICATION(
		"Please verify your email",
		"email_verification.txt",
		"email_verification.html"
	),
	ACCOUNT_LOCKED(
		"Account locked due to suspicious activity",
		"account_locked.txt",
		"account_locked.html"
	),
	PASSWORD_RESET(
		"Password reset",
		"password_reset.txt",
		"password_reset.html"
	),
	ROLE_CREATION_CONFIRMATION(
		"${study.defaultLocalizedLongname} role creation",
		"role_creation_confirmation.txt",
		"role_creation_confirmation.html"
	),
	ROLE_ENABLE_CONFIRMATION(
		"${study.defaultLocalizedLongname} role activation",
		"role_enable_confirmation.txt",
		"role_enable_confirmation.html"
	),
	ROLE_ENABLE_CONFIRMATION_FOR_PRIVILEGED_USERS(
		"${study.defaultLocalizedLongname} new role activation",
		"role_enable_privileged_users_notification.txt",
		"role_enable_privileged_users_notification.txt"
	),
	VALIDATE_REGISTRATION_FIRST(
		"${study.defaultLocalizedLongname} account activation",
		"validate_registration_first.txt",
		"validate_registration_first.html"
	),
	EXTERNAL_USER_CANNOT_RECOVER_PASSWORD(
		"${study.defaultLocalizedLongname} password reset",
		"external_user_cannot_recover_password.txt",
		"external_user_cannot_recover_password.html"
	),
	DOCUMENT_PUBLISHED(
		"${study.defaultLocalizedLongname}, new document published",
		"document_published.txt",
		"document_published.html"
	);

	private final String subject;
	private final String bodyTextFilename;
	private final String bodyHTMLFilename;

	/**
	 * Constructor
	 *
	 * @param subject          The subject
	 * @param bodyTextFilename The body text filename
	 * @param bodyHTMLFilename The body HTML filename
	 */
	MailTemplate(final String subject, final String bodyTextFilename, final String bodyHTMLFilename) {
		this.subject = subject;
		this.bodyTextFilename = bodyTextFilename;
		this.bodyHTMLFilename = bodyHTMLFilename;
	}

	public String getSubject() {
		return subject;
	}

	public String getBodyTextFilename() {
		return bodyTextFilename;
	}

	public String getBodyHTMLFilename() {
		return bodyHTMLFilename;
	}
}
