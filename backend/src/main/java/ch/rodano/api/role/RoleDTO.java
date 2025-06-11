package ch.rodano.api.role;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.rodano.api.config.ProfileDTO;
import ch.rodano.api.scope.ScopeMiniDTO;
import ch.rodano.core.model.role.RoleStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleDTO extends RoleCreationDTO {
	@NotNull
	Long pk;

	Long userPk;
	Long robotPk;

	@NotNull
	ProfileDTO profile;

	@NotNull
	ScopeMiniDTO scope;

	@NotNull
	RoleStatus status;
	@NotNull
	boolean enabled;
	@NotNull
	boolean canEnable;
	@NotNull
	boolean canDisable;

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public Long getUserPk() {
		return userPk;
	}

	public void setUserPk(final Long userPk) {
		this.userPk = userPk;
	}

	public Long getRobotPk() {
		return robotPk;
	}

	public void setRobotPk(final Long robotPk) {
		this.robotPk = robotPk;
	}

	public ProfileDTO getProfile() {
		return profile;
	}

	public void setProfile(final ProfileDTO profile) {
		this.profile = profile;
	}

	public ScopeMiniDTO getScope() {
		return scope;
	}

	public void setScope(final ScopeMiniDTO scope) {
		this.scope = scope;
	}

	public RoleStatus getStatus() {
		return status;
	}

	public void setStatus(final RoleStatus status) {
		this.status = status;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isCanEnable() {
		return canEnable;
	}

	public void setCanEnable(final boolean canEnable) {
		this.canEnable = canEnable;
	}

	public boolean isCanDisable() {
		return canDisable;
	}

	public void setCanDisable(final boolean canDisable) {
		this.canDisable = canDisable;
	}
}
