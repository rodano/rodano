package ch.rodano.core.services.bll.session;

import java.util.List;

import ch.rodano.core.model.session.Session;
import ch.rodano.core.model.user.User;

public interface SessionService {
	int SESSION_TOKEN_BYTE_LENGTH = 24;
	int SESSION_TOKEN_STRING_LENGTH = 32;

	/**
	 * Create or update a session
	 *
	 * @param user    The user to create the session for
	 * @return The saved session
	 */
	Session createSession(User user);

	/**
	 * Update a session
	 *
	 * @param session The session to update
	 * @return The updated session
	 */
	Session refreshSession(Session session);

	/**
	 * Delete a session by its token
	 *
	 * @param session The session token to delete
	 */
	void deleteSession(Session session);

	/**
	 * Get a session by its token
	 *
	 * @param token The token
	 * @return The session with the given token
	 */
	Session getSessionByToken(String token);

	/**
	 * Get a session by its pk
	 *
	 * @param pk The pk
	 * @return The session with the given pk
	 */
	Session getSessionByPk(Long pk);

	/**
	 * Delete old sessions
	 *
	 * @param ageInMinutes The maximum age of the session in minutes
	 */
	void deleteOldSessions(Integer ageInMinutes);

	/**
	 * Get all sessions
	 *
	 * @return A list of session
	 */
	List<Session> getSessions();
}
