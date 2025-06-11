package ch.rodano.configuration.model.profile;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

public class ProfileRight implements Node {
	private static final long serialVersionUID = 3498268060491376410L;

	private boolean system;
	private Set<String> profileIds;

	public ProfileRight() {
		profileIds = new TreeSet<>();
	}

	public final boolean isSystem() {
		return system;
	}

	public final void setSystem(final boolean system) {
		this.system = system;
	}

	public final Set<String> getProfileIds() {
		return profileIds;
	}

	public final void setProfileIds(final Set<String> profileIds) {
		this.profileIds = profileIds;
	}

	@Override
	public Entity getEntity() {
		return Entity.PROFILE_RIGHT;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
