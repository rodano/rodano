package ch.rodano.api.authentication;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.core.model.session.Session;
import ch.rodano.core.model.user.User;

public record SessionDTO(
	@NotNull Long pk,
	@NotNull String token,
	@NotNull ZonedDateTime lastAccessTime,
	@NotNull ZonedDateTime connectedSince,

	@NotNull Long userPk,
	@NotBlank String name,
	@NotNull String userAgent
) {

	/**
	 * Constructor
	 *
	 * @param session The session
	 * @param user    The user who uses this session
	 */
	public SessionDTO(final Session session, final User user) {
		this(
			session.getPk(),
			session.getToken(),
			session.getLastAccessTime(),
			session.getCreationTime(),
			user.getPk(),
			user.getName(),
			user.getUserAgent()
		);
	}
}
