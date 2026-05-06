package ch.rodano.configuration.model.rights;

import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.common.Node;

/**
 * FamilyAssignableParent is like Assignable but can contain children
 * It allow rights management on an entity and its children
 */
public interface FamilyAssignableParent<T> extends Node, Comparable<T>, Displayable {
	String getAssignableDescription();
}
