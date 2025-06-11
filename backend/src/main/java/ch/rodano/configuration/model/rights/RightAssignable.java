package ch.rodano.configuration.model.rights;

import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.common.Node;

/**
 * RightAssignable is used for advanced right management
 * Right type must be defined (WRITE, READ)
 */
public interface RightAssignable<T> extends Comparable<T>, Node, Displayable {

	String getAssignableDescription();
}
