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
	 * Save a session
	 *
	 * @param session The session to save
	 * @return The saved session
	 */
	void saveSession(Session session);

	/**
	 * Delete a session
	 *
	 * @param session The session to delete
	 */
	void deleteSession(Session session);

	/**
	 * Delete all sessions of a user
	 *
	 * @param userPk The pk of the user
	 */
	void deleteSessionsByUserPk(Long userPk);

	/**
	 * Delete all sessions of a user except one
	 *
	 * @param userPk            The pk of the user
	 * @param retainedSessionPk The pk of the session to keep
	 */
	void deleteOtherSessionsByUserPk(Long userPk, Long retainedSessionPk);

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
	void deleteExpiredSessions(ZonedDateTime expiryDate);

	/**
	 * Update the last access time of a session
	 *
	 * @param sessionPk The pk of the session
	 * @param time      The new last access time
	 */
	void updateLastAccessTime(Long sessionPk, ZonedDateTime time);

}
