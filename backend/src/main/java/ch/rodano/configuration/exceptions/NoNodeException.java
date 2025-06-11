package ch.rodano.configuration.exceptions;

import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

public class NoNodeException extends RuntimeException {
	private static final long serialVersionUID = 5506135232567556526L;

	/**
	 * Constructor
	 *
	 * @param parentNode
	 * @param soughtEntity
	 * @param soughtNodeId
	 */
	public <T extends Displayable & Node>NoNodeException(final T parentNode, final Entity soughtEntity, final String soughtNodeId) {
		super(String.format("No %s with id [%s] in %s with id [%s]", soughtEntity.name(), soughtNodeId, parentNode.getEntity().name(), parentNode.getId()));
	}

	/**
	 * Constructor
	 *
	 * @param parentNode
	 * @param soughtEntity
	 * @param soughtNodeId
	 */
	public NoNodeException(final Node parentNode, final Entity soughtEntity, final String soughtNodeId) {
		super(String.format("No %s with id [%s] in %s", soughtEntity.name(), soughtNodeId, parentNode.getEntity().name()));
	}

	/**
	 * Constructor
	 *
	 * @param soughtEntity
	 * @param soughtNodeId
	 */
	public NoNodeException(final Entity soughtEntity, final String soughtNodeId) {
		super(String.format("No %s with id [%s]", soughtEntity.name(), soughtNodeId));
	}
}
