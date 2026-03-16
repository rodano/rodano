package ch.rodano.core.services.bll.session;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

/**
 * Thread-safe accumulator for session last-access-time updates.
 * Drained atomically by the {@code SessionUpdaterTask} scheduler.
 */
@Component
public class PendingSessionUpdates {

	private final AtomicReference<ConcurrentHashMap<Long, ZonedDateTime>> buffer = new AtomicReference<>(new ConcurrentHashMap<>());

	/**
	 * Add a session to the update list.
	 * If the session already has a pending update, keeps the more recent timestamp.
	 */
	public void add(final Long sessionPk, final ZonedDateTime time) {
		buffer.get().merge(sessionPk, time, (existing, incoming) -> incoming.isAfter(existing) ? incoming : existing);
	}

	/**
	 * Atomically swaps the buffer with a new empty one and returns all accumulated updates.
	 */
	public Map<Long, ZonedDateTime> drain() {
		return buffer.getAndSet(new ConcurrentHashMap<>());
	}
}
