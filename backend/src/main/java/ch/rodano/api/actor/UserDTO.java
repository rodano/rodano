package ch.rodano.api.actor;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO extends ActorDTO {

	@NotBlank
	@Email
	String email;
	@NotNull
	boolean externallyManaged;
	@NotNull
	boolean activated;
	String pendingEmail;
	ZonedDateTime newEmailExpirationDate;
	String phone;

	@NotNull
	boolean canWrite;

	String countryId;
	@NotBlank
	String languageId;
	String userAgent;
	ZonedDateTime loginDate;

	@NotNull
	boolean hasPassword;
	public ZonedDateTime passwordChangedDate;

	@NotNull
	boolean isAdmin;
	@NotNull
	UserRightsDTO rights;

	@NotNull
	boolean blocked;

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(final boolean activated) {
		this.activated = activated;
	}

	public boolean isExternallyManaged() {
		return externallyManaged;
	}

	public void setExternallyManaged(final boolean externallyManaged) {
		this.externallyManaged = externallyManaged;
	}

	public String getPendingEmail() {
		return pendingEmail;
	}

	public void setPendingEmail(final String pendingEmail) {
		this.pendingEmail = pendingEmail;
	}

	public ZonedDateTime getNewEmailExpirationDate() {
		return newEmailExpirationDate;
	}

	public void setNewEmailExpirationDate(final ZonedDateTime newEmailExpirationDate) {
		this.newEmailExpirationDate = newEmailExpirationDate;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(final String phone) {
		this.phone = phone;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(final boolean canBeEdited) {
		this.canWrite = canBeEdited;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(final String countryId) {
		this.countryId = countryId;
	}

	public String getLanguageId() {
		return languageId;
	}

	public void setLanguageId(final String languageId) {
		this.languageId = languageId;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(final String userAgent) {
		this.userAgent = userAgent;
	}

	public ZonedDateTime getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(final ZonedDateTime loginDate) {
		this.loginDate = loginDate;
	}

	public boolean isHasPassword() {
		return hasPassword;
	}

	public void setHasPassword(final boolean hasPassword) {
		this.hasPassword = hasPassword;
	}

	public ZonedDateTime getPasswordChangedDate() {
		return passwordChangedDate;
	}

	public void setPasswordChangedDate(final ZonedDateTime passwordChangedDate) {
		this.passwordChangedDate = passwordChangedDate;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(final boolean admin) {
		isAdmin = admin;
	}

	public final boolean isBlocked() {
		return blocked;
	}

	public final void setBlocked(final boolean blocked) {
		this.blocked = blocked;
	}

	public UserRightsDTO getRights() {
		return rights;
	}

	public void setRights(final UserRightsDTO rights) {
		this.rights = rights;
	}
}
