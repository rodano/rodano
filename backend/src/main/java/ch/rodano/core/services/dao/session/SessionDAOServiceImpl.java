package ch.rodano.core.services.dao.session;

import java.time.ZonedDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.session.Session;

import static ch.rodano.core.model.jooq.Tables.USER_SESSION;

@Service
public class SessionDAOServiceImpl implements SessionDAOService {

	private final DSLContext create;

	public SessionDAOServiceImpl(
		final DSLContext create
	) {
		this.create = create;
	}

	@Override
	public Session insertSession(final Session session) {
		final var record = create.newRecord(USER_SESSION, session);
		record.insert();
		session.setPk(record.getPk());
		return session;
	}

	@Override
	public Session updateSession(final Session session) {
		final var record = create.newRecord(USER_SESSION, session);
		record.update();
		return session;
	}

	@Override
	public void deleteSession(final Session session) {
		create.deleteFrom(USER_SESSION).where(USER_SESSION.PK.eq(session.getPk())).execute();
	}

	@Override
	public Session getSessionByToken(final String token) {
		return create.selectFrom(USER_SESSION).where(USER_SESSION.TOKEN.eq(token)).fetchOneInto(Session.class);
	}

	@Override
	public Session getSessionByPk(final Long pk) {
		return create.selectFrom(USER_SESSION).where(USER_SESSION.PK.eq(pk)).fetchOneInto(Session.class);
	}

	@Override
	public List<Session> getSessions() {
		return create.selectFrom(USER_SESSION).fetchInto(Session.class);
	}

	@Override
	public void deleteOlderSession(final ZonedDateTime expiryDate) {
		create.deleteFrom(USER_SESSION).where(USER_SESSION.LAST_ACCESS_TIME.le(expiryDate)).execute();
	}
}
