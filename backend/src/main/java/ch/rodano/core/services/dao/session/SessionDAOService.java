package ch.rodano.core.services.dao.session;

import java.time.ZonedDateTime;
import java.util.List;

import ch.rodano.core.model.session.Session;

public interface SessionDAOService {

	/**
	 * Create a session
	 *
	 * @param session The session to create
	 * @return The saved session
	 */
	Session insertSession(Session session);

	/**
	 * Update a session
	 *
	 * @param session The session to update
	 * @return The saved session
	 */
	Session updateSession(Session session);

	/**
	 * Delete a session
	 *
	 * @param session The session to delete
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
	 * Get all sessions
	 *
	 * @return A list of sessions
	 */
	List<Session> getSessions();

	/**
	 * Delete expired sessions
	 *
	 * @param expiryDate The upper date limit
	 */
	void deleteOlderSession(ZonedDateTime expiryDate);

}
