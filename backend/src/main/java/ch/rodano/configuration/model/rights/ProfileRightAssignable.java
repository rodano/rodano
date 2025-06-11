package ch.rodano.configuration.model.rights;

import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.common.Node;

/**
 * ProfileRightAssignable is used for very advanced right management
 * Right depends of the profile that have created the node
 */
public interface ProfileRightAssignable<T> extends Node, Comparable<T>, Displayable {
	String getAssignableDescription();

	String getParentId();
}
