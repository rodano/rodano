package ch.rodano.configuration.model.rights;

import ch.rodano.configuration.model.common.Displayable;
import ch.rodano.configuration.model.common.Node;

/**
 * Attributable is very like Assignable
 * It allow rights management on profile right assignable
 */
public interface Attributable<T> extends Node, Comparable<T>, Displayable {
	String getAssignableDescription();
}
