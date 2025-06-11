package ch.rodano.core.model.resource;

import org.jooq.TableField;

import static ch.rodano.core.model.jooq.Tables.RESOURCE;

public enum ResourceSortBy {
	creationTime(RESOURCE.CREATION_TIME),
	lastUpdateTime(RESOURCE.LAST_UPDATE_TIME),
	title(RESOURCE.TITLE),
	category(RESOURCE.CATEGORY_ID),
	isPublic(RESOURCE.PUBLIC_RESOURCE),
	scopeShortname(RESOURCE.scope().SHORTNAME);

	private final TableField<?, ?> field;

	ResourceSortBy(final TableField<?, ?> field) {
		this.field = field;
	}

	public TableField<?, ?> getField() {
		return field;
	}

}
