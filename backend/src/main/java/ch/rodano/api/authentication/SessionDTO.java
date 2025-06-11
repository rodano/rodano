package ch.rodano.api.authentication;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.core.model.session.Session;
import ch.rodano.core.model.user.User;

public class SessionDTO {
	@NotNull private Long pk;
	@NotNull private String token;
	@NotNull private ZonedDateTime lastAccessTime;
	@NotNull private ZonedDateTime connectedSince;

	@NotNull private Long userPk;
	@NotBlank private String name;
	@NotNull private String userAgent;

	/**
	 * Constructor
	 *
	 * @param session The session
	 * @param user    The user who uses this session
	 */
	public SessionDTO(final Session session, final User user) {
		pk = session.getPk();
		token = session.getToken();
		lastAccessTime = session.getLastAccessTime();
		connectedSince = session.getCreationTime();

		userPk = user.getPk();
		name = user.getName();
		userAgent = user.getUserAgent();
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public String getToken() {
		return token;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public ZonedDateTime getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(final ZonedDateTime lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public Long getUserPk() {
		return userPk;
	}

	public void setUserPk(final Long userPk) {
		this.userPk = userPk;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(final String userAgent) {
		this.userAgent = userAgent;
	}

	public ZonedDateTime getConnectedSince() {
		return connectedSince;
	}

	public void setConnectedSince(final ZonedDateTime connectedSince) {
		this.connectedSince = connectedSince;
	}
}
