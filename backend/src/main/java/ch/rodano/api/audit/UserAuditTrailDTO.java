package ch.rodano.api.audit;

import ch.rodano.core.model.audit.models.UserAuditTrail;

public final class UserAuditTrailDTO extends UserAuditTrail {

	public UserAuditTrailDTO(final UserAuditTrail userAuditTrail) {
		// copy all the user record properties
		this.setDeleted(userAuditTrail.getDeleted());
		this.name = userAuditTrail.getName();
		this.externallyManaged = userAuditTrail.isExternallyManaged();
		this.previousLoginDate = userAuditTrail.getPreviousLoginDate();
		this.email = userAuditTrail.getEmail();
		this.pendingEmail = userAuditTrail.getPendingEmail();
		this.emailModificationDate = userAuditTrail.getEmailModificationDate();
		this.emailVerificationCode = userAuditTrail.getEmailVerificationCode();
		this.loginBlockingDate = userAuditTrail.getLoginBlockingDate();
		this.loginDate = userAuditTrail.getLoginDate();
		this.logoutDate = userAuditTrail.getLogoutDate();
		this.passwordAttempts = userAuditTrail.getPasswordAttempts();
		this.passwordChangedDate = userAuditTrail.getPasswordChangedDate();
		this.recoveryCode = userAuditTrail.getRecoveryCode();
		this.shouldChangePassword = userAuditTrail.isShouldChangePassword();
		this.languageId = userAuditTrail.getLanguageId();
		this.countryId = userAuditTrail.getCountryId();
		this.phone = userAuditTrail.getPhone();

		// copy all the user audit trail properties
		this.setPk(userAuditTrail.getPk());
		this.setAuditObjectFk(userAuditTrail.getAuditObjectFk());
		this.setAuditActionFk(userAuditTrail.getAuditActionFk());
		this.setAuditActor(userAuditTrail.getAuditActor());
		this.setAuditUserFk(userAuditTrail.getAuditUserFk());
		this.setAuditRobotFk(userAuditTrail.getAuditRobotFk());
		this.setAuditDatetime(userAuditTrail.getAuditDatetime());
		this.setAuditContext(userAuditTrail.getAuditContext());

		// hide the sensitive user information
		this.password = "**********";
		this.previousPasswords = "**********";
		this.userAgent = "**********";
	}
}
