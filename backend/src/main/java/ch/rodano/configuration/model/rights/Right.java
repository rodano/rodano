package ch.rodano.configuration.model.rights;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.profile.ProfileRight;

public class Right implements Node {
	private static final long serialVersionUID = -6284310979400853784L;

	private boolean right;
	private SortedMap<String, ProfileRight> childRights;

	public Right() {
		childRights = new TreeMap<>();
	}

	public final boolean isRight() {
		return right;
	}

	public final void setRight(final boolean right) {
		this.right = right;
	}

	public final SortedMap<String, ProfileRight> getChildRights() {
		return childRights;
	}

	public final void setChildRights(final SortedMap<String, ProfileRight> childRights) {
		this.childRights = childRights;
	}

	@Override
	public final Entity getEntity() {
		return Entity.RIGHT;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
