package ch.rodano.configuration.model.cms;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.rights.Rights;

public class RequiredRight implements Node {
	@Serial
	private static final long serialVersionUID = -3789324658324836433L;

	public static final Collection<Entity> REQUIRE_RIGHT_ENTITIES = List.of(
		Entity.SCOPE_MODEL,
		Entity.EVENT_MODEL,
		Entity.FORM_MODEL,
		Entity.DATASET_MODEL,
		Entity.PROFILE
	);

	private String id;
	private Rights right;
	private Entity rightEntity;

	public RequiredRight() {
		//required by serialization
	}

	public RequiredRight(final String id, final Rights right, final Entity rightEntity) {
		this.id = id;
		this.right = right;
		this.rightEntity = rightEntity;
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final Rights getRight() {
		return right;
	}

	public final void setRight(final Rights right) {
		this.right = right;
	}

	public final Entity getRightEntity() {
		return rightEntity;
	}

	public final void setRightEntity(final Entity rightEntity) {
		this.rightEntity = rightEntity;
	}

	@Override
	public final Entity getEntity() {
		return Entity.REQUIRED_RIGHT;
	}

	@JsonIgnore
	public boolean isValid() {
		return StringUtils.isNotBlank(id) && right != null && rightEntity != null && REQUIRE_RIGHT_ENTITIES.contains(rightEntity);
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
