package ch.rodano.core.model.role;

import java.util.UUID;

public class RoleRecord {

	protected UUID projectId;
	protected UUID profileId;
	protected RoleStatus status;
	protected Long userFk;
	protected Long robotFk;
	protected Long scopeFk;

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public void setProfileId(final UUID profileId) {
		this.profileId = profileId;
	}

	public RoleStatus getStatus() {
		return status;
	}

	public void setStatus(final RoleStatus status) {
		this.status = status;
	}

	public Long getUserFk() {
		return userFk;
	}

	public void setUserFk(final Long userFk) {
		this.userFk = userFk;
	}

	public Long getRobotFk() {
		return robotFk;
	}

	public void setRobotFk(final Long robotFk) {
		this.robotFk = robotFk;
	}

	public Long getScopeFk() {
		return scopeFk;
	}

	public void setScopeFk(final Long scopeFk) {
		this.scopeFk = scopeFk;
	}
}
