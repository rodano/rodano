package ch.rodano.core.model.common;

public interface LockableObject extends IdentifiableObject {
	/**
	 * Check if the object is locked
	 *
	 * @return true if the object is locked, false otherwise
	 */
	Boolean getLocked();

	void setLocked(final Boolean locked);

	/**
	 * Mark an object as locked
	 */
	default void lock() {
		setLocked(true);
	}

	/**
	 * Mark an object as not locked
	 */
	default void unlock() {
		setLocked(false);
	}
}
