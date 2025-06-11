package ch.rodano.api.resource;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.api.config.ResourceCategoryDTO;

public class ResourceDTO extends ResourceSubmissionDTO {
	@NotNull Long pk;
	@NotNull ZonedDateTime creationTime;
	@NotNull ZonedDateTime lastUpdateTime;

	@NotNull ResourceCategoryDTO category;

	@NotNull boolean canBeManaged;

	@NotBlank String scopeShortname;

	@NotNull Long userPk;
	@NotBlank String userEmail;
	@NotBlank String userName;

	@NotNull Boolean removed;

	String filename;

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public ResourceCategoryDTO getCategory() {
		return category;
	}

	public void setCategory(final ResourceCategoryDTO category) {
		this.category = category;
	}

	public boolean isCanBeManaged() {
		return canBeManaged;
	}

	public void setCanBeManaged(final boolean canBeManaged) {
		this.canBeManaged = canBeManaged;
	}

	public String getScopeShortname() {
		return scopeShortname;
	}

	public void setScopeShortname(final String scopeShortname) {
		this.scopeShortname = scopeShortname;
	}

	public Long getUserPk() {
		return userPk;
	}

	public void setUserPk(final Long userPk) {
		this.userPk = userPk;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(final String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public Boolean getRemoved() {
		return removed;
	}

	public void setRemoved(final Boolean removed) {
		this.removed = removed;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}
}
