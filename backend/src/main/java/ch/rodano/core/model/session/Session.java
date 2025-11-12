package ch.rodano.core.model.session;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Session {

	protected UUID projectId;
	protected Long pk;
	protected ZonedDateTime creationTime;
	private ZonedDateTime lastAccessTime;
	private Long userFk;
	private String token;

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

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

	public String getToken() {
		return token;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public Long getUserFk() {
		return userFk;
	}

	public void setUserFk(final Long userFk) {
		this.userFk = userFk;
	}

	public ZonedDateTime getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(final ZonedDateTime lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
}
