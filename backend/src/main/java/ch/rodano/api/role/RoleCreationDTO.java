package ch.rodano.api.role;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class RoleCreationDTO {
	@NotNull
	Long scopePk;
	@NotNull
	UUID profileId;

	public Long getScopePk() {
		return scopePk;
	}

	public void setScopePk(final Long scopePk) {
		this.scopePk = scopePk;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public void setProfileId(final UUID profileId) {
		this.profileId = profileId;
	}
}
