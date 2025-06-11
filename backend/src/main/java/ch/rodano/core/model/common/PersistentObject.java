package ch.rodano.core.model.common;

import ch.rodano.configuration.model.study.Study;

public interface PersistentObject {

	/**
	 * Called when an object is ready to be persisted in the database
	 */
	void onPreUpdate();

	/**
	 * Called when an object has been persisted in the database
	 */
	void onPostUpdate(Study study);

	/**
	 * Called when an object has been loaded
	 */
	void onPostLoad(Study study);
}
