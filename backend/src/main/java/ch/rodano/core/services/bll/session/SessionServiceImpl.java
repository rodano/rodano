package ch.rodano.core.services.bll.session;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;

import ch.rodano.core.model.session.Session;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.dao.session.SessionDAOService;

@Service
public class SessionServiceImpl implements SessionService {

	private final SessionDAOService sessionDAOService;

	public SessionServiceImpl(final SessionDAOService sessionDAOService) {
		this.sessionDAOService = sessionDAOService;
	}

	@Override
	public Session createSession(final User user) {
		final var now = ZonedDateTime.now();
		final var session = new Session();
		session.setUserFk(user.getPk());
		session.setCreationTime(now);
		session.setLastAccessTime(ZonedDateTime.now());

		final var token = generateSessionToken();
		session.setToken(token);

		return sessionDAOService.insertSession(session);
	}

	@Override
	public Session refreshSession(final Session session) {
		session.setLastAccessTime(ZonedDateTime.now());
		return sessionDAOService.updateSession(session);
	}

	@Override
	public void deleteSession(final Session session) {
		sessionDAOService.deleteSession(session);
	}

	@Override
	public Session getSessionByToken(final String token) {
		return sessionDAOService.getSessionByToken(token);
	}

	@Override
	public Session getSessionByPk(final Long pk) {
		return sessionDAOService.getSessionByPk(pk);
	}

	@Override
	public void deleteOldSessions(final Integer ageInMinutes) {
		final var expiryDate = ZonedDateTime.now().minusMinutes(ageInMinutes);
		sessionDAOService.deleteOlderSession(expiryDate);
	}

	@Override
	public List<Session> getSessions() {
		return sessionDAOService.getSessions();
	}

	private String generateSessionToken() {
		final var random = new SecureRandom();
		final var bytes = new byte[SESSION_TOKEN_BYTE_LENGTH];
		random.nextBytes(bytes);

		final var encoder = Base64.getUrlEncoder().withoutPadding();
		return encoder.encodeToString(bytes);
	}
}
