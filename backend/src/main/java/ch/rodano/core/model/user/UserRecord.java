package ch.rodano.core.model.user;

import java.time.ZonedDateTime;

public class UserRecord {

	private boolean deleted;

	protected String name;

	protected boolean externallyManaged;
	protected boolean activated;
	protected String activationCode;

	protected String email;
	protected String pendingEmail;
	protected ZonedDateTime emailModificationDate;
	protected String emailVerificationCode;

	protected String recoveryCode;

	protected ZonedDateTime previousLoginDate;
	protected ZonedDateTime loginBlockingDate;
	protected ZonedDateTime loginDate;
	protected ZonedDateTime logoutDate;

	protected String password;
	protected int passwordAttempts;
	protected ZonedDateTime passwordChangedDate;
	protected String previousPasswords;
	protected String passwordResetCode;
	protected ZonedDateTime passwordResetDate;
	protected boolean shouldChangePassword;

	protected String userAgent;
	protected String languageId;
	protected String countryId;
	protected String phone;

	// TODO 2FA
	//protected Byte[] data;

	protected UserRecord() {
		deleted = false;
		setPasswordAttempts(0);
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(final boolean deleted) {
		this.deleted = deleted;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public boolean isExternallyManaged() {
		return externallyManaged;
	}

	public void setExternallyManaged(final boolean externallyManaged) {
		this.externallyManaged = externallyManaged;
	}

	public ZonedDateTime getPreviousLoginDate() {
		return previousLoginDate;
	}

	public void setPreviousLoginDate(final ZonedDateTime previousLoginDate) {
		this.previousLoginDate = previousLoginDate;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(final boolean activated) {
		this.activated = activated;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(final String activationCode) {
		this.activationCode = activationCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getPendingEmail() {
		return pendingEmail;
	}

	public void setPendingEmail(final String pendingEmail) {
		this.pendingEmail = pendingEmail;
	}

	public ZonedDateTime getEmailModificationDate() {
		return emailModificationDate;
	}

	public void setEmailModificationDate(final ZonedDateTime emailModificationDate) {
		this.emailModificationDate = emailModificationDate;
	}

	public String getEmailVerificationCode() {
		return emailVerificationCode;
	}

	public void setEmailVerificationCode(final String emailVerificationCode) {
		this.emailVerificationCode = emailVerificationCode;
	}

	public ZonedDateTime getLoginBlockingDate() {
		return loginBlockingDate;
	}

	public void setLoginBlockingDate(final ZonedDateTime loginBlockingDate) {
		this.loginBlockingDate = loginBlockingDate;
	}

	public ZonedDateTime getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(final ZonedDateTime loginDate) {
		this.loginDate = loginDate;
	}

	public ZonedDateTime getLogoutDate() {
		return logoutDate;
	}

	public void setLogoutDate(final ZonedDateTime logoutDate) {
		this.logoutDate = logoutDate;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public int getPasswordAttempts() {
		return passwordAttempts;
	}

	public void setPasswordAttempts(final int passwordAttempts) {
		this.passwordAttempts = passwordAttempts;
	}

	public ZonedDateTime getPasswordChangedDate() {
		return passwordChangedDate;
	}

	public void setPasswordChangedDate(final ZonedDateTime passwordChangedDate) {
		this.passwordChangedDate = passwordChangedDate;
	}

	public String getPreviousPasswords() {
		return previousPasswords;
	}

	public void setPreviousPasswords(final String previousPasswords) {
		this.previousPasswords = previousPasswords;
	}

	public String getRecoveryCode() {
		return recoveryCode;
	}

	public void setRecoveryCode(final String recoveryCode) {
		this.recoveryCode = recoveryCode;
	}

	public String getPasswordResetCode() {
		return passwordResetCode;
	}

	public void setPasswordResetCode(final String passwordResetCode) {
		this.passwordResetCode = passwordResetCode;
	}

	public ZonedDateTime getPasswordResetDate() {
		return passwordResetDate;
	}

	public void setPasswordResetDate(final ZonedDateTime passwordResetDate) {
		this.passwordResetDate = passwordResetDate;
	}

	public boolean isShouldChangePassword() {
		return shouldChangePassword;
	}

	public void setShouldChangePassword(final boolean shouldChangePassword) {
		this.shouldChangePassword = shouldChangePassword;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(final String userAgent) {
		this.userAgent = userAgent;
	}

	public String getLanguageId() {
		return languageId;
	}

	public void setLanguageId(final String languageId) {
		this.languageId = languageId;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(final String countryId) {
		this.countryId = countryId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(final String phone) {
		this.phone = phone;
	}
}
