package ch.rodano.api.actor;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRightsDTO {
	@NotNull List<String> readProfilesIds = new ArrayList<>();
	@NotNull List<String> writeProfilesIds = new ArrayList<>();
	@NotNull List<String> readScopeModelIds = new ArrayList<>();
	@NotNull List<String> writeScopeModelIds = new ArrayList<>();
	@NotNull boolean canCreateUser;

	public List<String> getReadProfilesIds() {
		return readProfilesIds;
	}

	public void setReadProfilesIds(final List<String> readProfiles) {
		this.readProfilesIds = readProfiles;
	}

	public List<String> getWriteProfilesIds() {
		return writeProfilesIds;
	}

	public void setWriteProfilesIds(final List<String> writeProfilesIds) {
		this.writeProfilesIds = writeProfilesIds;
	}

	public List<String> getReadScopeModelIds() {
		return readScopeModelIds;
	}

	public void setReadScopeModelIds(final List<String> readScopeModelIds) {
		this.readScopeModelIds = readScopeModelIds;
	}

	public List<String> getWriteScopeModelIds() {
		return writeScopeModelIds;
	}

	public void setWriteScopeModelIds(final List<String> writeScopeModelIds) {
		this.writeScopeModelIds = writeScopeModelIds;
	}

	public boolean isCanCreateUser() {
		return canCreateUser;
	}

	public void setCanCreateUser(final boolean canCreateUser) {
		this.canCreateUser = canCreateUser;
	}
}
