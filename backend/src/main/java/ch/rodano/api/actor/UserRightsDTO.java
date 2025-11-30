package ch.rodano.api.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRightsDTO {
	@NotNull
	List<UUID> readProfilesIds = new ArrayList<>();
	@NotNull
	List<UUID> writeProfilesIds = new ArrayList<>();
	@NotNull
	List<UUID> readScopeModelIds = new ArrayList<>();
	@NotNull
	List<UUID> writeScopeModelIds = new ArrayList<>();
	@NotNull
	boolean canCreateUser;

	public List<UUID> getReadProfilesIds() {
		return readProfilesIds;
	}

	public void setReadProfilesIds(final List<UUID> readProfiles) {
		this.readProfilesIds = readProfiles;
	}

	public List<UUID> getWriteProfilesIds() {
		return writeProfilesIds;
	}

	public void setWriteProfilesIds(final List<UUID> writeProfilesIds) {
		this.writeProfilesIds = writeProfilesIds;
	}

	public List<UUID> getReadScopeModelIds() {
		return readScopeModelIds;
	}

	public void setReadScopeModelIds(final List<UUID> readScopeModelIds) {
		this.readScopeModelIds = readScopeModelIds;
	}

	public List<UUID> getWriteScopeModelIds() {
		return writeScopeModelIds;
	}

	public void setWriteScopeModelIds(final List<UUID> writeScopeModelIds) {
		this.writeScopeModelIds = writeScopeModelIds;
	}

	public boolean isCanCreateUser() {
		return canCreateUser;
	}

	public void setCanCreateUser(final boolean canCreateUser) {
		this.canCreateUser = canCreateUser;
	}
}
