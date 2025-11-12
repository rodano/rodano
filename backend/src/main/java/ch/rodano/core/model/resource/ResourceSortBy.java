package ch.rodano.core.model.resource;

import org.jooq.TableField;

import static ch.rodano.core.model.jooq.Tables.RESOURCE;
import static ch.rodano.core.model.jooq.Tables.SCOPE;

public enum ResourceSortBy {
	creationTime(RESOURCE.CREATION_TIME),
	lastUpdateTime(RESOURCE.LAST_UPDATE_TIME),
	category(RESOURCE.CATEGORY_ID),
	isPublic(RESOURCE.PUBLIC_RESOURCE),
	scopeShortname(SCOPE.SHORTNAME);

	private final TableField<?, ?> field;

	ResourceSortBy(final TableField<?, ?> field) {
		this.field = field;
	}

	public TableField<?, ?> getField() {
		return field;
	}

}
