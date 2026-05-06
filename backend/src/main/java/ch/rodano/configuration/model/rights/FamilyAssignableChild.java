package ch.rodano.configuration.model.rights;

import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.common.Node;

/**
 * FamilyAssignableChild is used for children in family assignables
 */
public interface FamilyAssignableChild<T> extends Node, Comparable<T>, Displayable {
	String getAssignableDescription();

	String getParentId();
}
