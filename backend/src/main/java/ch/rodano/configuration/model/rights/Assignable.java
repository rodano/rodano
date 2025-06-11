package ch.rodano.configuration.model.rights;

import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.common.Node;

/**
 * Assignable is used for simple right management.
 * Any profile can have a right on any assignable node.
 */
public interface Assignable<T> extends Node, Comparable<T>, Displayable {
	String getAssignableDescription();
}
