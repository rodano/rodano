package ch.rodano.configuration.model.cms;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.rights.Rights;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class ScopeCriterionRight implements Node {
	private static final long serialVersionUID = -3789324658324836433L;

	private String id;
	private Rights right;
	private Entity rightEntity;

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
		return Entity.SCOPE_CRITERION_RIGHT;
	}

	@JsonIgnore
	public boolean isValid() {
		return StringUtils.isNotBlank(id) && right != null && rightEntity != null;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
