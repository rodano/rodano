package ch.rodano.core.model.common;

public interface RemovableObject extends IdentifiableObject {
	/**
	 * Check if the object is removed
	 *
	 * @return true if the object is removed, false otherwise
	 */
	boolean isRemoved();

	void setRemoved(final boolean removed);

	/**
	 * Mark an object as removed
	 */
	default void remove() {
		setRemoved(true);
	}

	/**
	 * Mark an object as not removed
	 */
	default void restore() {
		setRemoved(false);
	}
}
