package ch.rodano.core.model.common;

public interface DeletableObject extends IdentifiableObject {
	/**
	 * Check if the object is deleted
	 *
	 * @return true if the object is deleted, false otherwise
	 */
	boolean getDeleted();

	void setDeleted(final boolean deleted);

	/**
	 * Mark an object as deleted
	 */
	default void delete() {
		setDeleted(true);
	}

	/**
	 * Mark an object as not deleted
	 */
	default void restore() {
		setDeleted(false);
	}
}
