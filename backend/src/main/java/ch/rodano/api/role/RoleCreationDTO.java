package ch.rodano.api.role;

import jakarta.validation.constraints.NotNull;

public class RoleCreationDTO {
	@NotNull Long scopePk;
	@NotNull String profileId;

	public Long getScopePk() {
		return scopePk;
	}

	public void setScopePk(final Long scopePk) {
		this.scopePk = scopePk;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(final String profileId) {
		this.profileId = profileId;
	}
}
